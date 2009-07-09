
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

    @Test public void pageSize() {
        long pageSize = Foreign.getInstance().pageSize();
        assertNotSame("Invalid page size", 0, pageSize);
    }
    @Test public void vmalloc() {
        long addr = Foreign.getInstance().vmalloc(0, 123, Foreign.PROT_READ | Foreign.PROT_WRITE, Foreign.MEM_DATA);
        assertNotSame("Failed to allocate memory", -1, addr);
    }

    @Test public void vmfree() {
        final int SIZE = 123;
        long addr = Foreign.getInstance().vmalloc(0, SIZE, Foreign.PROT_READ | Foreign.PROT_WRITE, Foreign.MEM_DATA);
        assertNotSame("Failed to allocate memory", -1, addr);
        assertTrue("Failed to free memory", Foreign.getInstance().vmfree(addr, SIZE));
    }

    @Test public void writeToAllocatedMemory() {
        final int SIZE = 257;
        final byte[] MAGIC = { 't', 'e', 's', 't' };
        long addr = Foreign.getInstance().vmalloc(0, SIZE, Foreign.PROT_READ | Foreign.PROT_WRITE, Foreign.MEM_DATA);
        assertNotSame("Failed to allocate memory", -1, addr);
        MemoryIO.getInstance().putByteArray(addr, MAGIC, 0, MAGIC.length);
        byte[] tmp = new byte[MAGIC.length];
        MemoryIO.getInstance().getByteArray(addr, tmp, 0, MAGIC.length);
        assertArrayEquals("Incorrect data read back", MAGIC, tmp);
        assertTrue("Failed to free memory", Foreign.getInstance().vmfree(addr, SIZE));
    }

}