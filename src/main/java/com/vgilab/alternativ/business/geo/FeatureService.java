package com.vgilab.alternativ.business.geo;

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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.stereotype.Service;

/**
 *
 * @author smuellner
 */
@Service
public class FeatureService {

    private SimpleFeatureTypeBuilder createFeatureTypeBuilder() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
//set the name
        featureTypeBuilder.setName("Point");
//add some properties
        featureTypeBuilder.add("id", String.class);
//add timestamp
        featureTypeBuilder.add("timestamp", Integer.class);
//add a geometry property
        featureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84); // set crs first
        featureTypeBuilder.add("location", Point.class); // then add geometry
//build the type
        return featureTypeBuilder;
    }

    public List<SimpleFeature> createFeaturesFromTracks(List<Track> tracks) {
        /*
         * A list to collect features as we create them.
         */
        final List<SimpleFeature> features = new ArrayList<>();
        /*
         * GeometryFactory will be used to create the geometry attribute of each feature,
         * using a Point object for the location.
         */
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        final SimpleFeatureType TYPE = this.createFeatureTypeBuilder().buildFeatureType();
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        for (final Track curTrack : tracks) {
            if (null != curTrack.getLocation() && null != curTrack.getLocation().getCoords()) {
                final Point point = geometryFactory.createPoint(new Coordinate(curTrack.getLocation().getCoords().getLongitude(), curTrack.getLocation().getCoords().getLatitude()));
                featureBuilder.add(curTrack.getId());
                if(StringUtils.isNotBlank(curTrack.getLocation().getTimestamp())) {
                    featureBuilder.add(Integer.valueOf(curTrack.getLocation().getTimestamp()));
                }
                featureBuilder.add(point);
                final SimpleFeature feature = featureBuilder.buildFeature(null);
                features.add(feature);
            }
        }
        return features; 
    }
}
