package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Leg;
import com.vgilab.alternativ.generated.Route;
import com.vgilab.alternativ.generated.Step;
import com.vgilab.alternativ.generated.Track;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.stereotype.Service;

/**
 *
 * @author smuellner
 */
@Service
public class FeatureService {

    public List<SimpleFeature> createFeaturesFromTracks(final List<Track> tracks, final String tripId) {
        final List<SimpleFeature> features = new ArrayList<>();
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getTypeForTracks());
        for (final Track curTrack : tracks) {
            if (null != curTrack.getLocation() && null != curTrack.getLocation().getCoords()) {
                final Point point = geometryFactory.createPoint(new Coordinate(curTrack.getLocation().getCoords().getLongitude(), curTrack.getLocation().getCoords().getLatitude()));
                featureBuilder.add(tripId);
                if (StringUtils.isNotBlank(curTrack.getLocation().getTimestamp())) {
                    final DateTimeFormatter patternFormat = new DateTimeFormatterBuilder()
                            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                            .appendTimeZoneOffset("Z", true, 2, 4)
                            .toFormatter();
                    final DateTime dateTime = patternFormat.parseDateTime(curTrack.getLocation().getTimestamp());
                    // 2016-01-10T14:47:35.820Z
                    featureBuilder.add(dateTime.getMillis() / 1000);
                }
                featureBuilder.add(point);
                final SimpleFeature feature = featureBuilder.buildFeature(null);
                features.add(feature);
            }
        }
        return features;
    }

    public SimpleFeatureType getTypeForTracks() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Point");
        featureTypeBuilder.add("id", String.class);
        featureTypeBuilder.add("timestamp", Integer.class);
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("location", Point.class); // then add geometry
        return featureTypeBuilder.buildFeatureType();
    }

    public List<SimpleFeature> createFeaturesFromChosenRoute(final ChosenRoute chosenRoute, final String tripId) {
        final List<SimpleFeature> features = new ArrayList<>();
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getTypeForChosenRoute());
        for (final Route curRoute : chosenRoute.getRoutes()) {
            if (null != curRoute.getLegs()) {
                for (final Leg curLeg : curRoute.getLegs()) {
                    if (null != curLeg.getSteps()) {
                        for (final Step curStep : curLeg.getSteps()) {
                            final List<Coordinate> coordinates = this.decodePolyline(curStep.getPolyline().getPoints());
                            for (final Coordinate curCoordinate : coordinates) {
                                final Point point = geometryFactory.createPoint(curCoordinate);
                                featureBuilder.add(tripId);
                                featureBuilder.add(point);
                                final SimpleFeature feature = featureBuilder.buildFeature(null);
                                features.add(feature);
                            }
                        }
                    }
                }
            }
        }
        return features;
    }

    public SimpleFeatureType getTypeForChosenRoute() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Point");
        featureTypeBuilder.add("id", String.class);
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("location", Point.class); // then add geometry
        return featureTypeBuilder.buildFeatureType();
    }

    private List<Coordinate> decodePolyline(final String encoded) {
        final List<Coordinate> coordinates = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            final Coordinate c = new Coordinate((int) (((double) lat / 1E5) * 1E6),
                    (int) (((double) lng / 1E5) * 1E6));
            coordinates.add(c);
        }
        return coordinates;
    }
}
