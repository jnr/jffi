package com.kenai.jffi;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 */
public class InvokerTest {
    private static final MemoryIO IO = MemoryIO.getInstance();

    static Function getFunction(String name, Type returnType, Type... parameterTypes) {
        UnitHelper.Address fn = UnitHelper.findSymbol(name);
        return new Function(fn.address, returnType, parameterTypes);
    }

    static CallContext getContext(Type returnType, Type... parameterTypes) {
        return new CallContext(returnType, parameterTypes);
    }

    static final long N1 = 0x1eafbeef;
    static final long N2 = 0x1010dead;
    static final long N3 = 0x10101010;
    static final long N4 = 0x01010101;
    static final long N5 = 0x0000beef;

    static ObjectParameterInfo.ComponentType NATIVE_LONG = Platform.getPlatform().longSize() == 32
            ? ObjectParameterInfo.INT : ObjectParameterInfo.LONG;

    static long getNativeUnsignedLong(UnitHelper.Address ptr) {
        if (Platform.getPlatform().longSize() == 32) {
            long n = IO.getInt(ptr.address);
            return n < 0 ? ((n & 0x7FFFFFFFL) + 0x80000000L) : n;
        } else {
            return IO.getLong(ptr.address);
        }
    }

    static void putNativeUnsignedLong(UnitHelper.Address ptr, long value) {
        if (Platform.getPlatform().longSize() == 32) {
            IO.putInt(ptr.address, (int) value);
        } else {
            IO.putLong(ptr.address, value);
        }
    }

    static Object newNativeUnsignedLongArray(long... values) {
        if (Platform.getPlatform().longSize() == 32) {
            int[] iarray = new int[values.length];
            for (int i = 0; i < iarray.length; i++) {
                iarray[i] = (int) values[i];
            }
            return iarray;

        } else {
            return values;
        }
    }

    static long getNativeUnsignedLong(Object arr) {
        return getNativeUnsignedLong(arr, 0);
    }

    static long getNativeUnsignedLong(Object arr, int idx) {

        if (long[].class == arr.getClass()) {
            long[] larr = long[].class.cast(arr);
            return larr[idx];
        } else {
            int[] iarr = int[].class.cast(arr);
            long n = iarr[idx];
            return n < 0 ? ((n & 0x7FFFFFFFL) + 0x80000000L) : n;
        }
    }

    static long unsigned(long n) {
        return n < 0 ? ((n & 0x7FFFFFFFL) + 0x80000000L) : n;
    }


    static class HeapArrayStrategy extends ObjectParameterStrategy {
        private int offset, length;

        HeapArrayStrategy(int offset, int length) {
            super(HEAP);
            this.offset = offset;
            this.length = length;
        }

        @Override
        public long address(Object parameter) {
            return 0L;
        }

        @Override
        public Object object(Object parameter) {
            return parameter;
        }

        @Override
        public int offset(Object parameter) {
            return offset;
        }

        @Override
        public int length(Object parameter) {
            return length;
        }
    }

    static class DirectStrategy extends ObjectParameterStrategy {

        DirectStrategy() {
            super(DIRECT);
        }

        @Override
        public long address(Object parameter) {
            return ((UnitHelper.Address) parameter).address;
        }

        @Override
        public Object object(Object parameter) {
            throw new IllegalStateException("not a heap object");
        }

        @Override
        public int offset(Object parameter) {
            throw new IllegalStateException("not a heap object");
        }

        @Override
        public int length(Object parameter) {
            throw new IllegalStateException("not a heap object");
        }
    }

    static class NativeInvoker extends Invoker {
        NativeInvoker() {
            super(Foreign.getInstance(), NativeObjectParameterInvoker.getInstance());
        }

        @Override
        public long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return Invoker.getInstance().invokeAddress(function, buffer);
        }

        @Override
        public long invokeAddress(CallContext ctx, long function, HeapInvocationBuffer buffer) {
            return Invoker.getInstance().invokeAddress(ctx, function, buffer);
        }
    }

    static class HeapInvoker extends Invoker {
        HeapInvoker() {
            super(Foreign.getInstance(), HeapObjectParameterInvoker.getInstance());
        }

        @Override
        public long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return Invoker.getInstance().invokeAddress(function, buffer);
        }

        @Override
        public long invokeAddress(CallContext ctx, long function, HeapInvocationBuffer buffer) {
            return Invoker.getInstance().invokeAddress(ctx, function, buffer);
        }
    }

    public static void invokeO(Invoker invoker) {
        Function function = getFunction("invokeO", Type.SLONG, Type.POINTER);
        CallContext ctx = getContext(Type.SLONG, Type.POINTER);
        Object arr = newNativeUnsignedLongArray(N1);
        ObjectParameterStrategy strategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);

        long ret = invoker.invokeN1(ctx, function.getFunctionAddress(), 0, 1, arr, strategy, info);
        assertEquals("incorrect return value", N1, unsigned(ret));
        assertEquals("incorrect array value", 0xdeadbeefL, getNativeUnsignedLong(arr));
    }

    public static void invokeNO(Invoker invoker) {
        Function function = getFunction("invokeNO", Type.SLONG, Type.SLONG, Type.POINTER);
        CallContext ctx = getContext(Type.SLONG, Type.SLONG, Type.POINTER);
        Object arr = newNativeUnsignedLongArray(N1);
        ObjectParameterStrategy strategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        long ret = invoker.invokeN2(ctx, function.getFunctionAddress(), N2, 0L, 1, arr, strategy, info);
        assertEquals("incorrect return value", N1, unsigned(ret));
        assertEquals("incorrect array value", N2, getNativeUnsignedLong(arr));
    }

    public static void invokeON(Invoker invoker) {
        Function function = getFunction("invokeON", Type.SLONG, Type.POINTER, Type.SLONG);
        CallContext ctx = getContext(Type.SLONG, Type.POINTER, Type.SLONG);
        Object arr = newNativeUnsignedLongArray(N1);
        ObjectParameterStrategy strategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        long ret = invoker.invokeN2(ctx, function.getFunctionAddress(), 0L, N2, 1, arr, strategy, info);
        assertEquals("incorrect return value", N1, unsigned(ret));
        assertEquals("incorrect array value", N2, getNativeUnsignedLong(arr));
    }

    public static void invokeOO(Invoker invoker) {
        Function function = getFunction("invokeOO", Type.SLONG, Type.POINTER, Type.POINTER);
        CallContext ctx = getContext(Type.SLONG, Type.POINTER, Type.POINTER);
        Object arr1 = newNativeUnsignedLongArray(N1);
        Object arr2 = newNativeUnsignedLongArray(N2);
        ObjectParameterStrategy strategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        long ret = invoker.invokeN2(ctx, function.getFunctionAddress(), 0L, 0L, 2, arr1, strategy, o1info, arr2, strategy, o2info);
        assertEquals("incorrect array value", N2, getNativeUnsignedLong(arr1));
        assertEquals("incorrect array value", N1, getNativeUnsignedLong(arr2));
        assertEquals("incorrect return value", unsigned(N1 + N2), unsigned(ret));
    }

    public static void invokeDO(Invoker invoker) {
        Function function = getFunction("invokeOO", Type.SLONG, Type.POINTER, Type.POINTER);
        CallContext ctx = getContext(Type.SLONG, Type.POINTER, Type.POINTER);
        UnitHelper.Address o1 =  new UnitHelper.Address(IO.allocateMemory(8, true));
        putNativeUnsignedLong(o1, N1);
        Object o2 = newNativeUnsignedLongArray(N2);
        ObjectParameterStrategy s1 = new DirectStrategy();
        ObjectParameterStrategy s2 = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);

        long ret = invoker.invokeN2(ctx, function.getFunctionAddress(), o1.address, 0L, 1, o1, s1, o1info, o2, s2, o2info);
        assertEquals("incorrect ptr value", N2, getNativeUnsignedLong(o1));
        assertEquals("incorrect array value", N1, getNativeUnsignedLong(o2));
        assertEquals("incorrect return value", N1 + N2, unsigned(ret));
    }

    public static void invokeOD(Invoker invoker) {
        Function function = getFunction("invokeOO", Type.SLONG, Type.POINTER, Type.POINTER);
        CallContext ctx = getContext(Type.SLONG, Type.POINTER, Type.POINTER);
        UnitHelper.Address ptr =  new UnitHelper.Address(IO.allocateMemory(8, true));
        putNativeUnsignedLong(ptr, N2);
        Object array = newNativeUnsignedLongArray(N1);
        ObjectParameterStrategy ptrStrategy = new DirectStrategy();
        ObjectParameterStrategy arrayStrategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);

        long ret = invoker.invokeN2(ctx, function.getFunctionAddress(), 0L, ptr.address, 1, array, arrayStrategy, o1info, ptr, ptrStrategy, o2info);
        assertEquals("incorrect ptr value", N1, getNativeUnsignedLong(ptr));
        assertEquals("incorrect array value", N2, getNativeUnsignedLong(array));
        assertEquals("incorrect return value", N1 + N2, unsigned(ret));
    }

    public static void invokeOONOO(Invoker invoker) {
        Function function = getFunction("invokeOONOO", Type.SLONG, Type.POINTER, Type.POINTER, Type.SLONG, Type.POINTER, Type.POINTER);
        CallContext ctx = getContext(Type.SLONG, Type.POINTER, Type.POINTER, Type.SLONG, Type.POINTER, Type.POINTER);

        Object o1 = newNativeUnsignedLongArray(N1);
        Object o2 = newNativeUnsignedLongArray(N2);
        Object o3 = newNativeUnsignedLongArray(N3);
        Object o4 = newNativeUnsignedLongArray(N4);

        ObjectParameterStrategy s1 = new HeapArrayStrategy(0, 1);
        ObjectParameterStrategy s2 = new HeapArrayStrategy(0, 1);
        ObjectParameterStrategy s3 = new HeapArrayStrategy(0, 1);
        ObjectParameterStrategy s4 = new HeapArrayStrategy(0, 1);

        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o3info = ObjectParameterInfo.create(3, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o4info = ObjectParameterInfo.create(4, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);

        long ret = invoker.invokeN5(ctx, function.getFunctionAddress(),
                0L, 0L, N5, 0L, 0L,
                4,
                o1, s1, o1info,
                o2, s2, o2info,
                o3, s3, o3info,
                o4, s4, o4info);

        assertEquals("incorrect value o1", 1, getNativeUnsignedLong(o1));
        assertEquals("incorrect value o2", 2, getNativeUnsignedLong(o2));
        assertEquals("incorrect value o3", 3, getNativeUnsignedLong(o3));
        assertEquals("incorrect value o4", 4, getNativeUnsignedLong(o4));
        assertEquals("incorrect return value", N1 + N2 + N3 + N4 + N5, unsigned(ret));
    }

    public static void invokeDONOO(Invoker invoker) {
        Function function = getFunction("invokeOONOO", Type.SLONG, Type.POINTER, Type.POINTER, Type.SLONG, Type.POINTER, Type.POINTER);
        CallContext ctx = getContext(Type.SLONG, Type.POINTER, Type.POINTER, Type.SLONG, Type.POINTER, Type.POINTER);

        UnitHelper.Address o1 = new UnitHelper.Address(IO.allocateMemory(8, true));
        putNativeUnsignedLong(o1, N1);
        Object o2 = newNativeUnsignedLongArray(N2);
        Object o3 = newNativeUnsignedLongArray(N3);
        Object o4 = newNativeUnsignedLongArray(N4);

        ObjectParameterStrategy s1 = new DirectStrategy();
        ObjectParameterStrategy s2 = new HeapArrayStrategy(0, 1);
        ObjectParameterStrategy s3 = new HeapArrayStrategy(0, 1);
        ObjectParameterStrategy s4 = new HeapArrayStrategy(0, 1);

        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o3info = ObjectParameterInfo.create(3, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o4info = ObjectParameterInfo.create(4, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);

        long ret = invoker.invokeN5(ctx, function.getFunctionAddress(),
                o1.address, 0L, N5, 0L, 0L,
                4,
                o1, s1, o1info,
                o2, s2, o2info,
                o3, s3, o3info,
                o4, s4, o4info);

        assertEquals("incorrect value o1", 1, getNativeUnsignedLong(o1));
        assertEquals("incorrect value o2", 2, getNativeUnsignedLong(o2));
        assertEquals("incorrect value o3", 3, getNativeUnsignedLong(o3));
        assertEquals("incorrect value o4", 4, getNativeUnsignedLong(o4));
        assertEquals("incorrect return value", N1 + N2 + N3 + N4 + N5, unsigned(ret));
    }

    public static void invokeOONOD(Invoker invoker) {
        Function function = getFunction("invokeOONOO", Type.SLONG, Type.POINTER, Type.POINTER, Type.SLONG, Type.POINTER, Type.POINTER);
        CallContext ctx = getContext(Type.SLONG, Type.POINTER, Type.POINTER, Type.SLONG, Type.POINTER, Type.POINTER);

        Object o1 = newNativeUnsignedLongArray(N1);
        Object o2 = newNativeUnsignedLongArray(N2);
        Object o3 = newNativeUnsignedLongArray(N3);
        UnitHelper.Address o4 = new UnitHelper.Address(IO.allocateMemory(8, true));
        putNativeUnsignedLong(o4, N4);

        ObjectParameterStrategy s1 = new HeapArrayStrategy(0, 1);
        ObjectParameterStrategy s2 = new HeapArrayStrategy(0, 1);
        ObjectParameterStrategy s3 = new HeapArrayStrategy(0, 1);
        ObjectParameterStrategy s4 = new DirectStrategy();

        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o3info = ObjectParameterInfo.create(3, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o4info = ObjectParameterInfo.create(4, ObjectParameterInfo.ARRAY,
                NATIVE_LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);

        long ret = invoker.invokeN5(ctx, function.getFunctionAddress(),
                0L, 0L, N5, 0L, o4.address,
                4,
                o1, s1, o1info,
                o2, s2, o2info,
                o3, s3, o3info,
                o4, s4, o4info);

        assertEquals("incorrect value o1", 1, getNativeUnsignedLong(o1));
        assertEquals("incorrect value o2", 2, getNativeUnsignedLong(o2));
        assertEquals("incorrect value o3", 3, getNativeUnsignedLong(o3));
        assertEquals("incorrect value o4", 4, getNativeUnsignedLong(o4));
        assertEquals("incorrect return value", N1 + N2 + N3 + N4 + N5, unsigned(ret));
    }


    @Test public void invokeNativeO() {
        invokeO(new NativeInvoker());
    }

    @Test public void invokeHeapO() {
        invokeO(new HeapInvoker());
    }

    @Test public void invokeNativeNO() {
        invokeNO(new NativeInvoker());
    }

    @Test public void invokeHeapNO() {
        invokeNO(new HeapInvoker());
    }

    @Test public void invokeNativeON() {
        invokeON(new NativeInvoker());
    }

    @Test public void invokeHeapON() {
        invokeON(new HeapInvoker());
    }

    @Test public void invokeNativeOO() {
        invokeOO(new NativeInvoker());
    }

    @Test public void invokeHeapOO() {
        invokeOO(new HeapInvoker());
    }

    @Test public void invokeNativeDO() {
        invokeDO(new NativeInvoker());
    }

    @Test public void invokeHeapDO() {
        invokeDO(new HeapInvoker());
    }

    @Test public void invokeNativeOD() {
        invokeOD(new NativeInvoker());
    }

    @Test public void invokeHeapOD() {
        invokeOD(new HeapInvoker());
    }

    @Test public void invokeNativeOONOO() {
        invokeOONOO(new NativeInvoker());
    }

    @Test public void invokeHeapOONOO() {
        invokeOONOO(new HeapInvoker());
    }

    @Test public void invokeNativeDONOO() {
        invokeDONOO(new NativeInvoker());
    }

    @Test public void invokeHeapDONOO() {
        invokeDONOO(new HeapInvoker());
    }

    @Test public void invokeNativeOONOD() {
        invokeOONOD(new NativeInvoker());
    }

    @Test public void invokeHeapOONOD() {
        invokeOONOD(new HeapInvoker());
    }

    public static boolean string_equals(Invoker invoker, String s1, String s2) {
        Function function = getFunction("string_equals", Type.SINT, Type.POINTER, Type.POINTER);
        CallContext ctx = getContext(Type.SINT, Type.POINTER, Type.POINTER);
        ByteBuffer s1Buffer = Charset.defaultCharset().encode(CharBuffer.wrap(s1));
        ByteBuffer s2Buffer  = Charset.defaultCharset().encode(CharBuffer.wrap(s2));

        ObjectParameterStrategy s1strategy = new HeapArrayStrategy(s1Buffer.arrayOffset(), s1Buffer.remaining());
        ObjectParameterStrategy s2strategy = new HeapArrayStrategy(s2Buffer.arrayOffset(), s2Buffer.remaining());
        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.BYTE, ObjectParameterInfo.IN | ObjectParameterInfo.NULTERMINATE);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.BYTE, ObjectParameterInfo.IN | ObjectParameterInfo.NULTERMINATE);

        long ret = invoker.invokeN2(ctx, function.getFunctionAddress(), 0, 0, 2,
                s1Buffer.array(), s1strategy, o1info, s2Buffer.array(), s2strategy, o2info);
        return ret != 0;
    }

    @Test public void string_equals_heap() {
        assertTrue("strings not equal", string_equals(new NativeInvoker(), "test", "test"));
    }
}
