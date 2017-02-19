package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.Route;
import com.vgilab.alternativ.generated.Step;
import com.vgilab.alternativ.generated.Track;
import com.vgilab.alternativ.google.GoogleMapsRoadsApi;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author smuellner
 */
@Service
public class SpatialAnalysisService {

    @Autowired
    private FeatureService featureService;

    @Autowired
    private DeviationAnalysisService deviationAnalysisService;

    public List<AnalysedTrip> analyseRoutes(final List<AlterNativ> alterNativs, final Double deviationInMeters, final Integer minimumTracks) {
        final List<AnalysedTrip> trips = new LinkedList<>();
        if (null != alterNativs) {
            alterNativs.stream().forEach((AlterNativ curAlterNativ) -> {
                try {
                    trips.add(this.analyseRoute(curAlterNativ, deviationInMeters, minimumTracks));
                } catch (final SecurityException ex) {
                    Logger.getLogger(SpatialAnalysisService.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
        return trips;
    }

    public AnalysedTrip analyseRoute(final AlterNativ curAlterNativ, final Double deviationInMeters, final Integer minimumTracks) {
        final Double deviation = null == deviationInMeters ? 20d : deviationInMeters;
        final AnalysedTrip analysedTrip = new AnalysedTrip(curAlterNativ, deviation, minimumTracks); 
        
        if(CollectionUtils.isEmpty(analysedTrip.getSnapedToRoad())) {
            final List<Coordinate3D> snapedToRoad = GoogleMapsRoadsApi.snapToRoadsUsingBatches(AlterNativUtil.getCoordinatesFromTrack(curAlterNativ), true);
            analysedTrip.setSnapedToRoad(snapedToRoad);
        }
        final List<Coordinate> track = Coordinate3DUtil.convert(analysedTrip.getSnapedToRoad());
        // final List<DeviationSegment> deviationSegmentsWithTracks = deviationAnalysisService.createSegments(curAlterNativ);
        // final List<DeviationSegment> deviationSegmentsWithTracks = deviationAnalysisService.createSegments(curAlterNativ, track);
        // final List<DeviationSegment> filteredSegmentsByDeviation = deviationAnalysisService.filterSegmentsByDeviation(deviationSegmentsWithTracks, 2d);

        final List<DeviationSegment> filteredSegmentsByDeviation = deviationAnalysisService.createSegments(curAlterNativ, track);
        analysedTrip.setDeviationsFromTrip(filteredSegmentsByDeviation);
        analysedTrip.setDeviationArea(deviationAnalysisService.calculateTotalDeviationArea(filteredSegmentsByDeviation));

        final List<Position> positions = this.matchPositions(curAlterNativ, deviationInMeters, minimumTracks);
        analysedTrip.setPositions(positions);
        return analysedTrip;
    }
    
    private List<Position> matchPositions(final AlterNativ curAlterNativ, final Double deviation, final Integer minimumTracks) {
        final List<Position> positions = new LinkedList<>();
        // Create Feature Maps
        final Map<Point, Track> pointTrackMap = this.featureService.createPointTrackMapFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId());
        final Map<Route, Map<Step, List<Point>>> routeStepPointMap = new HashMap<>();
        final Map<Route, LineString> stepLineStringMap = new HashMap<>();
        curAlterNativ.getChosenRoute().stream().map((curChosenRoute) -> {
            routeStepPointMap.putAll(this.featureService.createRouteStepPointMapFromChosenRoute(curChosenRoute, curAlterNativ.getId()));
            return curChosenRoute;
        }).forEach((curChosenRoute) -> {
            stepLineStringMap.putAll(this.featureService.createRouteLineStringMapFromChosenRoute(curChosenRoute, curAlterNativ.getId()));
        });
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        final Point origin = geometryFactory.createPoint(new Coordinate(curAlterNativ.getOrigin().getLng(), curAlterNativ.getOrigin().getLat()));
        final Point destination = geometryFactory.createPoint(new Coordinate(curAlterNativ.getDestination().getLng(), curAlterNativ.getDestination().getLat()));
        // get origin
        origin_loop:
        for (Map.Entry<Route, LineString> curStepLineString : stepLineStringMap.entrySet()) {
            final LineString curLineString = curStepLineString.getValue();
            if (curLineString.isWithinDistance(origin, 0.01)) {
                for (final Coordinate curCoordinate : curLineString.getCoordinates()) {
                    final Point point = geometryFactory.createPoint(curCoordinate);
                    if (point.isWithinDistance(origin, 0.01)) {
                        final Position position = new Position();
                        position.setRoute(curStepLineString.getKey());
                        position.setTrack(pointTrackMap.get(point));
                        position.setCoordinateForStep(new Coordinate3D(curCoordinate.x, curCoordinate.y, curCoordinate.z));
                        position.setCoordinateForTrack(new Coordinate3D(origin.getCoordinate().x, origin.getCoordinate().y, origin.getCoordinate().z));
                        positions.add(position);
                        break origin_loop;
                    }
                }
            }
        }
        final DistanceCalculation distanceCalculation = new DistanceCalculation();
        // map step to points 
        for (final Map.Entry<Route, Map<Step, List<Point>>> curRouteStepPoint : routeStepPointMap.entrySet()) {
            final Route route = curRouteStepPoint.getKey();
            final Map<Step, List<Point>> stepPointMap = curRouteStepPoint.getValue();
            for (final Map.Entry<Step, List<Point>> curStepPoint : stepPointMap.entrySet()) {
                final Step step = curStepPoint.getKey();
                for (final Point curPointForStep : curStepPoint.getValue()) {
                    final Position position = new Position();
                    position.setRoute(route);
                    position.setStep(step);
                    position.setCoordinateForStep(new Coordinate3D(curPointForStep.getCoordinate().x, curPointForStep.getCoordinate().y, curPointForStep.getCoordinate().z));
                    for (Map.Entry<Point, Track> curPointForTrack : pointTrackMap.entrySet()) {
                        final Coordinate coordinate = curPointForTrack.getKey().getCoordinate();
                        try {
                            final double orthodromicDistance = distanceCalculation.calculate(curPointForTrack.getKey().getCoordinate(), curPointForStep.getCoordinate(), DefaultGeographicCRS.WGS84);
                            if (orthodromicDistance < deviation) {
                                position.setTrack(curPointForTrack.getValue());
                                position.setDistance(orthodromicDistance);
                                position.setCoordinateForTrack(new Coordinate3D(coordinate.x, coordinate.y, coordinate.z));
                            }
                        } catch (TransformException ex) {
                            Logger.getLogger(SpatialAnalysisService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    positions.add(position);
                }
            }
        }
        // get destination
        destination_loop:
        for (Map.Entry<Route, LineString> curStepLineString : stepLineStringMap.entrySet()) {
            final LineString curLineString = curStepLineString.getValue();
            if (curLineString.isWithinDistance(destination, 0.01)) {
                for (final Coordinate curCoordinate : curLineString.getCoordinates()) {
                    final Point point = geometryFactory.createPoint(curCoordinate);
                    if (point.isWithinDistance(destination, 0.01)) {
                        final Position position = new Position();
                        position.setRoute(curStepLineString.getKey());
                        position.setTrack(pointTrackMap.get(point));
                        position.setCoordinateForStep(new Coordinate3D(curCoordinate.x, curCoordinate.y, curCoordinate.z));
                        position.setCoordinateForTrack(new Coordinate3D(destination.getCoordinate().x, destination.getCoordinate().y, destination.getCoordinate().z));
                        positions.add(position);
                        break destination_loop;
                    }
                }
            }
        }
        return positions;
    }
}
