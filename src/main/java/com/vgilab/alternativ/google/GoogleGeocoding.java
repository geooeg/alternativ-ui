package com.vgilab.alternativ.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 *
 * @author ljzhang
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleGeocoding {
    
    private String status;

    private List<GeocodingResult> results;

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
     * @return the results
     */
    public List<GeocodingResult> getResults() {
        return results;
    }

    /**
     * @param results the results to set
     */
    public void setResults(List<GeocodingResult> results) {
        this.results = results;
    }
}
