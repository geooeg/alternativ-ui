package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.business.geo.AlterNativUtil;
import com.vgilab.alternativ.business.geo.AnalysedTrip;
import com.vgilab.alternativ.business.geo.Coordinate3D;
import com.vgilab.alternativ.business.geo.FeatureService;
import com.vgilab.alternativ.business.geo.Position;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Destination;
import com.vgilab.alternativ.generated.Location;
import com.vgilab.alternativ.generated.Origin;
import com.vgilab.alternativ.generated.Track;
import com.vgilab.alternativ.google.GoogleMapsRoadsApi;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 *
 * @author smuellner
 */
@Component
@ManagedBean(name = "positionDetailView")
@SessionScoped
public class PositionDetailView {

    private AnalysedTrip selectedTrip;
    private List<Coordinate3D> snapedToRoad;
    private MapModel routeMapModel;
    private String mapCenter;
    

    @Autowired
    private FeatureService featureService;

    @PostConstruct
    public void init() {
    }

    public String getMapCenter() {
        return StringUtils.isEmpty(this.mapCenter) ? "37.335556, -122.009167" : this.mapCenter;
    }

    public Integer getTrackPointCountForTrip() {
        return this.featureService.getPointCountForTracks(this.selectedTrip.getAlterNativ().getTracks());
    }

    public Integer getStepPointCountForTrip() {
        return this.featureService.getPointCountForChosenRoutes(this.selectedTrip.getAlterNativ().getChosenRoute());
    }
    
    public String printTrip() {
        final AlterNativ alterNativ = this.selectedTrip.getAlterNativ();
        final Origin origin = alterNativ.getOrigin();
        return null != origin ? origin.getLat() + ", " + origin.getLng() : "37.335556, -122.009167";
    }

    public String getRouteMapCenterForTrip() {
        final AlterNativ alterNativ = this.selectedTrip.getAlterNativ();
        final Origin origin = alterNativ.getOrigin();
        return null != origin ? origin.getLat() + ", " + origin.getLng() : "37.335556, -122.009167";
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
            // create a list of coordinates for the google maps roads api
            final List<Coordinate3D> coordinates = AlterNativUtil.getCoordinatesFromTrack(alterNativ);
            try {
                this.snapedToRoad = GoogleMapsRoadsApi.snapToRoadsUsingBatches(coordinates, true);
                final Polyline googleMapsTrackPolyline = new Polyline();
                googleMapsTrackPolyline.setStrokeWeight(2);
                googleMapsTrackPolyline.setStrokeColor("blue");
                googleMapsTrackPolyline.setStrokeOpacity(0.7);
                for (final Coordinate3D curCoordinate : this.snapedToRoad) {
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
     * @return the postions
     */
    public List<Position> getPositions() {
        return this.selectedTrip.getPositions();
    }

}
