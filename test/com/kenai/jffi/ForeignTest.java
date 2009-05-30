
package com.kenai.jffi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test internals of the Foreign class
 */
public class ForeignTest {

    public ForeignTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test public void version() {
        final int VERSION = Foreign.VERSION_MAJOR << 16 | Foreign.VERSION_MINOR << 8 | Foreign.VERSION_MICRO;
        int version = Foreign.getInstance().getVersion();
        assertEquals("Bad version", VERSION, version);
    }
    
}