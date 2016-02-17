package com.vgilab.alternativ.google;

import java.util.logging.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author Zhang
 */
public class GoogleApi {
    public static String GOOGLE_REST_URL = "https://maps.googleapis.com/maps/api/geocode/json?";
    public static String GOOGLE_API_KEY = "";
    public static String Sample_Address = "מונטיפיורי 2, תל אביב יפו, ישראל";
    
    private final static Logger LOGGER = Logger.getGlobal();
    
    public static GoogleGeocoding googleGeocoding () {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GOOGLE_REST_URL)
                .queryParam("address", Sample_Address)
                .queryParam("key", GOOGLE_API_KEY)
                .queryParam("language", "iw");
        final HttpEntity<?> entity = new HttpEntity<>(headers);
        
        // lets just get the plain response as a string
        final HttpEntity<String> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity, String.class);

        LOGGER.severe(response.getBody());

        // Now deserialize/map the json data to our defined POJOs  ???? i dont understand this part
        final ResponseEntity<GoogleGeocoding> geocodingResponse
                = restTemplate.exchange(builder.build().encode().toUri(),
                        HttpMethod.GET, entity, new ParameterizedTypeReference<GoogleGeocoding>() {
                });
        return geocodingResponse.getBody();
    }
}
