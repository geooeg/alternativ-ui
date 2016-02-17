package com.vgilab.alternativ.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author Zhang
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResult {
    @JsonProperty(value="formatted_address")
    private String formattedAddress;
    @JsonProperty(value="place_id")
    private String placeId;
    private List<String> types;

    /**
     * @return the formattedAddress
     */
    public String getFormattedAddress() {
        return formattedAddress;
    }

    /**
     * @param formattedAddress the formattedAddress to set
     */
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    /**
     * @return the placeId
     */
    public String getPlaceId() {
        return placeId;
    }

    /**
     * @param placeId the placeId to set
     */
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    /**
     * @return the types
     */
    public List<String> getTypes() {
        return types;
    }

    /**
     * @param types the types to set
     */
    public void setTypes(List<String> types) {
        this.types = types;
    }
}
