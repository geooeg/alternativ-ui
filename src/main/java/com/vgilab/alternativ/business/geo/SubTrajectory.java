package com.vgilab.alternativ.business.geo;

import java.util.List;

/**
 *
 * @author smuellner
 */
public class SubTrajectory {
    private String id;
    private TravelMode travelMode;
    private List<Coordinate3D> coordinates;

    public SubTrajectory(String id, TravelMode travelMode, List<Coordinate3D> coordinates) {
       this.id = id;
       this.travelMode = travelMode;
       this.coordinates = coordinates;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

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
     * @return the coordinates
     */
    public List<Coordinate3D> getCoordinates() {
        return coordinates;
    }

    /**
     * @param coordinates the coordinates to set
     */
    public void setCoordinates(List<Coordinate3D> coordinates) {
        this.coordinates = coordinates;
    }
    
}
