package com.vgilab.alternativ.business.geo;

import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Route;
import com.vgilab.alternativ.generated.Step;
import com.vgilab.alternativ.generated.Track;
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
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author smuellner
 */
@Service
public class SpatialAnalysisService {

    @Autowired
    private FeatureService featureService;

    public List<AnalysedTrip> analyseRoutes(final List<AlterNativ> alterNativs, final Double deviationInMeters) {
        final Double deviation = null == deviationInMeters ? 20d : deviationInMeters;
        final List<AnalysedTrip> trips = new LinkedList<>();
        if (null != alterNativs) {
            for (final AlterNativ curAlterNativ : alterNativs) {
                final List<Position> positions = new LinkedList<>();
                // Create Feature Maps
                final Map<Point, Track> pointTrackMap = this.featureService.createPointTrackMapFromTracks(curAlterNativ.getTracks(), curAlterNativ.getId());
                final Map<Route, Map<Step, List<Point>>> routeStepPointMap = new HashMap<>();
                final Map<Route, LineString> stepLineStringMap = new HashMap<>();
                for (final ChosenRoute curChosenRoute : curAlterNativ.getChosenRoute()) {
                    routeStepPointMap.putAll(this.featureService.createRouteStepPointMapFromChosenRoute(curChosenRoute, curAlterNativ.getId()));
                    stepLineStringMap.putAll(this.featureService.createRouteLineStringMapFromChosenRoute(curChosenRoute, curAlterNativ.getId()));
                }
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
                                final GeodeticCalculator gc = new GeodeticCalculator(DefaultGeographicCRS.WGS84);
                                try {
                                    gc.setStartingPosition(JTS.toDirectPosition(curPointForTrack.getKey().getCoordinate(), DefaultGeographicCRS.WGS84));
                                    gc.setDestinationPosition(JTS.toDirectPosition(curPointForStep.getCoordinate(), DefaultGeographicCRS.WGS84));
                                    final double orthodromicDistance = gc.getOrthodromicDistance();
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
                trips.add(new AnalysedTrip(curAlterNativ, positions));
            }
        }
        return trips;
    }
}
