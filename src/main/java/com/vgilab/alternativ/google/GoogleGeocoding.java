package com.vgilab.alternativ.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author ljzhang
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleGeocoding {
    
    private String status;

    private GeocodingResults geocodingResults;

    public GoogleGeocoding() {

    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the geocodingResults
     */
    public GeocodingResults getGeocodingResults() {
        return geocodingResults;
    }

    /**
     * @param geocodingResults the geocodingResults to set
     */
    public void setGeocodingResults(GeocodingResults geocodingResults) {
        this.geocodingResults = geocodingResults;
    }
}
