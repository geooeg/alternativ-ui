package com.vgilab.alternativ.google;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author Zhang
 */
public class GoogleApi {
    public static String GOOGLE_REST_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    public static String GOOGLE_API_KEY = "";
    private static final Logger LOGGER = Logger.getGlobal();

    static {
        final Properties properties = new Properties();
        try {
            final InputStream inputStream = GoogleMapsRoadsApi.class.getResourceAsStream("api.properties");
            if (null != inputStream) {
                properties.load(inputStream);
                GOOGLE_API_KEY = StringUtils.trimWhitespace(properties.getProperty("GOOGLE_API_KEY"));
            } else {
                LOGGER.severe("Google Maps Roads API inactive. Missing api properties file!");
            }
        } catch (FileNotFoundException e) {
            LOGGER.severe(e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
    }
    
    public static GoogleGeocoding googleGeocoding (final String address, final String language) {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GOOGLE_REST_URL)
                .queryParam("address", address)
                .queryParam("key", GOOGLE_API_KEY)
                .queryParam("language", language);
        final HttpEntity<?> entity = new HttpEntity<>(headers);
        
        // lets just get the plain response as a string
        final HttpEntity<String> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity, String.class);

        LOGGER.finest(response.getBody());

        // Now deserialize/map the json data to our defined POJOs  ???? i dont understand this part
        final ResponseEntity<GoogleGeocoding> geocodingResponse
                = restTemplate.exchange(builder.build().encode().toUri(),
                        HttpMethod.GET, entity, new ParameterizedTypeReference<GoogleGeocoding>() {
                });
        return geocodingResponse.getBody();
    }
}
