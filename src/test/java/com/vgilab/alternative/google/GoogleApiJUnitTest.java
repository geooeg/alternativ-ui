package com.vgilab.alternative.google;

import com.vgilab.alternativ.google.GoogleApi;
import com.vgilab.alternativ.google.GoogleGeocoding;
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
    public void hello() {
        final GoogleGeocoding googleGeocoding = GoogleApi.googleGeocoding();
        
    }
}
