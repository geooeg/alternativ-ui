package com.vgilab.alternativ.ui;

import com.vgilab.alternativ.business.geo.TravelMode;
import java.awt.Color;
import java.util.Random;

/**
 *
 * @author smuellner
 */
public class HtmlUtil {

    public static String getRandomHTMLColor() {
        final Random ra = new Random();
        final int r, g, b;
        r = ra.nextInt(255);
        g = ra.nextInt(255);
        b = ra.nextInt(255);
        final Color color = new Color(r, g, b);
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }

    public static String getColorFromTravelMode(final TravelMode travelMode) {
        switch (travelMode) {
            case WALKING:
                return "black";
            case BIYCYCLE:
                return "green";
            case CAR:
                return "blue";
            case BUS:
                return "orange";
            case TRAIN:
                return "red";
            case TRAM:
                return "pink";
        }
        return "white";
    }
}