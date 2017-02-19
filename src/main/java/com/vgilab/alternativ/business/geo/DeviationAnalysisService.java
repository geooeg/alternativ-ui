package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.AlterNativ;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
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
import org.springframework.util.CollectionUtils;

/**
 *
 * @author smuellner
 */
@Service
public class DeviationAnalysisService {

    private final static Logger LOGGER = Logger.getGlobal();

    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    
    private final static Integer FILTER_SIZE = 1;

    @Autowired
    private FeatureService featureService;

    public List<DeviationSegment> createSegments(final AlterNativ alterNativ) {
        if (null == alterNativ || null == alterNativ.getTracks() || alterNativ.getTracks().isEmpty() || null == alterNativ.getChosenRoute() || alterNativ.getChosenRoute().isEmpty()) {
            return null;
        }
        LOGGER.log(Level.INFO, "=== Create deviation segments for ID {0} ===", alterNativ.getId());
        final SimpleFeature featureFromTracks = this.featureService.createLineFromTracks(alterNativ.getTracks(), alterNativ.getId());
        final SimpleFeature featureFromChosenRoute = this.featureService.createLineFromChosenRoute(alterNativ.getChosenRoute().get(0), alterNativ.getId(), alterNativ.getUserId(), alterNativ.getChosenType());
        if (null == featureFromTracks || null == featureFromChosenRoute) {
            return null;
        }
        final LineString lineFromTracks = (LineString) featureFromTracks.getDefaultGeometry();
        final LineString lineFromChosenRoute = (LineString) featureFromChosenRoute.getDefaultGeometry();
        final List<DeviationSegment> segments = this.createSegments(lineFromTracks, lineFromChosenRoute);
        LOGGER.log(Level.INFO, "=== Found {0} deviation segments ===", segments.size());
        return segments;
    }

    public List<DeviationSegment> createSegments(final AlterNativ alterNativ, List<Coordinate> track) {
        if (null == alterNativ 
                || CollectionUtils.isEmpty(alterNativ.getChosenRoute())
                || CollectionUtils.isEmpty(track) 
                || track.size() < 2) {
            return null;
        }
        final Coordinate[] trackArray = track.toArray(new Coordinate[track.size()]);
        final LineString lineFromTrack = geometryFactory.createLineString(trackArray);
        final SimpleFeature featureFromChosenRoute = this.featureService.createLineFromChosenRoute(alterNativ.getChosenRoute().get(0), alterNativ.getId(), alterNativ.getUserId(), alterNativ.getChosenType());
        if (null == lineFromTrack || null == featureFromChosenRoute) {
            return null;
        }
        final LineString lineFromChosenRoute = (LineString) featureFromChosenRoute.getDefaultGeometry();
        return this.createSegments(lineFromChosenRoute, lineFromTrack);
    }
    
    private List<DeviationSegment> createSegments(final LineString lineX, final LineString lineY) {
        if (null == lineX || null == lineY) {
            return null;
        }
        LOGGER.info(MessageFormat.format("Amount of Coordinates in Line X %d and Line Y %d", lineX.getCoordinates().length, lineY.getCoordinates().length));
        final List<DeviationSegment> segments = new LinkedList<>();
        final CoordinateList segmentCoordinatesY = new CoordinateList();
        for(Coordinate curCoordinateY : lineY.getCoordinates()) {
            LOGGER.info(MessageFormat.format("Coordinate at Y %d , Y %d", curCoordinateY.x, curCoordinateY.y));
            if(segmentCoordinatesY.size() > 1) {
                final Coordinate previousCoordinateY = segmentCoordinatesY.getCoordinate(segmentCoordinatesY.size() - 1);
                final CoordinateList lineStringCoordinatesY = new CoordinateList();
                lineStringCoordinatesY.add(previousCoordinateY);
                lineStringCoordinatesY.add(curCoordinateY);
                final LineString curLineStringY = this.geometryFactory.createLineString(lineStringCoordinatesY.toCoordinateArray());
                if(lineX.crosses(curLineStringY) || lineX.touches(curLineStringY)) {
                    segmentCoordinatesY.add(curCoordinateY);
                    LOGGER.info(MessageFormat.format("Found crossing or touch on Line X after %d coordinates", segmentCoordinatesY.size()));  
                    final CoordinateList startCoordinateListY = new CoordinateList();
                    startCoordinateListY.add(segmentCoordinatesY.getCoordinate(0));
                    startCoordinateListY.add(segmentCoordinatesY.getCoordinate(1));
                    final LineString startLineStringY = this.geometryFactory.createLineString(startCoordinateListY.toCoordinateArray());
                    final CoordinateList coordinatesX = new CoordinateList(lineX.getCoordinates());
                    final CoordinateList segmentCoordinatesX = DeviationUtil.cutSegment(coordinatesX, startLineStringY, curLineStringY);
                    if (segmentCoordinatesX.size() > FILTER_SIZE && segmentCoordinatesY.size() > FILTER_SIZE) {
                        final LineString segmentLineX = this.geometryFactory.createLineString(segmentCoordinatesX.toCoordinateArray());
                        final LineString segmentLineY = this.geometryFactory.createLineString(segmentCoordinatesY.toCoordinateArray());
                        segments.add(new DeviationSegment(segmentLineX, segmentLineY));
                    }
                    segmentCoordinatesY.clear();
                }
            }
            segmentCoordinatesY.add(curCoordinateY);
        }
        LOGGER.info(MessageFormat.format("Found %d intersections", segments.size()));
        return segments;
    }
    
    public double calculateTotalDeviationArea(List<DeviationSegment> segments) {
        if (null != segments) {
            double area = 0d;
            for (final DeviationSegment curDeviationSegment : segments) {
                try
                {
                    final Polygon polygon = this.geometryFactory.createPolygon(DeviationUtil.createRingAsArrayFromSegment(curDeviationSegment));
                    // sum up areas
                    if (null != polygon) {
                        area += polygon.getArea();
                    }
                }
                catch (Exception ex)
                {
                    LOGGER.log(Level.SEVERE, "Could not create polygon '{'0'}'", ex.getLocalizedMessage());
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
