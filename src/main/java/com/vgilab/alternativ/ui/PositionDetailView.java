package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.business.geo.AlterNativUtil;
import com.vgilab.alternativ.business.geo.AnalysedTrip;
import com.vgilab.alternativ.business.geo.Coordinate3D;
import com.vgilab.alternativ.business.geo.Coordinate3DUtil;
import com.vgilab.alternativ.business.geo.DeviationUtil;
import com.vgilab.alternativ.business.geo.FeatureService;
import com.vgilab.alternativ.business.geo.Position;
import com.vgilab.alternativ.business.geo.ShapefileService;
import com.vgilab.alternativ.business.geo.SubTrajectory;
import com.vgilab.alternativ.business.geo.TravelMode;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.Destination;
import com.vgilab.alternativ.generated.Origin;
import com.vgilab.alternativ.google.GoogleMapsRoadsApi;
import com.vividsolutions.jts.geom.Coordinate;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polygon;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author smuellner
 */
@Component
@ManagedBean(name = "positionDetailView")
@SessionScoped
public class PositionDetailView {

    private String coordinateReferenceSystem = "EPSG:2039";
    private AnalysedTrip selectedTrip;
    private MapModel routeMapModel;
    private MapModel importedMapModel = new DefaultMapModel();
    private List<SubTrajectory> importedCoordinates;
    private VisibleImportedRoute visibleImportedRoute = VisibleImportedRoute.ANALYSED_DATA;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> importedFeatures;
    private CoordinateReferenceSystem projectedCoordinateReferenceSystem;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private ShapefileService shapefileService;

    @PostConstruct
    public void init() {
    }

    public Integer getTrackPointCountForTrip() {
        if (null != this.selectedTrip && null != this.selectedTrip.getAlterNativ()) {
            return this.featureService.getPointCountForTracks(this.selectedTrip.getAlterNativ().getTracks());
        }
        return 0;
    }

    public Integer getStepPointCountForTrip() {
        if (null != this.selectedTrip && null != this.selectedTrip.getAlterNativ()) {
            return this.featureService.getPointCountForChosenRoutes(this.selectedTrip.getAlterNativ().getChosenRoute());
        }
        return 0;
    }

    public MapModel getRouteMapModelForTrip() {
        this.routeMapModel = new DefaultMapModel();
        if (null != this.selectedTrip && null != this.selectedTrip.getAlterNativ()) {
            final AlterNativ alterNativ = this.selectedTrip.getAlterNativ();
            // draw deviations
            if (null != this.selectedTrip.getDeviationsFromTrip()) {
                this.selectedTrip.getDeviationsFromTrip().stream().forEach(deviationSegment -> {
                    final List<Coordinate> ring = DeviationUtil.createRingAsListFromSegment(deviationSegment);
                    final List<LatLng> latLngs = Coordinate3DUtil.convertToLatLngs(ring);
                    final Polygon deviationPolygon = new Polygon();
                    deviationPolygon.setStrokeWeight(1);
                    deviationPolygon.setStrokeColor("yellow");
                    deviationPolygon.setStrokeOpacity(0.2);
                    deviationPolygon.setFillColor("yellow");
                    deviationPolygon.setFillOpacity(0.1);
                    ring.stream().map((coordinate) -> new LatLng(coordinate.y, coordinate.x)).forEach((latLng) -> {
                        deviationPolygon.getPaths().add(latLng);
                    });
                    this.routeMapModel.addOverlay(deviationPolygon);
                });
            }
            // Chosen Routes
            final Polyline choosenRoutePolyline = new Polyline();
            choosenRoutePolyline.setStrokeWeight(2);
            choosenRoutePolyline.setStrokeColor("green");
            choosenRoutePolyline.setStrokeOpacity(0.7);
            alterNativ.getChosenRoute().stream().forEach((curChosenRoute) -> {
                this.featureService.getCoordinatesFromChosenRoute(curChosenRoute).stream().map((curCoordinate) -> new LatLng(curCoordinate.y, curCoordinate.x)).forEach((latLng) -> {
                    choosenRoutePolyline.getPaths().add(latLng);
                });
            });
            this.routeMapModel.addOverlay(choosenRoutePolyline);
            // user tracks
            final Origin origin = alterNativ.getOrigin();
            final LatLng originLatLng = new LatLng(origin.getLat(), origin.getLng());
            final Marker originMarker = new Marker(originLatLng, "UID: " + alterNativ.getId(), "Origin: " + origin.getAddress(), "resources/images/startmarker.png");
            this.routeMapModel.addOverlay(originMarker);
            final Polyline trackPolyline = new Polyline();
            trackPolyline.setStrokeWeight(2);
            trackPolyline.setStrokeColor("red");
            trackPolyline.setStrokeOpacity(0.7);
            // Tracks
            alterNativ.getTracks().stream().map((curTrack) -> curTrack.getLocation()).forEach((location) -> {
                final LatLng latLng = new LatLng(location.getCoords().getLatitude(), location.getCoords().getLongitude());
                trackPolyline.getPaths().add(latLng);
                final StringBuilder message = new StringBuilder();
                if (null != location.getActivity()) {
                    message.append("Activity: ").append(location.getActivity().getType()).append(" | ");
                }
                if (null != location.getTimestamp()) {
                    message.append("Timestamp: ").append(location.getTimestamp());
                }
            });
            final Destination destination = alterNativ.getDestination();
            final LatLng destinationLatLng = new LatLng(destination.getLat(), destination.getLng());
            final Marker destinationMarker = new Marker(destinationLatLng, "UID: " + alterNativ.getId(), "Destination: " + destination.getAddress(), "resources/images/endmarker.png");
            this.routeMapModel.addOverlay(destinationMarker);
            this.routeMapModel.addOverlay(trackPolyline);
            try {
                final Polyline googleMapsTrackPolyline = new Polyline();
                googleMapsTrackPolyline.setStrokeWeight(2);
                googleMapsTrackPolyline.setStrokeColor("blue");
                googleMapsTrackPolyline.setStrokeOpacity(0.7);
                final List<Coordinate3D> coordinates = AlterNativUtil.getCoordinatesFromTrack(alterNativ);
                final List<Coordinate3D> snapedToRoad = this.selectedTrip.getSnapedToRoad() != null ? this.selectedTrip.getSnapedToRoad() : GoogleMapsRoadsApi.snapToRoadsUsingBatches(coordinates, true);
                snapedToRoad.stream().map((curCoordinate) -> new LatLng(curCoordinate.getLatitude(), curCoordinate.getLongitude())).forEach((latLng) -> {
                    googleMapsTrackPolyline.getPaths().add(latLng);
                });
                this.routeMapModel.addOverlay(googleMapsTrackPolyline);
            } catch (final SecurityException ex) {
                Logger.getLogger(PositionListView.class.getName()).log(Level.SEVERE, null, ex);
                FacesMessage message = new FacesMessage("Security Error", ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
        return this.routeMapModel;
    }

    public MapModel getImportedMapModel() {
        this.importedMapModel = new DefaultMapModel();
        switch (this.visibleImportedRoute) {
            case ALL_ROUTES:
                this.importedCoordinates = this.shapefileService.getCoordinatesFromFeatureCollection(this.projectedCoordinateReferenceSystem, null, this.importedFeatures);
                break;
            case THIS_ROUTE:
                final String referenceId = null != this.selectedTrip && null != this.selectedTrip.getAlterNativ() ? this.selectedTrip.getAlterNativ().getId() : null;
                this.importedCoordinates = this.shapefileService.getCoordinatesFromFeatureCollection(this.projectedCoordinateReferenceSystem, referenceId, this.importedFeatures);
                break;
            case ANALYSED_DATA:
                this.importedCoordinates = null;
        }
        if (!CollectionUtils.isEmpty(this.importedCoordinates)) {
            this.importedCoordinates.stream().map((SubTrajectory curSubTrajectory) -> {
                final Polyline polyline = new Polyline();
                polyline.setStrokeWeight(1);
                polyline.setStrokeColor(HtmlUtil.getColorFromTravelMode(curSubTrajectory.getTravelMode()));
                curSubTrajectory.getCoordinates().stream().map((curCoordinate3D) -> new LatLng(curCoordinate3D.getLatitude(), curCoordinate3D.getLongitude())).forEach((latLng) -> {
                    polyline.getPaths().add(latLng);
                });
                return polyline;
            }).forEach((polyline) -> {
                this.importedMapModel.addOverlay(polyline);
            });
            if (null != this.selectedTrip && null != this.selectedTrip.getAlterNativ()) {
                final AlterNativ alterNativ = this.selectedTrip.getAlterNativ();
                final Origin origin = alterNativ.getOrigin();
                final LatLng originLatLng = new LatLng(origin.getLat(), origin.getLng());
                final Marker originMarker = new Marker(originLatLng, "UID: " + alterNativ.getId(), "Origin: " + origin.getAddress(), "resources/images/startmarker.png");
                this.importedMapModel.addOverlay(originMarker);
                final Destination destination = alterNativ.getDestination();
                final LatLng destinationLatLng = new LatLng(destination.getLat(), destination.getLng());
                final Marker destinationMarker = new Marker(destinationLatLng, "UID: " + alterNativ.getId(), "Destination: " + destination.getAddress(), "resources/images/endmarker.png");
                this.importedMapModel.addOverlay(destinationMarker);
            }

        }
        return this.importedMapModel;
    }

    /**
     * @return the selectedTrip
     */
    public AnalysedTrip getSelectedTrip() {
        return selectedTrip;
    }

    /**
     * @param selectedTrip the selectedTrip to set
     */
    public void setSelectedTrip(AnalysedTrip selectedTrip) {
        this.selectedTrip = selectedTrip;
    }

    /**
     * @return the coordinateReferenceSystem
     */
    public String getCoordinateReferenceSystem() {
        return coordinateReferenceSystem;
    }

    /**
     * @param coordinateReferenceSystem the coordinateReferenceSystem to set
     */
    public void setCoordinateReferenceSystem(String coordinateReferenceSystem) {
        this.coordinateReferenceSystem = coordinateReferenceSystem;
    }

    /**
     * @return the postions
     */
    public List<Position> getPositions() {
        return this.selectedTrip.getPositions();
    }

    /**
     * @return the visibleImportedRoute
     */
    public VisibleImportedRoute getVisibleImportedRoute() {
        return visibleImportedRoute;
    }

    /**
     * @param visibleImportedRoute the visibleImportedRoute to set
     */
    public void setVisibleImportedRoute(VisibleImportedRoute visibleImportedRoute) {
        this.visibleImportedRoute = visibleImportedRoute;
    }

    public VisibleImportedRoute[] getVisibleImportedRouteValues() {
        return VisibleImportedRoute.values();
    }

    /**
     * @return the travelModes
     */
    public TravelMode[] getTravelModes() {
        return TravelMode.values();
    }

    /**
     * get Color from travelmode
     *
     * @param travelMode
     * @return
     */
    public String getColorFromTravelMode(final TravelMode travelMode) {
        return HtmlUtil.getColorFromTravelMode(travelMode);
    }

    public void handleFileUpload(FileUploadEvent event) {
        event.getComponent().setTransient(false);
        if (event.getFile() != null && event.getFile().getSize() > 0) {
            final byte[] contents = event.getFile().getContents();
            try {
                this.importedFeatures = this.shapefileService.importFeaturesFromArchive(contents);
                this.setVisibleImportedRoute(VisibleImportedRoute.THIS_ROUTE);
                final FacesMessage message = new FacesMessage("Successful", "Imported features from " + event.getFile().getFileName() + ".");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (FactoryException | IOException ex) {
                Logger.getLogger(PositionDetailView.class.getName()).log(Level.SEVERE, null, ex);
                final FacesMessage message = new FacesMessage("Error", "Could not read features from " + event.getFile().getFileName() + ".");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
            try {
                this.projectedCoordinateReferenceSystem = this.shapefileService.importCRSFromArchive(this.coordinateReferenceSystem, contents);
                final FacesMessage message = new FacesMessage("Successful", "Imported CRS from " + event.getFile().getFileName() + ".");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (FactoryException | IOException ex) {
                Logger.getLogger(PositionDetailView.class.getName()).log(Level.SEVERE, null, ex);
                final FacesMessage message = new FacesMessage("Error", "Could not read CRS from " + event.getFile().getFileName() + ".");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
}
