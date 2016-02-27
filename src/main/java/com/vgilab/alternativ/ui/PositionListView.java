package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.business.geo.AnalysedTrip;
import com.vgilab.alternativ.business.geo.Position;
import com.vgilab.alternativ.business.geo.SpatialAnalysisService;
import com.vgilab.alternativ.generated.AlterNativ;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private SpatialAnalysisService spatialAnalysisService;

    @PostConstruct
    public void init() {
    }

    /**
     * @param alterNativs the alterNativs to set
     */
    public void setAlterNativs(List<AlterNativ> alterNativs) {
        this.alterNativs = alterNativs;
        this.trips = this.spatialAnalysisService.analyseRoutes(this.alterNativs);
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
}
