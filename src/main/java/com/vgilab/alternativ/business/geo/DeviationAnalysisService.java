package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.AlterNativ;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author smuellner
 */
@Service
public class DeviationAnalysisService {

    private final static Logger LOGGER = Logger.getGlobal();

    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    @Autowired
    private FeatureService featureService;

    public List<DeviationSegment> createSegments(final AlterNativ alterNativ) {
        if (null == alterNativ || null == alterNativ.getTracks() || alterNativ.getTracks().isEmpty() || null == alterNativ.getChosenRoute() || alterNativ.getChosenRoute().isEmpty()) {
            return null;
        }
        final SimpleFeature featureFromTracks = this.featureService.createLineFromTracks(alterNativ.getTracks(), alterNativ.getId());
        final SimpleFeature featureFromChosenRoute = this.featureService.createLineFromChosenRoute(alterNativ.getChosenRoute().get(0), alterNativ.getId(), alterNativ.getUserId(), alterNativ.getChosenType());
        if (null == featureFromTracks || null == featureFromChosenRoute) {
            return null;
        }
        final LineString lineFromTracks = (LineString) featureFromTracks.getDefaultGeometry();
        final LineString lineFromChosenRoute = (LineString) featureFromChosenRoute.getDefaultGeometry();
        return this.createSegments(lineFromTracks, lineFromChosenRoute);
    }

    private List<DeviationSegment> createSegments(final LineString lineX, final LineString lineY) {
        if (null == lineX || null == lineY) {
            return null;
        }
        LOGGER.info(MessageFormat.format("Coordinates in Line X %d and Line Y %d", lineX.getCoordinates().length, lineY.getCoordinates().length));
        final List<DeviationSegment> segments = new LinkedList<>();
        final Geometry intersections = lineX.intersection(lineY);
        final Geometry intersectionsY = lineY.intersection(lineX);
        ///final Geometry diff = intersections.difference(intersectionsY);
        if (null != intersections.getCoordinates() && intersections.getCoordinates().length > 0) {
            final Coordinate[] intersectingCoordinates = intersections.getCoordinates();
            LOGGER.info(MessageFormat.format("Found %d intersections", intersectingCoordinates.length));
            final CoordinateList coordinatesX = new CoordinateList(lineX.getCoordinates());
            final CoordinateList coordinatesY = new CoordinateList(lineY.getCoordinates());
            for (final Coordinate curIntersectingCoordinate : intersectingCoordinates) {
                LOGGER.info(MessageFormat.format("Intersecting Coordinate at X %d , Y %d", curIntersectingCoordinate.x, curIntersectingCoordinate.y));
                final Point intersectingPoint = this.geometryFactory.createPoint(curIntersectingCoordinate);
                final Geometry intersectingBufferedGeometry = intersectingPoint.buffer(0.002);
                final PreparedGeometry preparedIntersectingGeometry = PreparedGeometryFactory.prepare(intersectingBufferedGeometry);
                final CoordinateList segmentCoordinatesX = new CoordinateList();
                Coordinate prevCoordinateX = null;
                for (final Coordinate curCoordinateX : coordinatesX.toCoordinateArray()) {
                    if (prevCoordinateX != null) {
                        final CoordinateList coordinates = new CoordinateList();
                        coordinates.add(prevCoordinateX);
                        coordinates.add(curCoordinateX);
                        final LineString line = this.geometryFactory.createLineString(coordinates.toCoordinateArray());
                        if (preparedIntersectingGeometry.intersects(line)) {
                            LOGGER.info(MessageFormat.format("Found intersection on Line X after %d coordinates", segmentCoordinatesX.size()));
                            break;
                        }
                    }
                    segmentCoordinatesX.add(curCoordinateX);
                    prevCoordinateX = curCoordinateX;
                }
                coordinatesX.removeAll(segmentCoordinatesX);
                segmentCoordinatesX.add(curIntersectingCoordinate);
                final CoordinateList segmentCoordinatesY = new CoordinateList();
                Coordinate prevCoordinateY = null;
                for (final Coordinate curCoordinateY : coordinatesY.toCoordinateArray()) {
                    if (prevCoordinateY != null) {
                        final CoordinateList coordinates = new CoordinateList();
                        coordinates.add(prevCoordinateY);
                        coordinates.add(curCoordinateY);
                        final LineString line = this.geometryFactory.createLineString(coordinates.toCoordinateArray());
                        if (preparedIntersectingGeometry.intersects(line)) {
                            LOGGER.info(MessageFormat.format("Found intersection on Line X after %d coordinates", segmentCoordinatesY.size()));
                            break;
                        }
                    }
                    segmentCoordinatesY.add(curCoordinateY);
                    prevCoordinateY = curCoordinateY;
                }
                coordinatesY.removeAll(segmentCoordinatesY);
                segmentCoordinatesY.add(curIntersectingCoordinate);
                if (segmentCoordinatesX.size() > 1 && segmentCoordinatesY.size() > 1) {
                    final LineString segmentLineX = this.geometryFactory.createLineString(segmentCoordinatesX.toCoordinateArray());
                    final LineString segmentLineY = this.geometryFactory.createLineString(segmentCoordinatesY.toCoordinateArray());
                    segments.add(new DeviationSegment(segmentLineX, segmentLineY));
                }
            }
        }
        return segments;
    }
    
    public double calculateTotalDeviationArea(List<DeviationSegment> segments) {
        if (null != segments) {
            double area = 0d;
            for (final DeviationSegment curDeviationSegment : segments) {
                final Polygon polygon = this.geometryFactory.createPolygon(DeviationUtil.createRingAsArrayFromSegment(curDeviationSegment));
                // sum up areas
                if (null != polygon) {
                    area += polygon.getArea();
                }
            }
            return area;
        }
        return -1d;
    }

    public List<DeviationSegment> filterSegmentsByDeviation(final List<DeviationSegment> segments, final Double deviationInMeters) {
        if (null != segments) {
            final Double deviation = null == deviationInMeters ? 1d : deviationInMeters;
            final List<DeviationSegment> filteredSegments = new LinkedList<>();
            final DistanceCalculation distanceCalculation = new DistanceCalculation();
            segments.stream().forEach((curDeviationSegment) -> {
                final Coordinate[] coordinatesX = curDeviationSegment.getSegmentLineX().getCoordinates();
                final Coordinate[] coordinatesY = curDeviationSegment.getSegmentLineY().getCoordinates();
                double longestDeviation = 0d;
                int idx = 0;
                if (coordinatesX.length < coordinatesY.length) {
                    for (final Coordinate curCoordinateX : coordinatesX) {
                        try {
                            final double distance = distanceCalculation.calculate(curCoordinateX, coordinatesY[idx], DefaultGeographicCRS.WGS84);
                            if (distance > longestDeviation) {
                                longestDeviation = distance;
                            }
                        } catch (TransformException ex) {
                            Logger.getLogger(DeviationAnalysisService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        idx++;
                    }
                } else {
                    for (final Coordinate curCoordinateY : coordinatesY) {
                        try {
                            final double distance = distanceCalculation.calculate(coordinatesX[idx], curCoordinateY, DefaultGeographicCRS.WGS84);
                            if (distance > longestDeviation) {
                                longestDeviation = distance;
                            }
                        } catch (TransformException ex) {
                            Logger.getLogger(DeviationAnalysisService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        idx++;
                    }
                }
                if (longestDeviation > deviation) {
                    filteredSegments.add(curDeviationSegment);
                }
            });
            return filteredSegments;
        }
        return null;
    }
}
