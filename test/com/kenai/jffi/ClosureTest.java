
package com.kenai.jffi;

import com.kenai.jffi.UnitHelper.InvokerType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClosureTest {
    private static interface LibClosureTest {
        void testClosureVrV(long addr);
        byte testClosureVrB(long addr);
        short testClosureVrS(long addr);
        int testClosureVrI(long addr);
        long testClosureVrL(long addr);
        float testClosureVrF(long addr);
        double testClosureVrD(long addr);
    }
    private LibClosureTest lib, fastint, fastlong;
    public ClosureTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        lib = UnitHelper.loadTestLibrary(LibClosureTest.class, InvokerType.Default);
        fastlong = UnitHelper.loadTestLibrary(LibClosureTest.class, InvokerType.FastLong);
        fastint = Platform.getPlatform().addressSize() == 32
                ? UnitHelper.loadTestLibrary(LibClosureTest.class, InvokerType.FastInt)
                : fastlong;
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    private void testClosureVrV(LibClosureTest lib) {
        final boolean called[] = { false };
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.VOID, new Type[0], CallingConvention.DEFAULT);
        lib.testClosureVrV(handle.getAddress());
        assertTrue("Closure not called", called[0]);
    }
    @Test public void defaultClosureVrV() throws Throwable {
        testClosureVrV(lib);
    }
    @Test public void fastIntClosureVrV() throws Throwable {
        if (Platform.getPlatform().addressSize() == 32) {
            testClosureVrV(fastint);
        }
    }
    @Test public void fastLongClosureVrV() throws Throwable {
        testClosureVrV(fastlong);
    }
    private void testClosureVrB(LibClosureTest lib) {
        final boolean called[] = { false };
        final byte MAGIC = (byte) 0x12;
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                buffer.setByteReturn(MAGIC);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.SINT8, new Type[0], CallingConvention.DEFAULT);
        byte retval = lib.testClosureVrB(handle.getAddress());
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong value returned by closure", MAGIC, retval);
    }
    @Test public void defaultClosureVrB() throws Throwable {
        testClosureVrB(lib);
    }
    @Test public void fastIntClosureVrB() throws Throwable {
        testClosureVrB(fastint);
    }
    @Test public void fastLongClosureVrB() throws Throwable {
        testClosureVrB(fastlong);
    }

    private void testClosureVrS(LibClosureTest lib) {
        final boolean called[] = { false };
        final short MAGIC = (byte) 0x1234;
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                buffer.setShortReturn(MAGIC);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.SINT32, new Type[0], CallingConvention.DEFAULT);
        short retval = lib.testClosureVrS(handle.getAddress());
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong value returned by closure", MAGIC, retval);
    }
    @Test public void defaultClosureVrS() throws Throwable {
        testClosureVrS(lib);
    }
    @Test public void fastIntClosureVrS() throws Throwable {
        testClosureVrS(fastint);
    }
    @Test public void fastLongClosureVrS() throws Throwable {
        testClosureVrS(fastlong);
    }

    private void testClosureVrI(LibClosureTest lib) {
        final boolean called[] = { false };
        final int MAGIC = 0x12345678;
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                buffer.setIntReturn(MAGIC);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.SINT64, new Type[0], CallingConvention.DEFAULT);
        int retval = lib.testClosureVrI(handle.getAddress());
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong value returned by closure", MAGIC, retval);
    }
    @Test public void defaultClosureVrI() throws Throwable {
        testClosureVrI(lib);
    }
    @Test public void fastIntClosureVrI() throws Throwable {
        testClosureVrI(fastint);
    }
    @Test public void fastLongClosureVrI() throws Throwable {
        testClosureVrI(fastlong);
    }

    private void testClosureVrF(LibClosureTest lib) {
        final boolean called[] = { false };
        final float MAGIC = (float) 0x12345678;
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                buffer.setFloatReturn(MAGIC);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.FLOAT, new Type[0], CallingConvention.DEFAULT);
        float retval = lib.testClosureVrF(handle.getAddress());
        assertTrue("Closure not called", called[0]);
        assertTrue("Wrong value returned by closure", (MAGIC -retval) < 0.0001);
    }
    @Test public void defaultClosureVrF() throws Throwable {
        testClosureVrF(lib);
    }
    @Test public void fastLongClosureVrF() throws Throwable {
        testClosureVrF(fastlong);
    }

    private void testClosureVrD(LibClosureTest lib) {
        final boolean called[] = { false };
        final double MAGIC = (double) 0x12345678;
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                buffer.setDoubleReturn(MAGIC);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.DOUBLE, new Type[0], CallingConvention.DEFAULT);
        float retval = lib.testClosureVrF(handle.getAddress());
        assertTrue("Closure not called", called[0]);
        assertTrue("Wrong value returned by closure", (MAGIC -retval) < 0.0001);
    }
    @Test public void defaultClosureVrD() throws Throwable {
        testClosureVrD(lib);
    }
    @Test public void fastLongClosureVrD() throws Throwable {
        testClosureVrD(fastlong);
    }
}