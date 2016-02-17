/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vgilab.alternativ.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author ljzhang
 */
@JsonIgnoreProperties(ignoreUnknown = true)

class GoogleGeocoding {
    
    private String status;

    private GeocodingResults geocodingResults;

    public GoogleGeocoding() {

    }
}
