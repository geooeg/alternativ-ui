package com.vgilab.alternative.google;

import com.vgilab.alternativ.business.geo.Coordinate3D;
import com.vgilab.alternativ.google.GoogleApi;
import com.vgilab.alternativ.google.GoogleGeocoding;
import com.vgilab.alternativ.google.GoogleMapsRoadsApi;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author smuellner
 */
public class GoogleApiJUnitTest {
    
    private final static Logger LOGGER = Logger.getGlobal();
    
    public GoogleApiJUnitTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void googleGeocodingHebrew() {
        final GoogleGeocoding googleGeocoding = GoogleApi.googleGeocoding("מונטיפיורי 2, תל אביב יפו, ישראל", "iw");
        final String status = googleGeocoding.getStatus();
        LOGGER.severe(status);
    }
    
    @Test
    public void googleGeocodingEnglish() {
        final GoogleGeocoding googleGeocoding = GoogleApi.googleGeocoding("USA, New York, 1st Avenue", "en");
        final String status = googleGeocoding.getStatus();
        LOGGER.severe(status);
    }
    
    @Test
    public void snapToRoads() {
        List<Coordinate3D> coordinates = new LinkedList<>();
        coordinates.add(new Coordinate3D(52.442222,9.745222,0.0));
        GoogleMapsRoadsApi.snapToRoads(coordinates, true);
    }
    
}
