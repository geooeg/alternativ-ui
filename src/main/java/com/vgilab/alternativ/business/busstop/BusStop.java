package com.vgilab.alternativ.business.busstop;

import org.primefaces.model.map.LatLng;

/**
 *
 * @author Zhang
 */
public class BusStop {
    private String id;
    private String code;
    private String name;
    private String description;
    private LatLng latLng;

    public BusStop(String id, String code, String name, String description, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.latLng = new LatLng(lat, lng);
    }
    
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }
}
