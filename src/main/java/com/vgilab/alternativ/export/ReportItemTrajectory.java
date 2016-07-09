package com.vgilab.alternativ.export;

import com.vgilab.alternativ.business.geo.TravelMode;

/**
 *
 * @author smuellner
 */
public class ReportItemTrajectory {
    private TravelMode travelMode;
    private Double distance = 0d;

    /**
     * @return the travelMode
     */
    public TravelMode getTravelMode() {
        return travelMode;
    }

    /**
     * @param travelMode the travelMode to set
     */
    public void setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
    }

    /**
     * @return the distance
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
