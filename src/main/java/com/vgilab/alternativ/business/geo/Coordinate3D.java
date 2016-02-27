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
        this.latitude = x;
        this.longitude = y;
        this.altitude = z;
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
