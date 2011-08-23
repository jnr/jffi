
package com.kenai.jffi;

import com.kenai.jffi.UnitHelper.InvokerType;
import com.kenai.jffi.UnitHelper.Address;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClosureTest {
    private static interface LibClosureTest {
        void testClosureVrV(Address closure);
        byte testClosureVrB(Address closure);
        short testClosureVrS(Address closure);
        int testClosureVrI(Address closure);
        long testClosureVrL(Address closure);
        float testClosureVrF(Address closure);
        double testClosureVrD(Address closure);
        void testClosureTrV(Address closure, Address struct);

        void testClosureBrV(Address closure, byte value);
        void testClosureSrV(Address closure, short value);
        void testClosureIrV(Address closure, int value);
        void testClosureLrV(Address closure, long value);
        void testClosureFrV(Address closure, float value);
        void testClosureDrV(Address closure, double value);
        
        void testThreadedClosureVrV(Address closure, int count);
        
    }
    private LibClosureTest lib, fastint, fastlong, fastnum;
    public ClosureTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private static boolean isFastLongSupported() {
        return Platform.getPlatform().getCPU() == Platform.CPU.I386
                || Platform.getPlatform().getCPU() == Platform.CPU.X86_64;
    }

    private static boolean isFastIntSupported() {
        return Platform.getPlatform().getCPU() == Platform.CPU.I386
                || Platform.getPlatform().getCPU() == Platform.CPU.X86_64;
    }

    @Before
    public void setUp() {
        lib = UnitHelper.loadTestLibrary(LibClosureTest.class, InvokerType.Default);
        fastlong = UnitHelper.loadTestLibrary(LibClosureTest.class,
                isFastLongSupported() ? InvokerType.FastLong : InvokerType.Default);
        fastnum = UnitHelper.loadTestLibrary(LibClosureTest.class, InvokerType.FastNumeric);
        fastint = UnitHelper.loadTestLibrary(LibClosureTest.class,
                    isFastIntSupported() ? InvokerType.FastLong : InvokerType.Default);
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
        lib.testClosureVrV(new Address(handle));
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
    
    private void testThreadedClosureVrV(LibClosureTest lib, int count) {
        final boolean called[] = { false };
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.VOID, new Type[0], CallingConvention.DEFAULT);
        lib.testThreadedClosureVrV(new Address(handle), count);
        assertTrue("Closure not called", called[0]);
    }
    @Test public void defaultThreadedClosureVrV() throws Throwable {
        testThreadedClosureVrV(lib, 10000);
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
        byte retval = lib.testClosureVrB(new Address(handle));
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
        short retval = lib.testClosureVrS(new Address(handle));
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
        int retval = lib.testClosureVrI(new Address(handle));
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

    private void testClosureVrL(LibClosureTest lib) {
        final boolean called[] = { false };
        final long MAGIC = 0x12345678cafebabeL;
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                buffer.setLongReturn(MAGIC);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.SINT64, new Type[0], CallingConvention.DEFAULT);
        long retval = lib.testClosureVrL(new Address(handle));
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong value returned by closure", MAGIC, retval);
    }
    @Test public void defaultClosureVrL() throws Throwable {
        testClosureVrL(lib);
    }
    @Test public void fastLongClosureVrL() throws Throwable {
        testClosureVrL(fastlong);
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
        float retval = lib.testClosureVrF(new Address(handle));
        assertTrue("Closure not called", called[0]);
        assertTrue("Wrong value returned by closure", (MAGIC -retval) < 0.0001);
    }

    @Test public void defaultClosureVrF() throws Throwable {
        testClosureVrF(lib);
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
        double retval = lib.testClosureVrD(new Address(handle));
        assertTrue("Closure not called", called[0]);
        assertTrue("Wrong value returned by closure", (MAGIC -retval) < 0.0001);
    }
    @Test public void defaultClosureVrD() throws Throwable {
        testClosureVrD(lib);
    }
    @Test public void fastNumericClosureVrD() throws Throwable {
        testClosureVrD(fastnum);
    }

    @Test public void testClosureTrV() throws Throwable {
        final boolean called[] = { false };
        final byte[] s8 = { 0 };
        final float[] f32 = { 0 };
        final int[] s32 = { 0 };

        final byte S8_MAGIC = (byte) 0xfe;
        final int S32_MAGIC = (int) 0xdeadbeef;
        final float F32_MAGIC = (float) 0x12345678;

        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                long struct = buffer.getStruct(0);
                s8[0] = MemoryIO.getInstance().getByte(struct);
                f32[0] = MemoryIO.getInstance().getFloat(struct + 4);
                s32[0] = MemoryIO.getInstance().getInt(struct + 8);
            }
        };
        Struct s8f32s32 = new Struct(new Type[] { Type.SINT8, Type.FLOAT, Type.SINT32 });
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.VOID, new Type[] { s8f32s32, Type.POINTER }, CallingConvention.DEFAULT);
        long struct = MemoryIO.getInstance().allocateMemory(12, true);
        MemoryIO.getInstance().putByte(struct, S8_MAGIC);
        MemoryIO.getInstance().putFloat(struct + 4, F32_MAGIC);
        MemoryIO.getInstance().putInt(struct + 8, S32_MAGIC);
        lib.testClosureTrV(new Address(handle), new Address(struct));
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong s8 field value", S8_MAGIC, s8[0]);
        assertEquals("Wrong s32 field value", S32_MAGIC, s32[0]);
        assertTrue("Wrong f32 field value", (F32_MAGIC - f32[0]) < 0.0001);
        
    }


    @Test public void testClosureVrTFromArray() throws Throwable {
        final boolean called[] = { false };
        
        final byte S8_MAGIC = (byte) 0xfe;
        final int S32_MAGIC = (int) 0xdeadbeef;
        final float F32_MAGIC = (float) 0x12345678;

        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                ByteBuffer retVal = ByteBuffer.allocate(12).order(ByteOrder.nativeOrder());
                retVal.put(0, S8_MAGIC);
                retVal.putFloat(4, F32_MAGIC);
                retVal.putInt(8, S32_MAGIC);
                buffer.setStructReturn(retVal.array(), retVal.arrayOffset());
            }
        };
        Struct s8f32s32 = new Struct(new Type[] { Type.SINT8, Type.FLOAT, Type.SINT32 });

        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                s8f32s32, new Type[] { }, CallingConvention.DEFAULT);

        Function f = new Function(UnitHelper.findSymbol("testClosureVrT").address, s8f32s32, new Type[] { Type.POINTER });
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(f);
        paramBuffer.putAddress(handle.getAddress());

        ByteBuffer retval = ByteBuffer.wrap(Invoker.getInstance().invokeStruct(f, paramBuffer));
        retval.order(ByteOrder.nativeOrder());
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong s8 field value", S8_MAGIC, retval.get(0));
        assertEquals("Wrong s32 field value", S32_MAGIC, retval.getInt(8));
        assertTrue("Wrong f32 field value", (F32_MAGIC - retval.getFloat(4)) < 0.0001);

    }

    @Test public void testClosureVrTFromPointer() throws Throwable {
        final boolean called[] = { false };

        final byte S8_MAGIC = (byte) 0xfe;
        final int S32_MAGIC = (int) 0xdeadbeef;
        final float F32_MAGIC = (float) 0x12345678;

        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                long struct = MemoryIO.getInstance().allocateMemory(12, true);
                MemoryIO.getInstance().putByte(struct, S8_MAGIC);
                MemoryIO.getInstance().putFloat(struct + 4, F32_MAGIC);
                MemoryIO.getInstance().putInt(struct + 8, S32_MAGIC);
                buffer.setStructReturn(struct);
            }
        };
        Struct s8f32s32 = new Struct(new Type[] { Type.SINT8, Type.FLOAT, Type.SINT32 });

        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                s8f32s32, new Type[] { }, CallingConvention.DEFAULT);

        Function f = new Function(UnitHelper.findSymbol("testClosureVrT").address, s8f32s32, new Type[] { Type.POINTER });
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(f);
        paramBuffer.putAddress(handle.getAddress());
        
        ByteBuffer retval = ByteBuffer.wrap(Invoker.getInstance().invokeStruct(f, paramBuffer));
        retval.order(ByteOrder.nativeOrder());
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong s8 field value", S8_MAGIC, retval.get(0));
        assertEquals("Wrong s32 field value", S32_MAGIC, retval.getInt(8));
        assertTrue("Wrong f32 field value", (F32_MAGIC - retval.getFloat(4)) < 0.0001);

    }
    private void testClosureBrV(LibClosureTest lib) {
        final boolean called[] = { false };
        final byte MAGIC = (byte) 0x12;
        final byte[] data = { 0 };
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                data[0] = buffer.getByte(0);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.VOID, new Type[] { Type.SINT8 }, CallingConvention.DEFAULT);
        lib.testClosureBrV(new Address(handle), MAGIC);
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong value passed to closure", MAGIC, data[0]);
    }
    @Test public void defaultClosureBrV() throws Throwable {
        testClosureBrV(lib);
    }
    @Test public void fastIntClosureBrV() throws Throwable {
        testClosureBrV(fastint);
    }
    @Test public void fastLongClosureBrV() throws Throwable {
        testClosureBrV(fastlong);
    }
    private void testClosureSrV(LibClosureTest lib) {
        final boolean called[] = { false };
        final short MAGIC = (byte) 0x1234;
        final short[] data = { 0 };
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                data[0] = buffer.getShort(0);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.VOID, new Type[] { Type.SINT16 }, CallingConvention.DEFAULT);
        lib.testClosureSrV(new Address(handle), MAGIC);
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong value passed to closure", MAGIC, data[0]);
    }
    @Test public void defaultClosureSrV() throws Throwable {
        testClosureSrV(lib);
    }
    @Test public void fastIntClosureSrV() throws Throwable {
        testClosureSrV(fastint);
    }
    @Test public void fastLongClosureSrV() throws Throwable {
        testClosureSrV(fastlong);
    }

    private void testClosureIrV(LibClosureTest lib) {
        final boolean called[] = { false };
        final int MAGIC = 0x12345678;
        final int[] data = { 0 };
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                data[0] = buffer.getInt(0);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.VOID, new Type[] { Type.SINT32 }, CallingConvention.DEFAULT);
        lib.testClosureIrV(new Address(handle), MAGIC);
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong value passed to closure", MAGIC, data[0]);
    }
    @Test public void defaultClosureIrV() throws Throwable {
        testClosureIrV(lib);
    }
    @Test public void fastIntClosureIrV() throws Throwable {
        testClosureIrV(fastint);
    }
    @Test public void fastLongClosureIrV() throws Throwable {
        testClosureIrV(fastlong);
    }
    
    private void testClosureLrV(LibClosureTest lib) {
        final boolean called[] = { false };
        final long MAGIC = 0x12345678fee1deadL;
        final long[] data = { 0 };
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                data[0] = buffer.getLong(0);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.VOID, new Type[] { Type.SINT64 }, CallingConvention.DEFAULT);
        lib.testClosureLrV(new Address(handle), MAGIC);
        assertTrue("Closure not called", called[0]);
        assertEquals("Wrong value passed to closure", MAGIC, data[0]);
    }
    @Test public void defaultClosureLrV() throws Throwable {
        testClosureLrV(lib);
    }
    @Test public void fastLongClosureLrV() throws Throwable {
        testClosureLrV(fastlong);
    }

    private void testClosureFrV(LibClosureTest lib) {
        final boolean called[] = { false };
        final float MAGIC = (float) 0x12345678;
        final float[] data = { 0 };
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                data[0] = buffer.getFloat(0);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.VOID, new Type[] { Type.FLOAT }, CallingConvention.DEFAULT);
        lib.testClosureFrV(new Address(handle), MAGIC);
        assertTrue("Closure not called", called[0]);
        assertTrue("Wrong value passed to closure", (MAGIC - data[0]) < 0.0001);
    }
    @Test public void defaultClosureFrV() throws Throwable {
        testClosureFrV(lib);
    }
    
    private void testClosureDrV(LibClosureTest lib) {
        final boolean called[] = { false };
        final double MAGIC = (double) 0x12345678;
        final double[] data = { 0 };
        Closure closure = new Closure() {
            public void invoke(Buffer buffer) {
                called[0] = true;
                data[0] = buffer.getDouble(0);
            }
        };
        Closure.Handle handle = ClosureManager.getInstance().newClosure(closure,
                Type.VOID, new Type[] { Type.DOUBLE }, CallingConvention.DEFAULT);
        lib.testClosureDrV(new Address(handle), MAGIC);
        assertTrue("Closure not called", called[0]);
        assertTrue("Wrong value passed to closure", (MAGIC - data[0]) < 0.0001);
    }

    @Test public void defaultClosureDrV() throws Throwable {
        testClosureDrV(lib);
    }

    @Test public void allocateLots() throws Throwable {
        ClosureManager m = ClosureManager.getInstance();
        List<Closure.Handle> handles = new ArrayList<Closure.Handle>();

        for (int i = 0; i < 1000; ++i) {
            Closure c = new Closure() {

                public void invoke(Buffer buffer) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
            handles.add(m.newClosure(c, Type.FLOAT, new Type[0], CallingConvention.DEFAULT));
        }
        for (Closure.Handle h : handles) {
            h.dispose();
        }
        handles.clear();
        for (int i = 0; i < 1000; ++i) {
            Closure c = new Closure() {

                public void invoke(Buffer buffer) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
            handles.add(m.newClosure(c, Type.FLOAT, new Type[0], CallingConvention.DEFAULT));
        }
    }

    static class Proxy {
        public void invoke(long retval, long args) {

        }
    }
}
