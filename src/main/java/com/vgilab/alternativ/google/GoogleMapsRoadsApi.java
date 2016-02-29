/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vgilab.alternativ.google;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author Zhang
 */
public class GoogleMapsRoadsApi {

    public static String GOOGLE_MAPS_ROADS_REST_URL = "https://roads.googleapis.com/v1/snapToRoads";
    public static String GOOGLE_MAPS_ROADS_API_KEY = "";

    public static HttpEntity snapToRoads(final List<Coordinate3D> coordinates, final boolean interpolate) {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GOOGLE_MAPS_ROADS_REST_URL)
                .queryParam("path", GoogleMapsRoadsApi.coordinatesToPath(coordinates))
                .queryParam("interpolate", interpolate ? "true" : "false")
                .queryParam("key", GOOGLE_MAPS_ROADS_API_KEY);
        final HttpEntity<?> entity = new HttpEntity<>(headers);
        // lets just get the plain response as a string
        final HttpEntity response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity, String.class);
        return response;
    }

    public static String coordinatesToPath(final List<Coordinate3D> coordinates) {
        final StringBuilder stringBuilder = new StringBuilder();
        final DecimalFormat formatter = new DecimalFormat("###.######");
        final DecimalFormatSymbols decimalFormatSymbols = formatter.getDecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(decimalFormatSymbols);
        for (final Coordinate3D curCoordinate : coordinates) {
            stringBuilder.append(MessageFormat.format("{0},{1}", formatter.format(curCoordinate.getLatitude()), formatter.format(curCoordinate.getLongitude())));
            stringBuilder.append("|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
   }
}
