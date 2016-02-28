package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.business.geo.AnalysedTrip;
import com.vgilab.alternativ.business.geo.Coordinate3D;
import com.vgilab.alternativ.business.geo.Position;
import com.vgilab.alternativ.business.geo.SpatialAnalysisService;
import com.vgilab.alternativ.generated.AlterNativ;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    private String mapCenter;
    private Position selectedPosition;

    @Autowired
    private SpatialAnalysisService spatialAnalysisService;

    @PostConstruct
    public void init() {
        this.mapModel = new DefaultMapModel();
    }

    /**
     * @param alterNativs the alterNativs to set
     */
    public void setAlterNativs(List<AlterNativ> alterNativs) {
        this.alterNativs = alterNativs;
        this.trips = this.spatialAnalysisService.analyseRoutes(this.alterNativs, 20d);
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
        return deviation;
    }

    /**
     * @param deviation the deviation to set
     */
    public void setDeviation(String deviation) {
        this.deviation = deviation;
        try {
            final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            String pattern = "###0.0#";
            final DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
            decimalFormat.setParseBigDecimal(true);
            final BigDecimal deviationNumber = (BigDecimal) decimalFormat.parse(deviation);
            this.trips = this.spatialAnalysisService.analyseRoutes(this.alterNativs, deviationNumber.doubleValue());
        } catch (final ParseException pex) {

        }
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
                    this.mapCenter = coordinate.getLatitude()  + "," +  coordinate.getLongitude();
                }
                if (null != this.selectedPosition.getCoordinateForStep()) {
                    final Coordinate3D coordinate = this.selectedPosition.getCoordinateForStep();
                    final LatLng latLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                    this.mapModel.addOverlay(new Marker(latLng, "Step", ""));
                    this.mapCenter = coordinate.getLatitude()  + "," +  coordinate.getLongitude();
                }
            }
        }
    }

    public String getMapCenter() {
        return StringUtils.isEmpty(this.mapCenter) ? "37.335556, -122.009167" : this.mapCenter;
    }

    public MapModel getMapModel() {
        return this.mapModel;
    }

}
