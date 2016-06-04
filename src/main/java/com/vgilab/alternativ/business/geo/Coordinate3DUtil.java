package com.vgilab.alternativ.business.geo;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.LinkedList;
import java.util.List;
import org.primefaces.model.map.LatLng;

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
    
    public static List<Coordinate3D> convertToCoordinates3D(List<Coordinate> coordinates){
        final List<Coordinate3D> coordinates3D = new LinkedList<>();
        coordinates.stream().forEach((coordinate) -> {
            final Coordinate3D coordinate3D = new Coordinate3D(coordinate.x, coordinate.y, coordinate.z);
            coordinates3D.add(coordinate3D);
        });
        return coordinates3D;
    }
    
    public static List<LatLng> convertToLatLngs(List<Coordinate> coordinates){
        final List<LatLng> latLngs = new LinkedList<>();
        coordinates.stream().forEach((coordinate) -> {
            latLngs.add(new LatLng(coordinate.y, coordinate.x));
        });
        return latLngs;
    }
}
