package com.vgilab.alternativ.business.geo;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Zhang
 */
public class Coordinate3DUtil {
    
    public static List<Coordinate> convert(List<Coordinate3D> coordinates3D){
        final List<Coordinate> coordinates = new LinkedList<>();
        coordinates3D.stream().forEach((coorindate3D) -> {
            final Coordinate coordinate = new Coordinate();
            coordinate.x = coorindate3D.getLongitude();
            coordinate.y = coorindate3D.getLatitude();
            coordinate.z = coorindate3D.getAltitude();
            coordinates.add(coordinate);
        });
        return coordinates;
    }
}
