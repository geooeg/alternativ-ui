package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.business.busstop.BusStop;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Feature;
import com.vgilab.alternativ.generated.Leg;
import com.vgilab.alternativ.generated.Route;
import com.vgilab.alternativ.generated.Step;
import com.vgilab.alternativ.generated.Track;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
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

    private final static Logger LOGGER = Logger.getGlobal();

    public List<SimpleFeature> createPointsFromTracks(final List<Track> tracks, final String tripId) {
        final List<SimpleFeature> features = new ArrayList<>();
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getPointTypeForTracks());
        for (final Track curTrack : tracks) {
            if (null != curTrack.getLocation() && null != curTrack.getLocation().getCoords()) {
                final SimpleFeature feature = this.buildFeatureFromTrack(curTrack, tripId, featureBuilder);
                features.add(feature);
            }
        }
        return features;
    }

    public SimpleFeature createLineFromTracks(final List<Track> tracks, final String tripId) {
        final List<Coordinate> coordinates = new ArrayList<>();
        for (final Track curTrack : tracks) {
            if (null != curTrack.getLocation() && null != curTrack.getLocation().getCoords()) {
                final Coordinate coordinate = new Coordinate(curTrack.getLocation().getCoords().getLongitude(), curTrack.getLocation().getCoords().getLatitude());
                coordinates.add(coordinate);
            }
        }
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        if (coordinates.size() > 1) {
            final LineString line = geometryFactory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
            final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getLineTypeForTracks());
            featureBuilder.add(line);
            featureBuilder.add(tripId);
            return featureBuilder.buildFeature(null);
        }
        return null;
    }

    public Map<Track, Point> createTrackPointMapFromTracks(final List<Track> tracks, final String tripId) {
        final Map<Track, Point> trackPointMap = new HashMap<>();
        for (final Track curTrack : tracks) {
            if (null != curTrack.getLocation() && null != curTrack.getLocation().getCoords()) {
                final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                final Coordinate coordinate = new Coordinate(curTrack.getLocation().getCoords().getLongitude(), curTrack.getLocation().getCoords().getLatitude());
                trackPointMap.put(curTrack, geometryFactory.createPoint(coordinate));
            }
        }
        return trackPointMap;
    }

    public Map<Point, Track> createPointTrackMapFromTracks(final List<Track> tracks, final String tripId) {
        final Map<Point, Track> pointTrackMap = new HashMap<>();
        for (final Track curTrack : tracks) {
            if (null != curTrack.getLocation() && null != curTrack.getLocation().getCoords()) {
                final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                final Coordinate coordinate = new Coordinate(curTrack.getLocation().getCoords().getLongitude(), curTrack.getLocation().getCoords().getLatitude());
                pointTrackMap.put(geometryFactory.createPoint(coordinate), curTrack);
            }
        }
        return pointTrackMap;
    }

    public SimpleFeatureType getPointTypeForTracks() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Point");
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("the_geom", Point.class); // then add geometry
        featureTypeBuilder.add("trip_id", String.class);
        featureTypeBuilder.add("timestamp", Long.class);
        return featureTypeBuilder.buildFeatureType();
    }

    public SimpleFeatureType getLineTypeForTracks() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Line");
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("the_geom", LineString.class); // then add geometry
        featureTypeBuilder.add("trip_id", String.class);
        return featureTypeBuilder.buildFeatureType();
    }

    public Integer getPointCountForTracks(List<Track> tracks) {
        return tracks.size();
    }

    public Integer getPointCountForChosenRoutes(final List<ChosenRoute> chosenRoutes) {
        int size = 0;
        for (final ChosenRoute curChosenRoute : chosenRoutes) {
            size += this.getCoordinatesFromChosenRoute(curChosenRoute).size();
        }
        return size;
    }

    public Integer getPointCountForChosenRoute(final ChosenRoute chosenRoute) {
        return this.getCoordinatesFromChosenRoute(chosenRoute).size();
    }

    public List<SimpleFeature> createPointsFromChosenRoute(final ChosenRoute chosenRoute, final String tripId) {
        final List<SimpleFeature> features = new ArrayList<>();
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getPointTypeForChosenRoute());
        for (final Route curRoute : chosenRoute.getRoutes()) {
            if (null != curRoute.getLegs()) {
                for (final Leg curLeg : curRoute.getLegs()) {
                    if (null != curLeg.getSteps()) {
                        for (final Step curStep : curLeg.getSteps()) {
                            final String travelMode = curStep.getTravelMode();
                            final List<Coordinate> coordinates = this.decodePolyline(curStep.getPolyline().getPoints());
                            for (final Coordinate curCoordinate : coordinates) {
                                final Point point = geometryFactory.createPoint(curCoordinate);
                                featureBuilder.add(point);
                                featureBuilder.add(tripId);
                                featureBuilder.add(travelMode);
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

    public SimpleFeature createLineFromChosenRoute(final ChosenRoute chosenRoute, final String tripId) {

        final List<Coordinate> coordinates = new ArrayList<>();
        for (final Route curRoute : chosenRoute.getRoutes()) {
            if (null != curRoute.getLegs()) {
                for (final Leg curLeg : curRoute.getLegs()) {
                    if (null != curLeg.getSteps()) {
                        for (final Step curStep : curLeg.getSteps()) {
                            coordinates.addAll(this.decodePolyline(curStep.getPolyline().getPoints()));

                        }
                    }
                }
            }
        }
        if (coordinates.size() > 1) {
            final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getLineTypeForChosenRoute());
            final LineString line = geometryFactory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
            featureBuilder.add(line);
            featureBuilder.add(tripId);
            return featureBuilder.buildFeature(null);
        }
        return null;
    }

    private List<Coordinate> getCoordinatesFromRoute(final Route curRoute) {
        final List<Coordinate> coordinates = new LinkedList<>();
        if (null != curRoute.getLegs()) {
            for (final Leg curLeg : curRoute.getLegs()) {
                if (null != curLeg.getSteps()) {
                    for (final Step curStep : curLeg.getSteps()) {
                        coordinates.addAll(this.decodePolyline(curStep.getPolyline().getPoints()));
                    }
                }
            }
        }
        return coordinates;
    }

    public Map<Route, LineString> createRouteLineStringMapFromChosenRoute(final ChosenRoute chosenRoute, final String tripId) {
        final Map<Route, LineString> stepFeatureMap = new HashMap<>();
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        for (final Route curRoute : chosenRoute.getRoutes()) {
            final List<Coordinate> coordinates = this.getCoordinatesFromRoute(curRoute);
            final LineString lineString = geometryFactory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
            stepFeatureMap.put(curRoute, lineString);
        }
        return stepFeatureMap;
    }

    public Map<Route, Map<Step, List<Point>>> createRouteStepPointMapFromChosenRoute(final ChosenRoute chosenRoute, final String tripId) {
        final Map<Route, Map<Step, List<Point>>> routeStepPointMap = new HashMap<>();
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        for (final Route curRoute : chosenRoute.getRoutes()) {
            final Map<Step, List<Point>> stepPointMap = new HashMap<>();
            if (null != curRoute.getLegs()) {
                for (final Leg curLeg : curRoute.getLegs()) {
                    if (null != curLeg.getSteps()) {
                        for (final Step curStep : curLeg.getSteps()) {
                            final List<Point> points = new LinkedList<>();
                            final List<Coordinate> coordinates = this.decodePolyline(curStep.getPolyline().getPoints());
                            for (final Coordinate curCoordinate : coordinates) {
                                points.add(geometryFactory.createPoint(curCoordinate));
                            }
                            stepPointMap.put(curStep, points);
                        }
                    }
                }
            }
            routeStepPointMap.put(curRoute, stepPointMap);
        }
        return routeStepPointMap;
    }

    public List<Coordinate> getCoordinatesFromChosenRoute(final ChosenRoute chosenRoute) {
        final List<Coordinate> coordinates = new LinkedList<>();
        for (final Route curRoute : chosenRoute.getRoutes()) {
            coordinates.addAll(this.getCoordinatesFromRoute(curRoute));
        }
        return coordinates;
    }

    public SimpleFeatureType getPointTypeForChosenRoute() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Point");
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("the_geom", Point.class); // then add geometry
        featureTypeBuilder.add("trip_id", String.class);
        featureTypeBuilder.add("travelmode", String.class);
        return featureTypeBuilder.buildFeatureType();
    }

    public SimpleFeatureType getLineTypeForChosenRoute() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Line");
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("the_geom", LineString.class); // then add geometry
        featureTypeBuilder.add("trip_id", String.class);
        return featureTypeBuilder.buildFeatureType();
    }

    public List<SimpleFeature> createPointsFromBusStops(List<BusStop> busStops) {
        final List<SimpleFeature> features = new ArrayList<>();
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getPointTypeForBusStops());
        for (final BusStop curBusStop : busStops) {
            if (null != curBusStop.getLatLng()) {
                final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                final Coordinate coordinate = new Coordinate(curBusStop.getLatLng().getLng(), curBusStop.getLatLng().getLat());
                final Point point = geometryFactory.createPoint(coordinate);
                featureBuilder.add(point);
                featureBuilder.add(curBusStop.getCode());
                featureBuilder.add(curBusStop.getName());
                featureBuilder.add(curBusStop.getDescription());
                final SimpleFeature feature = featureBuilder.buildFeature(null);
                features.add(feature);
            }
        }
        return features;
    }

    public SimpleFeatureType getPointTypeForBusStops() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Point");
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("the_geom", Point.class); // then add geometry
        featureTypeBuilder.add("code", String.class);
        featureTypeBuilder.add("name", String.class);
        featureTypeBuilder.add("description", String.class);
        return featureTypeBuilder.buildFeatureType();
    }

    public List<SimpleFeature> createPointsFromTelofuns(List<com.vgilab.alternativ.generated.Feature> telofuns) {
        final List<SimpleFeature> features = new ArrayList<>();
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getPointTypeForTelofuns());
        for (final Feature curFeature : telofuns) {
            if (null != curFeature.getGeometry()) {
                final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                final Coordinate coordinate = new Coordinate(curFeature.getGeometry().getX(), curFeature.getGeometry().getY());
                final Point point = geometryFactory.createPoint(coordinate);
                featureBuilder.add(point);
                final SimpleFeature feature = featureBuilder.buildFeature(null);
                features.add(feature);
            }
        }
        return features;
    }

    public SimpleFeatureType getPointTypeForTelofuns() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Point");
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("the_geom", Point.class); // then add geometry
        return featureTypeBuilder.buildFeatureType();
    }

    public List<SimpleFeature> createPointsForCoordinates(List<Coordinate3D> coordinates) {
        final List<SimpleFeature> features = new ArrayList<>();
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getPointTypeForTelofuns());
        for (final Coordinate3D curCoordinate : coordinates) {
            final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            final Coordinate coordinate = new Coordinate(curCoordinate.getLongitude(), curCoordinate.getLatitude(), curCoordinate.getAltitude());
            final Point point = geometryFactory.createPoint(coordinate);
            featureBuilder.add(point);
            final SimpleFeature feature = featureBuilder.buildFeature(null);
            features.add(feature);
        }
        return features;
    }

    public SimpleFeatureType getPointTypeForCoordinates() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Point");
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("the_geom", Point.class); // then add geometry
        return featureTypeBuilder.buildFeatureType();
    }

    public List<SimpleFeature> createLinesForCoordinates(List<Coordinate3D> coordinates3D) {
        final List<Coordinate> coordinates = new ArrayList<>();
        for (final Coordinate3D curCoordinate3D : coordinates3D) {
            final Coordinate coordinate = new Coordinate(curCoordinate3D.getLongitude(), curCoordinate3D.getLatitude(), curCoordinate3D.getAltitude());
            coordinates.add(coordinate);
        }
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        if (coordinates.size() > 1) {
            final LineString line = geometryFactory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
            final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(this.getLineTypeForTracks());
            featureBuilder.add(line);
            return Collections.singletonList(featureBuilder.buildFeature(null));
        }
        return null;
    }

    public SimpleFeatureType getLineTypeForCoordinates() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("Line");
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("the_geom", LineString.class); // then add geometry
        return featureTypeBuilder.buildFeatureType();
    }

    /**
     * https://developers.google.com/maps/documentation/utilities/polylineutility
     *
     * @param encoded
     * @return
     */
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
            final Double latitude = ((double) lat / 1E5);
            final Double longitude = ((double) lng / 1E5);
            final Coordinate c = new Coordinate(longitude, latitude);
            coordinates.add(c);
        }
        return coordinates;
    }

    private SimpleFeature buildFeatureFromTrack(final Track curTrack, final String tripId, final SimpleFeatureBuilder featureBuilder) {
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        final Coordinate coordinate = new Coordinate(curTrack.getLocation().getCoords().getLongitude(), curTrack.getLocation().getCoords().getLatitude());
        final Point point = geometryFactory.createPoint(coordinate);
        featureBuilder.add(point);
        featureBuilder.add(tripId);
        String timestamp = curTrack.getLocation().getTimestamp();
        if (StringUtils.isNotBlank(timestamp)) {
            try {
                // ISO8601: https://en.wikipedia.org/wiki/ISO_8601 
                final DateTimeFormatter patternFormat = new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        .appendTimeZoneOffset("Z", true, 2, 4)
                        .toFormatter();
                final DateTime dateTime = patternFormat.parseDateTime(timestamp);
                // 2016-01-10T14:47:35.820Z
                final Long unixTimeStamp = dateTime.getMillis() / 1000;
                featureBuilder.add(unixTimeStamp);
            } catch (IllegalArgumentException ex) {
                LOGGER.severe(MessageFormat.format("Track {0} contains a not ISO8601 conform timestamp {1}. Exception: {2} ", curTrack.getId(), timestamp, ex.getLocalizedMessage()));
            }
        }
        return featureBuilder.buildFeature(null);
    }
}
