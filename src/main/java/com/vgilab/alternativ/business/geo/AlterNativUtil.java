package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.AlterNativ;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author smuellner
 */
public class AlterNativUtil {

    /**
     * create a list of coordinates for a Track of an AlterNativ
     * 
     * @param alterNativ
     * @return 
     */
    public static List<Coordinate3D> getCoordinatesFromTrack(final AlterNativ alterNativ) {
        final List<Coordinate3D> coordinates = new LinkedList<>();
        coordinates.add(new Coordinate3D(alterNativ.getOrigin().getLng(), alterNativ.getOrigin().getLat(), 0d));
        alterNativ.getTracks().stream().map((curTrack) -> curTrack.getLocation()).forEach((location) -> {
            coordinates.add(new Coordinate3D(location.getCoords().getLongitude(), location.getCoords().getLatitude(), 0d));
        });
        coordinates.add(new Coordinate3D(alterNativ.getDestination().getLng(), alterNativ.getDestination().getLat(), 0d));
        return coordinates;
    }
      
    /**
     * create a list of coordinates for a Track of an AlterNativ
     * 
     * @param alterNativ
     * @return 
     */
    public static List<Coordinate> getCoordinates2DFromTrack(final AlterNativ alterNativ) {
        final List<Coordinate> coordinates = new LinkedList<>();
        coordinates.add(new Coordinate(alterNativ.getOrigin().getLng(), alterNativ.getOrigin().getLat()));
        alterNativ.getTracks().stream().map((curTrack) -> curTrack.getLocation()).forEach((location) -> {
            coordinates.add(new Coordinate(location.getCoords().getLongitude(), location.getCoords().getLatitude()));
        });
        coordinates.add(new Coordinate(alterNativ.getDestination().getLng(), alterNativ.getDestination().getLat()));
        return coordinates;
    }
}
