package com.kenai.jffi;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 */
public class InvokerTest {
    private static final MemoryIO IO = MemoryIO.getInstance();

    static Function getFunction(String name, Type returnType, Type... parameterTypes) {
        UnitHelper.Address fn = UnitHelper.findSymbol(name);
        return new Function(fn.address, returnType, parameterTypes);
    }

    static long getNativeLong(UnitHelper.Address ptr) {
        return Platform.getPlatform().longSize() == 32 ? IO.getInt(ptr.address) : IO.getLong(ptr.address);
    }

    static void putNativeLong(UnitHelper.Address ptr, long value) {
        if (Platform.getPlatform().longSize() == 32) {
            IO.putInt(ptr.address, (int) value);
        } else {
            IO.putLong(ptr.address, value);
        }
    }


    static class HeapArrayStrategy extends ObjectParameterStrategy {
        private int offset, length;

        HeapArrayStrategy(int offset, int length) {
            super(HEAP);
            this.offset = offset;
            this.length = length;
        }

        @Override
        public long getAddress(Object parameter) {
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
        public long getAddress(Object parameter) {
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
        long[] arr = { 0xdeadbeefL };
        ObjectParameterStrategy strategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);

        long ret = invoker.invokeN1OrN(function, 0, 1, arr, strategy, info);
        assertEquals("incorrect return value", 0xdeadbeefL, ret);
        assertEquals("incorrect array value", 0xfee1deadL, arr[0]);
    }

    public static void invokeNO(Invoker invoker) {
        Function function = getFunction("invokeNO", Type.SLONG, Type.SLONG, Type.POINTER);
        long[] arr = { 0xdeadbeefL };
        ObjectParameterStrategy strategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        long ret = invoker.invokeN2OrN(function, 0xfee1deadL, 0L, 1, arr, strategy, info);
        assertEquals("incorrect return value", 0xdeadbeefL, ret);
        assertEquals("incorrect array value", 0xfee1deadL, arr[0]);
    }

    public static void invokeON(Invoker invoker) {
        Function function = getFunction("invokeON", Type.SLONG, Type.POINTER, Type.SLONG);
        long[] arr = { 0xdeadbeefL };
        ObjectParameterStrategy strategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        long ret = invoker.invokeN2OrN(function, 0L, 0xfee1deadL, 1, arr, strategy, info);
        assertEquals("incorrect return value", 0xdeadbeefL, ret);
        assertEquals("incorrect array value", 0xfee1deadL, arr[0]);
    }

    public static void invokeOO(Invoker invoker) {
        Function function = getFunction("invokeOO", Type.SLONG, Type.POINTER, Type.POINTER);
        long[] arr1 = { 0xdeadbeefL };
        long[] arr2 = { 0xfee1deadL };
        ObjectParameterStrategy strategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        long ret = invoker.invokeN2OrN(function, 0L, 0L, 2, arr1, strategy, o1info, arr2, strategy, o2info);
        assertEquals("incorrect return value", 0xdeadbeefL + 0xfee1deadL, ret);
        assertEquals("incorrect array value", 0xfee1deadL, arr1[0]);
        assertEquals("incorrect array value", 0xdeadbeefL, arr2[0]);
    }

    public static void invokeDO(Invoker invoker) {
        Function function = getFunction("invokeOO", Type.SLONG, Type.POINTER, Type.POINTER);
        UnitHelper.Address o1 =  new UnitHelper.Address(IO.allocateMemory(8, true));
        putNativeLong(o1, 0xdeadbeefL);
        long[] o2 = { 0xfee1deadL };
        ObjectParameterStrategy s1 = new DirectStrategy();
        ObjectParameterStrategy s2 = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);

        long ret = invoker.invokeN2OrN(function, o1.address, 0L, 1, o1, s1, o1info, o2, s2, o2info);
        assertEquals("incorrect ptr value", 0xfee1deadL, getNativeLong(o1));
        assertEquals("incorrect array value", 0xdeadbeefL, o2[0]);
        assertEquals("incorrect return value", 0xdeadbeefL + 0xfee1deadL, ret);
    }

    public static void invokeOD(Invoker invoker) {
        Function function = getFunction("invokeOO", Type.SLONG, Type.POINTER, Type.POINTER);
        UnitHelper.Address ptr =  new UnitHelper.Address(IO.allocateMemory(8, true));
        putNativeLong(ptr, 0xfee1deadL);
        long[] array = { 0xdeadbeefL };
        ObjectParameterStrategy ptrStrategy = new DirectStrategy();
        ObjectParameterStrategy arrayStrategy = new HeapArrayStrategy(0, 1);
        ObjectParameterInfo o1info = ObjectParameterInfo.create(0, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);
        ObjectParameterInfo o2info = ObjectParameterInfo.create(1, ObjectParameterInfo.ARRAY,
                ObjectParameterInfo.LONG, ObjectParameterInfo.IN | ObjectParameterInfo.OUT);

        long ret = invoker.invokeN2OrN(function, 0L, ptr.address, 1, array, arrayStrategy, o1info, ptr, ptrStrategy, o2info);
        assertEquals("incorrect ptr value", 0xdeadbeefL, getNativeLong(ptr));
        assertEquals("incorrect array value", 0xfee1deadL, array[0]);
        assertEquals("incorrect return value", 0xdeadbeefL + 0xfee1deadL, ret);
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
}
