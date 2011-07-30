
package com.kenai.jffi;

import java.lang.reflect.Method;
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
    @Test public void mmap() {
        if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
            final int SIZE = 123;
            long addr = Foreign.getInstance().mmap(0, SIZE, Foreign.PROT_READ | Foreign.PROT_WRITE,
                Foreign.MAP_PRIVATE | Foreign.MAP_ANON, -1, 0);
            assertNotSame("Failed to allocate memory", -1L, addr);
        }
    }

    @Test public void munmap() {
        if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
            final int SIZE = 123;
            long addr = Foreign.getInstance().mmap(0, SIZE, Foreign.PROT_READ | Foreign.PROT_WRITE,
                    Foreign.MAP_PRIVATE | Foreign.MAP_ANON, -1, 0);
            assertNotSame("Failed to allocate memory", -1, addr);
            assertTrue("Failed to free memory", Foreign.getInstance().munmap(addr, SIZE) == 0);
        }
    }

    @Test public void writeToAllocatedMemory() {
        if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
            final int SIZE = 257;
            final byte[] MAGIC = {'t', 'e', 's', 't'};
            long addr = Foreign.getInstance().mmap(0, SIZE, Foreign.PROT_READ | Foreign.PROT_WRITE,
                    Foreign.MAP_PRIVATE | Foreign.MAP_ANON, -1, 0);
            assertNotSame("Failed to allocate memory", -1, addr);
            MemoryIO.getInstance().putByteArray(addr, MAGIC, 0, MAGIC.length);
            byte[] tmp = new byte[MAGIC.length];
            MemoryIO.getInstance().getByteArray(addr, tmp, 0, MAGIC.length);
            assertArrayEquals("Incorrect data read back", MAGIC, tmp);
            assertTrue("Failed to free memory", Foreign.getInstance().munmap(addr, SIZE) == 0);
        }
    }
    static class ClosureProxy {
        void invoke(Closure.Buffer buf) {}
    }
//    @Test public void freeClosure() throws Throwable {
//        Method m = ClosureProxy.class.getDeclaredMethod("invoke", new Class[] { Closure.Buffer.class});
//        long cl = Foreign.getInstance().newClosure(new ClosureProxy(), m, Type.VOID.handle, new long[0], 0);
//        Foreign.getInstance().freeClosure(cl);
//    }

//    @Test public void newNativeMethod() throws Throwable {
//        Foreign.getInstance().newNativeMethod("test", "()V", 0);
//    }
}