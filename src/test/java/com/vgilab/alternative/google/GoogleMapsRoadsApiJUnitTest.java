package com.vgilab.alternative.google;

import com.vgilab.alternativ.business.geo.Coordinate3D;
import com.vgilab.alternativ.generated.GoogleMapsRoads;
import com.vgilab.alternativ.generated.SnappedPoint;
import com.vgilab.alternativ.google.GoogleMapsRoadsApi;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author smuellner
 */
public class GoogleMapsRoadsApiJUnitTest {

    private final static Logger LOGGER = Logger.getGlobal();
    
    public GoogleMapsRoadsApiJUnitTest() {
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

    @Ignore
    @Test
    public void snapToRoads() {
        final List<Coordinate3D> coordinates = new LinkedList<>();
        coordinates.add(new Coordinate3D(24.942795, 60.170880, 0d));
        coordinates.add(new Coordinate3D(24.942796, 60.170879, 0d));
        coordinates.add(new Coordinate3D(24.942796, 60.170877, 0d));
        final GoogleMapsRoads snapToRoads = GoogleMapsRoadsApi.snapToRoads(coordinates, true);
        for(final SnappedPoint curSnappedPoint : snapToRoads.getSnappedPoints()) {
            LOGGER.info(MessageFormat.format("Lng: {0}, Lat {1}, PlaceId {2}", curSnappedPoint.getLocation().getLongitude(), curSnappedPoint.getLocation().getLatitude(), curSnappedPoint.getPlaceId()));
        }
    }

    @Ignore
    @Test
    public void snapToRoadsWithBatches() {
        final List<Coordinate3D> coordinates = new LinkedList<>();
        // fill 250 entries to coordinates
        for(int i = 0; i <= 250; i++) {
            final double increment = 0.1 * i;
            coordinates.add(new Coordinate3D(24.942795 + increment, 60.170880 + increment, 0d));
        }
        final List<Coordinate3D> snapToRoadsUsingBatches = GoogleMapsRoadsApi.snapToRoadsUsingBatches(coordinates, true);
        for(final Coordinate3D curCoordinate : snapToRoadsUsingBatches) {
            LOGGER.info(MessageFormat.format("Lng: {0}, Lat {1}", curCoordinate.getLongitude(), curCoordinate.getLatitude()));
        }
    }
}