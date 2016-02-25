package com.vgilab.alternativ.business.busstop;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Zhang
 */
public class BusStopParser {

    public static List<BusStop> getBusStops(final InputStream is) throws UnsupportedEncodingException, IOException {
        final Reader reader = new InputStreamReader(is, "UTF-8");
        final List<BusStop> busStops = new LinkedList<>();
        for (final CSVRecord record : CSVFormat.DEFAULT.parse(reader)) {
            final String id = record.get(0);
            final String code = record.get(1);
            final String title = record.get(2);
            final String desc = record.get(3);
            try {
                final NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
                final Double lat = format.parse(record.get(4)).doubleValue();
                final Double lng = format.parse(record.get(5)).doubleValue();
                busStops.add(new BusStop(id, code, title, desc, lat, lng));
            } catch (ParseException | NumberFormatException ex) {
                Logger.getLogger(BusStopParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return busStops;
    }

}
