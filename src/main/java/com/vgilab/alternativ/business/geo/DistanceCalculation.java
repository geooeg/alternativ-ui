package com.vgilab.alternativ.business.geo;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.Iterator;
import java.util.List;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Zhang
 */
public class DistanceCalculation {

    final GeodeticCalculator geodeticCalculator = new GeodeticCalculator(DefaultGeographicCRS.WGS84);

    public Double calculate(final List<Coordinate> coordinates, final CoordinateReferenceSystem crs) throws TransformException {
        final Iterator<Coordinate> iterator = coordinates.iterator();
        Double distance = 0d;
        Coordinate previous = null;
        while (iterator.hasNext()) {
            final Coordinate current = iterator.next();
            if(null != previous) {
                final Double distanceForSegment = this.calculate(previous, current, crs);
                distance += null != distanceForSegment ? distanceForSegment : 0d;
            }
            previous = current;
        }
        return distance;
    }

    public Double calculate(final Coordinate p0, final Coordinate p1, final CoordinateReferenceSystem crs) throws TransformException {
        this.geodeticCalculator.setStartingPosition(JTS.toDirectPosition(p0, crs));
        this.geodeticCalculator.setDestinationPosition(JTS.toDirectPosition(p1, crs));
        return this.geodeticCalculator.getOrthodromicDistance();
    }
}
