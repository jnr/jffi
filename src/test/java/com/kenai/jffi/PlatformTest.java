
package com.kenai.jffi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Tests the Platform class
 */
public class PlatformTest {

    public PlatformTest() {
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
    @Test public void isSupported() {
        // This should never fail
        assertTrue("isSupported failed", Platform.getPlatform().isSupported());
    }

    @Test public void getMajorVersion() {
        // This should never fail
        String version = System.getProperty("java.version");
        Pattern pattern = Pattern.compile("1\\.([0-9]+).*|([0-9]+).*");
        Matcher matcher = pattern.matcher(version);
        boolean matches = matcher.matches();
        assertTrue("Unexpected java version string: " + version, matches);

        String major = matcher.group(1);
        if (major == null) {
            major = matcher.group(2);
        }
        assertEquals(
                Integer.parseInt(major),
                Platform.getPlatform().getJavaMajorVersion()
        );
    }
}