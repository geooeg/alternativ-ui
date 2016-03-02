package com.vgilab.alternativ.business.geo;

/**
 *
 * @author smuellner
 */
public class Coordinate3D {
    private Double latitude;
    private Double longitude;
    private Double altitude;

    public Coordinate3D(Double x, Double y, Double z) {
        this.longitude = x;
        this.latitude = y;
        this.altitude = z;
    }

    public Coordinate3D() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the altitude
     */
    public Double getAltitude() {
        return altitude;
    }

    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }
}
