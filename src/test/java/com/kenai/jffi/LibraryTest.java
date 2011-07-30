package com.kenai.jffi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author wayne
 */
public class LibraryTest {

    public LibraryTest() {
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
    @Test public void dlopen() {
        String libName = UnitHelper.getCLibraryName();
        long handle = Foreign.getInstance().dlopen(libName, Library.LAZY | Library.GLOBAL);
        assertNotSame("Could not open libc.so", 0L, handle);
    }
}
