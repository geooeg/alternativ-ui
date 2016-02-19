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
import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
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

    private List<AlterNativ> alterNativs;

    private List<BusStop> busStops;
    private List<Feature> telofuns;

    private AlterNativ selectedAlterNativ;

    private MapModel mapModel;

    private Marker marker;

    private UploadedFile file;

    private UploadedFile busStopFile;

    private UploadedFile telofunFile;

    private StreamedContent shapefile;

    @Autowired
    private ShapefileService shapefileService;

    @PostConstruct
    public void init() {
        this.alterNativs = AlterNativApi.getAll();
        if (null != this.alterNativs) {
            this.updateMapModel();
        }
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        marker = (Marker) event.getOverlay();
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

    private void updateMapModel() {
        this.mapModel = new DefaultMapModel();
        final List<AlterNativ> renderedAlterNativs = (null == this.selectedAlterNativ) ? this.getAlterNativs() : Collections.singletonList(this.selectedAlterNativ);
        if (null != renderedAlterNativs) {
            for (AlterNativ curAlterNativ : renderedAlterNativs) {
                final Origin origin = curAlterNativ.getOrigin();
                final LatLng originLatLng = new LatLng(origin.getLat(), origin.getLng());
                this.mapModel.addOverlay(new Marker(originLatLng, "UID: " + curAlterNativ.getId(), "Origin: " + origin.getAddress()));
                final Polyline polyline = new Polyline();
                polyline.setStrokeWeight(2);
                polyline.setStrokeColor(this.getRandomHTMLColor());
                polyline.setStrokeOpacity(0.7);
                polyline.getPaths().add(originLatLng);
                // Chosen Routes
                for (ChosenRoute curChosenRoute : curAlterNativ.getChosenRoute()) {
                    for (Route curRoute : curChosenRoute.getRoutes()) {
                        // TODO: curRoute.
                    }
                }
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
                final Destination destination = curAlterNativ.getDestination();
                final LatLng destinationLatLng = new LatLng(destination.getLat(), destination.getLng());
                this.mapModel.addOverlay(new Marker(destinationLatLng, "UID: " + curAlterNativ.getId(), "Destination: " + destination.getAddress()));
                polyline.getPaths().add(destinationLatLng);
                this.mapModel.addOverlay(polyline);
            }
        }
        // render bustops
        if (this.busStops != null) {
            for (final BusStop curBusStop : this.getBusStops()) {
                this.mapModel.addOverlay(new Marker(curBusStop.getLatLng(), "Bus Stop:" + curBusStop.getTitle()));
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

    private String getRandomHTMLColor() {
        final Random ra = new Random();
        final int r, g, b;
        r = ra.nextInt(255);
        g = ra.nextInt(255);
        b = ra.nextInt(255);
        final Color color = new Color(r, g, b);
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    /**
     * @return the busStopFile
     */
    public UploadedFile getBusStopFile() {
        return busStopFile;
    }

    /**
     * @param busStopFile the busStopFile to set
     */
    public void setBusStopFile(UploadedFile busStopFile) {
        this.busStopFile = busStopFile;
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
     * @return the telofunFile
     */
    public UploadedFile getTelofunFile() {
        return telofunFile;
    }

    /**
     * @param telofunFile the telofunFile to set
     */
    public void setTelofunFile(UploadedFile telofunFile) {
        this.telofunFile = telofunFile;
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

    public void upload() {
        if (this.file != null && this.file.getSize() > 0) {
            FacesMessage message = new FacesMessage("Succesful", this.file.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);

            final String content = new String(this.file.getContents());
            final ObjectMapper objectMapper = new ObjectMapper();

            try {
                this.alterNativs = objectMapper.readValue(content, new TypeReference<List<AlterNativ>>() {
                });
                this.updateMapModel();
            } catch (IOException ex) {
                Logger.getLogger(IndexView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.busStopFile != null && this.busStopFile.getSize() > 0) {
            FacesMessage message = new FacesMessage("Succesful", this.busStopFile.getFileName() + " bus stops are uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            try {
                this.setBusStops(BusStopParser.getBusStops(this.busStopFile.getInputstream()));
                this.updateMapModel();
            } catch (IOException ex) {
                Logger.getLogger(IndexView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.telofunFile != null && this.telofunFile.getSize() > 0) {
            FacesMessage message = new FacesMessage("Succesful", this.telofunFile.getFileName() + " telofun stops are uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            try {
                final Telofun telofun = TelofunParser.getTelofun(this.telofunFile.getContents());
                this.telofuns = telofun.getFeatures();
                this.updateMapModel();
            } catch (IOException ex) {
                Logger.getLogger(IndexView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public StreamedContent getShapefile() {
        if (null != this.alterNativs) {
            try {
                this.shapefile = new DefaultStreamedContent(new FileInputStream(this.shapefileService.exportToShapefile(this.alterNativs)), "application/zip", "alternativ-shp.zip");
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
