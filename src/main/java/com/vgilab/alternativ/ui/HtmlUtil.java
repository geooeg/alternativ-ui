package com.vgilab.alternativ.ui;

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

}
