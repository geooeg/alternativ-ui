package com.vgilab.alternativ.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgilab.alternativ.business.busstop.BusStop;
import com.vgilab.alternativ.business.busstop.BusStopParser;
import com.vgilab.alternativ.business.geo.ShapefileService;
import com.vgilab.alternativ.business.telofun.TelofunParser;
import com.vgilab.alternativ.generated.AlterNativ;
import com.vgilab.alternativ.generated.ChosenRoute;
import com.vgilab.alternativ.generated.Destination;
import com.vgilab.alternativ.generated.Feature;
import com.vgilab.alternativ.generated.Location;
import com.vgilab.alternativ.generated.Origin;
import com.vgilab.alternativ.generated.Route;
import com.vgilab.alternativ.generated.Telofun;
import com.vgilab.alternativ.generated.Track;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Setup of markers
 * http://www.primefaces.org/showcase/ui/data/gmap/markers.xhtml
 * http://www.primefaces.org/showcase/ui/data/dataList.xhtml
 *
 * @author ljzhang
 */
@Component
@ManagedBean(name = "indexView")
@SessionScoped
public class IndexView implements Serializable {

    private final List<AlterNativ> alterNativs = new LinkedList<>();

    private List<BusStop> busStops;
    private List<Feature> telofuns;

    private AlterNativ selectedAlterNativ;

    private MapModel mapModel;

    private Marker marker;

    private StreamedContent shapefile;

    @Autowired
    private ShapefileService shapefileService;

    @PostConstruct
    public void init() {
        final List<AlterNativ> alterNativApi = AlterNativApi.getAll();
        if (null != alterNativApi) {
            this.alterNativs.addAll(AlterNativApi.getAll());
            this.updateMapModel();
        }
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        marker = (Marker) event.getOverlay();
    }

    private void updateMapModel() {
        this.mapModel = new DefaultMapModel();
        final List<AlterNativ> renderedAlterNativs = (null == this.selectedAlterNativ) ? this.getAlterNativs() : Collections.singletonList(this.selectedAlterNativ);
        if (null != renderedAlterNativs) {
            for (AlterNativ curAlterNativ : renderedAlterNativs) {
                // Draw polyline for Chosen Routes 
                final Origin origin = curAlterNativ.getOrigin();
                final LatLng originLatLng = new LatLng(origin.getLat(), origin.getLng());
                final Marker originMarker = new Marker(originLatLng, "UID: " + curAlterNativ.getId(), "Origin: " + origin.getAddress(), "resources/images/startmarker.png");
                originMarker.setFlat(true);
                this.mapModel.addOverlay(originMarker);
                final Polyline polylineChosenRoute = new Polyline();
                polylineChosenRoute.getPaths().add(originLatLng);
                for (ChosenRoute curChosenRoute : curAlterNativ.getChosenRoute()) {
                    for (Route curRoute : curChosenRoute.getRoutes()) {
                        // TODO: curRoute.
                    }
                }
                final Destination destination = curAlterNativ.getDestination();
                final LatLng destinationLatLng = new LatLng(destination.getLat(), destination.getLng());
                final Marker destinationMarker = new Marker(destinationLatLng, "UID: " + curAlterNativ.getId(), "Destination: " + destination.getAddress(), "resources/images/endmarker.png");
                this.mapModel.addOverlay(destinationMarker);
                polylineChosenRoute.getPaths().add(destinationLatLng);
                // Draw polyline for tracks
                final Polyline polyline = new Polyline();
                polyline.setStrokeWeight(2);
                polyline.setStrokeColor(HtmlUtil.getRandomHTMLColor());
                polyline.setStrokeOpacity(0.7);
                // Tracks
                for (Track curTrack : curAlterNativ.getTracks()) {
                    final Location location = curTrack.getLocation();
                    final LatLng latLng = new LatLng(location.getCoords().getLatitude(), location.getCoords().getLongitude());
                    polyline.getPaths().add(latLng);
                    final StringBuilder message = new StringBuilder();
                    if (null != location.getActivity()) {
                        message.append("Activity: ").append(location.getActivity().getType()).append(" | ");
                    }
                    if (null != location.getTimestamp()) {
                        message.append("Timestamp: ").append(location.getTimestamp());
                    }
                    this.mapModel.addOverlay(new Marker(latLng, "UID: " + curTrack.getId(), message));
                }

                this.mapModel.addOverlay(polyline);
            }
        }
        // render bustops
        if (this.busStops != null) {
            for (final BusStop curBusStop : this.getBusStops()) {
                this.mapModel.addOverlay(new Marker(curBusStop.getLatLng(), "Bus Stop:" + curBusStop.getName() + " | Code:" + curBusStop.getCode()));
            }
        }
        // render telofuns
        if (this.telofuns != null) {
            for (final Feature curFeature : this.telofuns) {
                final LatLng latLng = new LatLng(curFeature.getAttributes().getLat(), curFeature.getAttributes().getLon());
                this.mapModel.addOverlay(new Marker(latLng, "Telofun: " + curFeature.getAttributes().getShemTachana()));
            }
        }
    }

    public void clearList() {
        this.alterNativs.clear();
    }

    public void handleTenCityFileUpload(final FileUploadEvent event) {
        event.getComponent().setTransient(false);
        if (event.getFile() != null && event.getFile().getSize() > 0) {
            final FacesMessage message = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            final String content = new String(event.getFile().getContents());
            final ObjectMapper objectMapper = new ObjectMapper();
            try {
                final List<AlterNativ> readValue = objectMapper.readValue(content, new TypeReference<List<AlterNativ>>() {
                });
                this.alterNativs.addAll(readValue);
            } catch (IOException ex) {
                Logger.getLogger(IndexView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void handleBusstopFileUpload(final FileUploadEvent event) {
        event.getComponent().setTransient(false);
        if (event.getFile() != null && event.getFile().getSize() > 0) {
            FacesMessage message = new FacesMessage("Successful", event.getFile().getFileName() + " bus stops are uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            try {
                this.setBusStops(BusStopParser.getBusStops(event.getFile().getInputstream()));
                this.updateMapModel();
            } catch (IOException ex) {
                Logger.getLogger(IndexView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void handleTelofunFileUpload(final FileUploadEvent event) {
        event.getComponent().setTransient(false);
        if (event.getFile() != null && event.getFile().getSize() > 0) {
            FacesMessage message = new FacesMessage("Successful", event.getFile().getFileName() + " telofun stops are uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            try {
                final Telofun telofun = TelofunParser.getTelofun(event.getFile().getContents());
                this.telofuns = telofun.getFeatures();
                this.updateMapModel();
            } catch (IOException ex) {
                Logger.getLogger(IndexView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void startAnalysis() {
        final PositionListView positionListView = (PositionListView) FacesContext.getCurrentInstance().getApplication().getELResolver().getValue(FacesContext.getCurrentInstance().getELContext(), null, "positionListView");
        positionListView.setAlterNativs(this.alterNativs);
        final ConfigurableNavigationHandler configurableNavigationHandler = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
        configurableNavigationHandler.performNavigation("/positionList.xhtml?faces-redirect=true");
    }
    
    public void startReport() {
        final ReportView reportView = (ReportView) FacesContext.getCurrentInstance().getApplication().getELResolver().getValue(FacesContext.getCurrentInstance().getELContext(), null, "exportView");
        reportView.setAlterNativs(this.alterNativs);
        final ConfigurableNavigationHandler configurableNavigationHandler = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
        configurableNavigationHandler.performNavigation("/report.xhtml?faces-redirect=true");
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    public Marker getMarker() {
        return marker;
    }

    /**
     * @return the alterNativs
     */
    public List<AlterNativ> getAlterNativs() {
        return alterNativs;
    }

    /**
     * @return the selectedAlterNativ
     */
    public AlterNativ getSelectedAlterNativ() {
        return selectedAlterNativ;
    }

    /**
     * @param selectedAlterNativ the selectedAlterNativ to set
     */
    public void setSelectedAlterNativ(AlterNativ selectedAlterNativ) {
        this.selectedAlterNativ = selectedAlterNativ;
        this.updateMapModel();
    }

    /**
     * @return the busStops
     */
    public List<BusStop> getBusStops() {
        return busStops;
    }

    /**
     * @param busStops the busStops to set
     */
    public void setBusStops(List<BusStop> busStops) {
        this.busStops = busStops;
    }

    /**
     * @return the telofuns
     */
    public List<Feature> getTelofuns() {
        return telofuns;
    }

    /**
     * @param telofuns the telofuns to set
     */
    public void setTelofuns(List<Feature> telofuns) {
        this.telofuns = telofuns;
    }

    public StreamedContent getShapefile(boolean snapToRoad) {
        if (null != this.alterNativs || null != this.busStops || null != this.telofuns) {
            try {
                this.shapefile = new DefaultStreamedContent(new FileInputStream(this.shapefileService.exportToShapefile(this.alterNativs, this.busStops, this.telofuns, snapToRoad)), "application/zip", "alternativ-shp.zip");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(IndexView.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            FacesMessage message = new FacesMessage("Failed", "Please import first data.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
        return this.shapefile;
    }
   
}
