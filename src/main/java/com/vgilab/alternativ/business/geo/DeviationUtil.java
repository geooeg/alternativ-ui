package com.vgilab.alternativ.business.geo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.LinearRing;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.geotools.geometry.jts.JTSFactoryFinder;

/**
 *
 * @author smuellner
 */
public class DeviationUtil {
    
    public static LinearRing createRingFromSegment(final DeviationSegment deviationSegment) {
        final Coordinate[] coordinates = createRingAsArrayFromSegment(deviationSegment);
        final LinearRing ring = JTSFactoryFinder.getGeometryFactory().createLinearRing(coordinates);
        return ring;
    }
    
    public static CoordinateList createRingAsCoordinateListFromSegment(final DeviationSegment deviationSegment) {
        // merge two line strings to one linearRing
        final CoordinateList coordinateList = new CoordinateList(deviationSegment.getSegmentLineX().getCoordinates());
        final List<Coordinate> coordinatesY = Arrays.asList(deviationSegment.getSegmentLineY().getCoordinates());
        Collections.reverse(coordinatesY);
        coordinateList.addAll(coordinatesY, false);
        // close geometry
        coordinateList.closeRing();
        return coordinateList;
    }
    
    public static Coordinate[] createRingAsArrayFromSegment(final DeviationSegment deviationSegment) {
        final CoordinateList coordinateList = createRingAsCoordinateListFromSegment(deviationSegment);
        return coordinateList.toCoordinateArray();
    }
    
    public static List<Coordinate> createRingAsListFromSegment(final DeviationSegment deviationSegment) {
        final Coordinate[] coordinateArray = DeviationUtil.createRingAsArrayFromSegment(deviationSegment);
        return Arrays.asList(coordinateArray);
    }
    
    public static List<Coordinate3D> createRingOfCoordinatesFromSegment(final DeviationSegment deviationSegment) {
        final List<Coordinate3D> coordinates3D = new LinkedList<>();
        final List<Coordinate> coordinates = DeviationUtil.createRingAsListFromSegment(deviationSegment);
        coordinates.stream().forEach(coordinate -> {
            final Coordinate3D coordinate3D = new Coordinate3D(coordinate.x, coordinate.y, coordinate.z);
            coordinates3D.add(coordinate3D);
        });
        return coordinates3D;
    }
}
