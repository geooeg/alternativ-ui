package com.vgilab.alternativ.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgilab.alternativ.generated.AlterNativ;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * http://alter.meteor.com/api/export
 *
 * @author ljzhang
 */
public class AlterNativApi {

    public static String ALTER_NATIV_REST_URL = "http://alter.meteor.com/api/export";

    private final static Logger LOGGER = Logger.getGlobal();

    public static List<AlterNativ> getAll() {
        final HttpHeaders headers = new HttpHeaders();
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ALTER_NATIV_REST_URL);
        final HttpEntity<?> entity = new HttpEntity<>(headers);

        // CORRECT: that would be the correct headers
        // headers.set("Accept", MediaType.APPLICATION_JSON_VALUE); 
        // FIXME: Workaround wrong MIME type!
        final RestTemplate restTemplate = new RestTemplate();
        final List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                final MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                jsonConverter.setObjectMapper(new ObjectMapper());
                // MediaType types[] = new MediaType[]{new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET), new MediaType("text", "javascript", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET)};
                final MediaType types[] = new MediaType[]{MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON};
                headers.setAccept(Arrays.asList(types));
                jsonConverter.setSupportedMediaTypes(Arrays.asList(types));
            }
        }
        try {
            // lets just get the plain response as a string
            final HttpEntity<String> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity, String.class);

            LOGGER.severe(response.getBody());

            // Now deserialize/map the json data to our defined POJOs
            final ResponseEntity<List<AlterNativ>> alterNativResponse
                    = restTemplate.exchange(builder.build().encode().toUri(),
                            HttpMethod.GET, entity, new ParameterizedTypeReference<List<AlterNativ>>() {
                    });
            return alterNativResponse.getBody();

        } catch (Exception ex) {

        }
        return null;
    }

}
