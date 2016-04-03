package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.business.geo.AlterNativUtil;
import com.vgilab.alternativ.business.geo.AnalysedTrip;
import com.vgilab.alternativ.business.geo.Coordinate3D;
import com.vgilab.alternativ.business.geo.FeatureService;
import com.vgilab.alternativ.business.geo.Position;
import com.vgilab.alternativ.business.geo.ShapefileService;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Destination;
import com.vgilab.alternativ.generated.Location;
import com.vgilab.alternativ.generated.Origin;
import com.vgilab.alternativ.generated.Track;
import com.vgilab.alternativ.google.GoogleMapsRoadsApi;
import com.vividsolutions.jts.geom.Coordinate;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.opengis.referencing.FactoryException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
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
 
    private String coordinateReferenceSystem;
    private AnalysedTrip selectedTrip;
    private MapModel routeMapModel;
    private MapModel importedMapModel = new DefaultMapModel();
    private Map<String, List<Coordinate3D>> importedCoordinates;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private ShapefileService shapefileService;

    @PostConstruct
    public void init() {
    }

    public Integer getTrackPointCountForTrip() {
        return this.featureService.getPointCountForTracks(this.selectedTrip.getAlterNativ().getTracks());
    }

    public Integer getStepPointCountForTrip() {
        return this.featureService.getPointCountForChosenRoutes(this.selectedTrip.getAlterNativ().getChosenRoute());
    }

    public MapModel getRouteMapModelForTrip() {
        this.routeMapModel = new DefaultMapModel();
        final AlterNativ alterNativ = this.selectedTrip.getAlterNativ();
        // Chosen Routes
        final Polyline choosenRoutePolyline = new Polyline();
        choosenRoutePolyline.setStrokeWeight(2);
        choosenRoutePolyline.setStrokeColor("green");
        choosenRoutePolyline.setStrokeOpacity(0.7);
        for (final ChosenRoute curChosenRoute : alterNativ.getChosenRoute()) {
            for (final Coordinate curCoordinate : this.featureService.getCoordinatesFromChosenRoute(curChosenRoute)) {
                final LatLng latLng = new LatLng(curCoordinate.y, curCoordinate.x);
                choosenRoutePolyline.getPaths().add(latLng);
            }
        }
        this.routeMapModel.addOverlay(choosenRoutePolyline);
        // user tracks
        final Origin origin = alterNativ.getOrigin();
        final LatLng originLatLng = new LatLng(origin.getLat(), origin.getLng());
        this.routeMapModel.addOverlay(new Marker(originLatLng, "UID: " + alterNativ.getId(), "Origin: " + origin.getAddress()));
        final Polyline trackPolyline = new Polyline();
        trackPolyline.setStrokeWeight(2);
        trackPolyline.setStrokeColor("red");
        trackPolyline.setStrokeOpacity(0.7);
        trackPolyline.getPaths().add(originLatLng);
        // Tracks
        for (final Track curTrack : alterNativ.getTracks()) {
            final Location location = curTrack.getLocation();
            final LatLng latLng = new LatLng(location.getCoords().getLatitude(), location.getCoords().getLongitude());
            trackPolyline.getPaths().add(latLng);
            final StringBuilder message = new StringBuilder();
            if (null != location.getActivity()) {
                message.append("Activity: ").append(location.getActivity().getType()).append(" | ");
            }
            if (null != location.getTimestamp()) {
                message.append("Timestamp: ").append(location.getTimestamp());
            }
            // this.routeMapModel.addOverlay(new Marker(latLng, "UID: " + curTrack.getId(), message));
        }
        final Destination destination = alterNativ.getDestination();
        final LatLng destinationLatLng = new LatLng(destination.getLat(), destination.getLng());
        this.routeMapModel.addOverlay(new Marker(destinationLatLng, "UID: " + alterNativ.getId(), "Destination: " + destination.getAddress()));
        trackPolyline.getPaths().add(destinationLatLng);
        this.routeMapModel.addOverlay(trackPolyline);
        try {
            final Polyline googleMapsTrackPolyline = new Polyline();
            googleMapsTrackPolyline.setStrokeWeight(2);
            googleMapsTrackPolyline.setStrokeColor("blue");
            googleMapsTrackPolyline.setStrokeOpacity(0.7);
            final List<Coordinate3D> coordinates = AlterNativUtil.getCoordinatesFromTrack(alterNativ);
            final List<Coordinate3D> snapedToRoad = this.selectedTrip.getSnapedToRoad() != null ? this.selectedTrip.getSnapedToRoad() : GoogleMapsRoadsApi.snapToRoadsUsingBatches(coordinates, true);
            for (final Coordinate3D curCoordinate : snapedToRoad) {
                final LatLng latLng = new LatLng(curCoordinate.getLatitude(), curCoordinate.getLongitude());
                googleMapsTrackPolyline.getPaths().add(latLng);
            }
            this.routeMapModel.addOverlay(googleMapsTrackPolyline);
        } catch (final SecurityException ex) {
            Logger.getLogger(PositionListView.class.getName()).log(Level.SEVERE, null, ex);
            FacesMessage message = new FacesMessage("Security Error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
        return this.routeMapModel;
    }

    public MapModel getImportedMapModel() {
        this.importedMapModel = new DefaultMapModel();
        if (!CollectionUtils.isEmpty(this.importedCoordinates)) {
            for (Map.Entry<String, List<Coordinate3D>> curCoordinateSet : this.importedCoordinates.entrySet()) {
                final Polyline polyline = new Polyline();
                polyline.setStrokeWeight(1);
                polyline.setStrokeColor("green");
                for (final Coordinate3D curCoordinate3D : curCoordinateSet.getValue()) {
                    final LatLng latLng = new LatLng(curCoordinate3D.getLatitude(), curCoordinate3D.getLongitude());
                    polyline.getPaths().add(latLng);
                }
                this.importedMapModel.addOverlay(polyline);
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

    public void handleFileUpload(FileUploadEvent event) {
        event.getComponent().setTransient(false);
        if (event.getFile() != null && event.getFile().getSize() > 0) {
            try {
                final String referenceId = this.selectedTrip.getAlterNativ().getId();
                this.importedCoordinates = this.shapefileService.importCoordinatesFromArchive(this.coordinateReferenceSystem, referenceId, event.getFile().getContents());
                final FacesMessage message = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (FactoryException | IOException ex) {
                Logger.getLogger(PositionDetailView.class.getName()).log(Level.SEVERE, null, ex);
                final FacesMessage message = new FacesMessage("Error", ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
}
