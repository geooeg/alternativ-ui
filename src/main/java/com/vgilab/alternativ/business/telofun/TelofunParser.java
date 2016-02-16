package com.vgilab.alternativ.business.telofun;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgilab.alternativ.generated.Telofun;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Zhang
 */
public class TelofunParser {

    public static Telofun getTelofun(byte[] contents) throws IOException {
        final String content = new String(contents);
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(content, new TypeReference<Telofun>() {
        });
    }

}
