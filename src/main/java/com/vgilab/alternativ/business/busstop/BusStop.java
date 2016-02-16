package com.vgilab.alternativ.business.busstop;

import org.primefaces.model.map.LatLng;

/**
 *
 * @author Zhang
 */
public class BusStop {
    private String title;
    private String description;
    private LatLng latLng;

    public BusStop(String title, String description, double lat, double lng) {
        this.title = title;
        this.description = description;
        this.latLng = new LatLng(lat, lng);
    }
    
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the latLng
     */
    public LatLng getLatLng() {
        return latLng;
    }

    /**
     * @param latLng the latLng to set
     */
    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
