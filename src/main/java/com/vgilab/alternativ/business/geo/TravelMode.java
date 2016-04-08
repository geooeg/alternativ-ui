package com.vgilab.alternativ.business.geo;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 1 black - WALKING 2 green - BIYCYCLE 3 blue - CAR 4 orange - BUS 5 red -
 * TRAIN 6 pink - TRAM 7 white - or more OTHER
 *
 * @author smuellner
 */
public enum TravelMode {

    WALKING(1),
    BIYCYCLE(2),
    CAR(3),
    BUS(4),
    TRAIN(5),
    TRAM(6),
    OTHER(7);

    private int travelModeCode = 1;
    private static final Map<Integer, TravelMode> travelModeMap = new HashMap<Integer, TravelMode>();

    static {
        for (TravelMode travelMode : TravelMode.values()) {
            travelModeMap.put(travelMode.travelModeCode, travelMode);
        }
    }

    private TravelMode(final int code) {
        this.travelModeCode = code;
    }

    public int getTravelModeCode() {
        return travelModeCode;
    }

    public static TravelMode fromInt(final int code) {
        final TravelMode travelMode = travelModeMap.get(code);
        if (travelMode == null) {
            return TravelMode.OTHER;
        }
        return travelMode;
    }
}
