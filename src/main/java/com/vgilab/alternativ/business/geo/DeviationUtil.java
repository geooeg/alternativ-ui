package com.vgilab.alternativ.business.geo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author smuellner
 */
public class DeviationUtil {
    
    public static Coordinate[] createRingAsArrayFromSegment(final DeviationSegment deviationSegment) {
        List<Coordinate> createRingFromSegment = DeviationUtil.createRingAsListFromSegment(deviationSegment);
        return createRingFromSegment.toArray(new Coordinate[createRingFromSegment.size()]);
    }
    
    public static List<Coordinate> createRingAsListFromSegment(final DeviationSegment deviationSegment) {
        // merge two line strings to one linearRing
        final CoordinateList coordinateList = new CoordinateList(deviationSegment.getSegmentLineX().getCoordinates());
        final List<Coordinate> coordinatesY = Arrays.asList(deviationSegment.getSegmentLineY().getCoordinates());
        Collections.reverse(coordinatesY);
        coordinateList.addAll(coordinatesY, false);
        // close geometry
        coordinateList.closeRing();
        return Arrays.asList(coordinateList.toCoordinateArray());
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
