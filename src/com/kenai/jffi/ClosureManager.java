
package com.kenai.jffi;

import java.lang.reflect.Method;

public class ClosureManager {
    private static final long ADDRESS_MASK = Platform.getPlatform().addressMask();
    private static final class SingletonHolder {
        static final ClosureManager INSTANCE = new ClosureManager();
    }
    public static final ClosureManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private ClosureManager() { }

    private static final class Handle implements Closure.Handle {
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        final long handle;
        final long cbAddress;
        Handle(long handle) {
            this.handle = handle;
            cbAddress = IO.getAddress(handle);
        }

        public long getAddress() {
            return cbAddress;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                Foreign.getInstance().freeClosure(handle);
            } finally {
                super.finalize();
            }
        }
    }
    private static final class Proxy {
        static final Method METHOD = getMethod();
        final Closure closure;
        private static  final Method getMethod() {
            try {
                return Proxy.class.getDeclaredMethod("invoke", new Class[] { long.class, long.class });
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
        Proxy(Closure closure) {
            this.closure = closure;
        }
        void invoke(long retvalAddress, long paramAddress) {
            closure.invoke(new DirectBuffer(retvalAddress, paramAddress));
        }
        
    }
    public final Closure.Handle newClosure(Closure closure, Type returnType, Type[] parameterTypes, CallingConvention convention) {
        Proxy proxy = new Proxy(closure);
        int[] nativeParamTypes = new int[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            nativeParamTypes[i] = parameterTypes[i].value();
        }
        return new Handle(Foreign.getInstance().newClosure(proxy, Proxy.METHOD,
                returnType.value(), nativeParamTypes, 0));
    }
    private static final class DirectBuffer implements Closure.Buffer {
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        private static final int PARAM_SIZE = Platform.getPlatform().addressSize() / 8;
        private final long retval, parameters;

        public DirectBuffer(long retval, long parameters) {
            this.retval = retval;
            this.parameters = parameters;
        }

        @Deprecated
        public int getInt8(int index) {
            return getByte(index);
        }
        @Deprecated
        public int getInt16(int index) {
            return getShort(index);
        }
        @Deprecated
        public int getInt32(int index) {
            return getInt(index);
        }
        @Deprecated
        public long getInt64(int index) {
            return getLong(index);
        }
        @Deprecated
        public void setInt8Return(int value) {
            setByteReturn((byte) value);
        }
        @Deprecated
        public void setInt16Return(int value) {
            setShortReturn((short) value);
        }
        @Deprecated
        public void setInt32Return(int value) {
            setIntReturn(value);
        }
        @Deprecated
        public final void setInt64Return(long value) {
            setLongReturn(value);
        }
        public final byte getByte(int index) {
            return IO.getByte(IO.getAddress(parameters + (index * PARAM_SIZE)));
        }

        public final short getShort(int index) {
            return IO.getShort(IO.getAddress(parameters + (index * PARAM_SIZE)));
        }

        public final int getInt(int index) {
            return IO.getInt(IO.getAddress(parameters + (index * PARAM_SIZE)));
        }

        public final long getLong(int index) {
            return IO.getLong(IO.getAddress(parameters + (index * PARAM_SIZE)));
        }

        public final float getFloat(int index) {
            return IO.getFloat(IO.getAddress(parameters + (index * PARAM_SIZE)));
        }

        public final double getDouble(int index) {
            return IO.getDouble(IO.getAddress(parameters + (index * PARAM_SIZE)));
        }

        public final long getAddress(int index) {
            return IO.getAddress(IO.getAddress(parameters + (index * PARAM_SIZE))) & ADDRESS_MASK;
        }

        public final void setByteReturn(byte value) {
            IO.putByte(retval, value);
        }

        public final void setShortReturn(short value) {
            IO.putShort(retval, value);
        }

        public final void setIntReturn(int value) {
            IO.putInt(retval, value);
        }

        public final void setLongReturn(long value) {
            IO.putLong(retval, value);
        }

        public final void setFloatReturn(float value) {
            IO.putFloat(retval, value);
        }

        public final void setDoubleReturn(double value) {
            IO.putDouble(retval, value);
        }

        public final void setAddressReturn(long address) {
            IO.putAddress(retval, address);
        }
    }
}
