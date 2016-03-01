package com.vgilab.alternativ.google;

import com.vgilab.alternativ.business.geo.Coordinate3D;
import com.vgilab.alternativ.generated.GoogleMapsRoads;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
public class GoogleMapsRoadsApi {

    private static final String GOOGLE_MAPS_ROADS_REST_URL = "https://roads.googleapis.com/v1/snapToRoads";
    private static final String GOOGLE_MAPS_ROADS_API_KEY = "AIzaSyCw7DLT2RpgkDbBT82raAt2kMJ5WqMjP7w";
    private static final Integer GOOGLE_MAPS_ROADS_CHUNK_SIZE = 100;

    private final static Logger LOGGER = Logger.getGlobal();

    public static List<Coordinate3D> snapToRoadsUsingBatches(final List<Coordinate3D> coordinates, final boolean interpolate) {
        final List<Coordinate3D> snappedCoordinates = new LinkedList<>();
        for (int i = 0; i < coordinates.size(); i += GOOGLE_MAPS_ROADS_CHUNK_SIZE) {
            final Coordinate3D[] coordinatesChunk = (Coordinate3D[]) Arrays.copyOfRange(coordinates.toArray(), i, i + GOOGLE_MAPS_ROADS_CHUNK_SIZE);
            final GoogleMapsRoads googleMapsRoads = GoogleMapsRoadsApi.snapToRoads(new LinkedList<>(Arrays.asList(coordinatesChunk)), interpolate);
        }
        return snappedCoordinates;
    }

    public static GoogleMapsRoads snapToRoads(final List<Coordinate3D> coordinates, final boolean interpolate) {
        final String coordinatesToPath = GoogleMapsRoadsApi.coordinatesToPath(coordinates);
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GOOGLE_MAPS_ROADS_REST_URL)
                .queryParam("path", coordinatesToPath)
                .queryParam("interpolate", interpolate ? "true" : "false")
                .queryParam("key", GOOGLE_MAPS_ROADS_API_KEY);
        final HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            // lets just get the plain response as a string
            final HttpEntity<String> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity, String.class);
            LOGGER.finest(response.getBody());
            // Now deserialize/map the json data to our defined POJOs
            final ResponseEntity<GoogleMapsRoads> googleMapsRoadsResponse
                    = restTemplate.exchange(builder.build().encode().toUri(),
                            HttpMethod.GET, entity, new ParameterizedTypeReference<GoogleMapsRoads>() {
                    });

            return googleMapsRoadsResponse.getBody();
        } catch (Exception ex) {
            LOGGER.severe(ex.getLocalizedMessage());
        }
        return null;
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
