package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.business.geo.AlterNativUtil;
import com.vgilab.alternativ.business.geo.AnalysedTrip;
import com.vgilab.alternativ.business.geo.Coordinate3D;
import com.vgilab.alternativ.business.geo.FeatureService;
import com.vgilab.alternativ.business.geo.Position;
import com.vgilab.alternativ.business.geo.ShapefileService;
import com.vgilab.alternativ.business.geo.SpatialAnalysisService;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Destination;
import com.vgilab.alternativ.generated.Location;
import com.vgilab.alternativ.generated.Origin;
import com.vgilab.alternativ.generated.Track;
import com.vgilab.alternativ.google.GoogleMapsRoadsApi;
import com.vividsolutions.jts.geom.Coordinate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.Visibility;
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
@ManagedBean(name = "positionListView")
@SessionScoped
public class PositionListView {

    private List<AlterNativ> alterNativs;
    private List<AnalysedTrip> trips;
    private String deviation;
    private MapModel mapModel;
    private MapModel routeMapModel;
    private Position selectedPosition;
    private AnalysedTrip selectedTrip;
    private StreamedContent shapefile;
    private String errorMessage;
    private Integer minimumTracks = 10;
    
    @Autowired
    private SpatialAnalysisService spatialAnalysisService;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private ShapefileService shapefileService;

    @PostConstruct
    public void init() {
        this.mapModel = new DefaultMapModel();
    }

    /**
     * @param alterNativs the alterNativs to set
     */
    public void setAlterNativs(final List<AlterNativ> alterNativs) {
        this.alterNativs = alterNativs;
        this.trips = this.spatialAnalysisService.analyseRoutes(this.alterNativs, 20d, this.minimumTracks);
    }

    /**
     * @return the trips
     */
    public List<AnalysedTrip> getTrips() {
        return trips;
    }

    /**
     * @param analysedTrip
     * @return the postions
     */
    public List<Position> getPositions(AnalysedTrip analysedTrip) {
        return analysedTrip.getPositions();
    }

    /**
     * @return the deviation
     */
    public String getDeviation() {
        return this.deviation;
    }

    /**
     * @param deviation the deviation to set
     */
    public void setDeviation(final String deviation) {
        this.deviation = deviation;
        if (StringUtils.isNotEmpty(deviation)) {
            try {
                final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setDecimalSeparator('.');
                final DecimalFormat decimalFormat = new DecimalFormat("###0.0#", symbols);
                decimalFormat.setParseBigDecimal(true);
                final BigDecimal deviationNumber = (BigDecimal) decimalFormat.parse(deviation);
                this.trips = this.spatialAnalysisService.analyseRoutes(this.alterNativs, deviationNumber.doubleValue(), this.minimumTracks);
            } catch (final ParseException pex) {
                FacesMessage message = new FacesMessage("Parse Error", pex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    /**
     * @return the minimumTracks
     */
    public Integer getMinimumTracks() {
        return minimumTracks;
    }

    /**
     * @param minimumTracks the minimumTracks to set
     */
    public void setMinimumTracks(Integer minimumTracks) {
        this.minimumTracks = minimumTracks;
        this.trips = this.spatialAnalysisService.analyseRoutes(this.alterNativs, 20d, this.minimumTracks);
    }

    public void onRowToggle(ToggleEvent event) {
        if (event.getVisibility() == Visibility.VISIBLE) {
            if (event.getData() instanceof Position) {
                this.mapModel = new DefaultMapModel();
                this.selectedPosition = (Position) event.getData();
                if (null != this.selectedPosition.getCoordinateForTrack()) {
                    final Coordinate3D coordinate = this.selectedPosition.getCoordinateForTrack();
                    final LatLng latLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                    this.mapModel.addOverlay(new Marker(latLng, "Track", ""));
                }
                if (null != this.selectedPosition.getCoordinateForStep()) {
                    final Coordinate3D coordinate = this.selectedPosition.getCoordinateForStep();
                    final LatLng latLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                    this.mapModel.addOverlay(new Marker(latLng, "Step", ""));
                }
            }
        }
    }

    public MapModel getMapModel() {
        return this.mapModel;
    }

    public Integer getTrackPointCountForTrip(final AnalysedTrip trip) {
        return this.featureService.getPointCountForTracks(trip.getAlterNativ().getTracks());
    }

    public Integer getStepPointCountForTrip(final AnalysedTrip trip) {
        return this.featureService.getPointCountForChosenRoutes(trip.getAlterNativ().getChosenRoute());
    }

    public void showTrip(final ActionEvent actionEvent) {
        final AnalysedTrip analysedTrip = (AnalysedTrip) actionEvent.getComponent().getAttributes().get("trip");
        final PositionDetailView positionDetailView = (PositionDetailView) FacesContext.getCurrentInstance().getApplication().getELResolver().getValue(FacesContext.getCurrentInstance().getELContext(), null, "positionDetailView");
        positionDetailView.setSelectedTrip(analysedTrip);
        final ConfigurableNavigationHandler configurableNavigationHandler = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
        configurableNavigationHandler.performNavigation("/positionDetail.xhtml?faces-redirect=true");
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public MapModel getRouteMapModelForTrip(final AnalysedTrip trip) {
        if (this.selectedTrip != trip) {
            this.selectedTrip = trip;
            this.routeMapModel = new DefaultMapModel();
            final AlterNativ alterNativ = trip.getAlterNativ();
            final Origin origin = alterNativ.getOrigin();
            final LatLng originLatLng = new LatLng(origin.getLat(), origin.getLng());
            final Marker originMarker = new Marker(originLatLng, "UID: " + alterNativ.getId(), "Origin: " + origin.getAddress(), "resources/images/startmarker.png");
            this.routeMapModel.addOverlay(originMarker);
            // Chosen Routes
            final Polyline choosenRoutePolyline = new org.primefaces.model.map.Polyline();
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
            final Destination destination = alterNativ.getDestination();
            final LatLng destinationLatLng = new LatLng(destination.getLat(), destination.getLng());
            final Marker destinationMarker = new Marker(destinationLatLng, "UID: " + alterNativ.getId(), "Destination: " + destination.getAddress(), "resources/images/endmarker.png");
            this.routeMapModel.addOverlay(destinationMarker);
            // user tracks
            final Polyline trackPolyline = new org.primefaces.model.map.Polyline();
            trackPolyline.setStrokeWeight(2);
            trackPolyline.setStrokeColor("red");
            trackPolyline.setStrokeOpacity(0.7);
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
            this.routeMapModel.addOverlay(trackPolyline);
            // create a list of coordinates for the google maps roads api
            try {
                if(!CollectionUtils.isEmpty(trip.getSnapedToRoad())) {
                    final List<Coordinate3D> snapedToRoad = trip.getSnapedToRoad();
                    final Polyline googleMapsTrackPolyline = new org.primefaces.model.map.Polyline();
                    googleMapsTrackPolyline.setStrokeWeight(2);
                    googleMapsTrackPolyline.setStrokeColor("blue");
                    googleMapsTrackPolyline.setStrokeOpacity(0.7);
                    for (final Coordinate3D curCoordinate : snapedToRoad) {
                        final LatLng latLng = new LatLng(curCoordinate.getLatitude(), curCoordinate.getLongitude());
                        googleMapsTrackPolyline.getPaths().add(latLng);
                    }
                    this.routeMapModel.addOverlay(googleMapsTrackPolyline);
                }
            } catch (final SecurityException ex) {
                Logger.getLogger(PositionListView.class
                        .getName()).log(Level.SEVERE, null, ex);
                FacesMessage message = new FacesMessage("Security Error", ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, message);
                this.errorMessage = ex.getLocalizedMessage();
            }
        }
        return this.routeMapModel;
    }

    public StreamedContent exportTripToShapefile(final AnalysedTrip trip) {
        try {
            final AlterNativ alterNativ = trip.getAlterNativ();
            final List<Coordinate3D> snapedToRoad = trip.getSnapedToRoad();
            final File zippedShapefiles = this.shapefileService.exportToShapefile(alterNativ, snapedToRoad);
            this.shapefile = new DefaultStreamedContent(new FileInputStream(zippedShapefiles), "application/zip", alterNativ.getId() + "-shp.zip");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PositionListView.class
                    .getName()).log(Level.SEVERE, null, ex);
            FacesMessage message = new FacesMessage("Failed", "Please import first data.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            this.errorMessage = ex.getLocalizedMessage();
        }
        return this.shapefile;
    }
}