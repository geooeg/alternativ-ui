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
}
