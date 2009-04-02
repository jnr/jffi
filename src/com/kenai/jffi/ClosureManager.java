
package com.kenai.jffi;

import java.lang.reflect.Method;

/**
 * Allocates and manages the lifecycle of native closures (aka callbacks).
 */
public class ClosureManager {
    private static final long ADDRESS_MASK = Platform.getPlatform().addressMask();

    /** Holder class to do lazy allocation of the ClosureManager instance */
    private static final class SingletonHolder {
        static final ClosureManager INSTANCE = new ClosureManager();
    }

    /**
     * Gets the global instance of the <tt>ClosureManager</tt>
     *
     * @return An instance of a <tt>ClosureManager</tt>
     */
    public static final ClosureManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /** Constructs a ClosureManager */
    private ClosureManager() { }

    /**
     * Manages the lifecycle of a native closure.
     *
     * Implements {@link Closure.Handle} interface.
     */
    private static final class Handle implements Closure.Handle {
        /** Store a reference to the MemoryIO accessor here for easy access */
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();

        /**
         * The address of the native closure structure.
         *
         * <b>Note:</b> This is <b>NOT</b> the code address, but a pointer to the structure
         * which contains the code address.
         */
        final long handle;

        /** The code trampoline address */
        final long cbAddress;

        /** 
         * Keep references to the return and parameter types so they do not get
         * garbage collected until the closure does.
         */
        private final Type returnType;
        private final Type[] parameterTypes;

        /**
         * Creates a new Handle to lifecycle manager the native closure.
         *
         * @param handle The address of the native closure structure.
         */
        Handle(long handle, Type returnType, Type[] parameterTypes) {
            this.handle = handle;
            cbAddress = IO.getAddress(handle);
            this.returnType = returnType;
            this.parameterTypes = (Type[]) parameterTypes.clone();
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

    /**
     * This is a proxy passed to the native code, to be called by the
     * native trampoline code.
     */
    private static final class Proxy {
        static final Method METHOD = getMethod();
        final Closure closure;
        /**
         * Gets the
         * @return
         */
        private static  final Method getMethod() {
            try {
                return Proxy.class.getDeclaredMethod("invoke", new Class[] { long.class, long.class });
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Creates a new <tt>Proxy</tt> instance.
         *
         * @param closure
         */
        Proxy(Closure closure) {
            this.closure = closure;
        }

        /**
         * Invoked by the native closure trampoline to execute the java side of
         * the closure.
         *
         * @param retvalAddress The address of the native return value buffer
         * @param paramAddress The address of the native parameter buffer.
         */
        void invoke(long retvalAddress, long paramAddress) {
            closure.invoke(new DirectBuffer(retvalAddress, paramAddress));
        }
    }

    /**
     * Wraps a java object that implements the {@link Closure} interface in a
     * native closure.
     *
     * @param closure The java object to be called when the native closure is invoked.
     * @param returnType The return type of the closure.
     * @param parameterTypes The parameter types of the closure.
     * @param convention The calling convention of the closure.
     * @return A new {@link Closure.Handle} instance.
     */
    public final Closure.Handle newClosure(Closure closure, Type returnType, Type[] parameterTypes, CallingConvention convention) {
        Proxy proxy = new Proxy(closure);
        long[] nativeParamTypes = new long[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            nativeParamTypes[i] = parameterTypes[i].handle();
        }
        
        if (!(returnType instanceof Type.Builtin)) {
            throw new IllegalArgumentException("Unsupported return type " + returnType);
        }

        long handle = Foreign.getInstance().newClosure(proxy, Proxy.METHOD,
                returnType.handle(), nativeParamTypes, 0);
        if (handle == 0) {
            throw new RuntimeException("Failed to create native closure");
        }
        
        return new Handle(handle, returnType, parameterTypes);
    }

    /**
     * Implementation of the {@link Closure.Buffer} interface to read/write
     * parameter and return value data in native memory
     */
    private static final class DirectBuffer implements Closure.Buffer {
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        private static final int PARAM_SIZE = Platform.getPlatform().addressSize() / 8;
        private final long retval, parameters;

        public DirectBuffer(long retval, long parameters) {
            this.retval = retval;
            this.parameters = parameters;
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

        public final long getStruct(int index) {
            return IO.getAddress(parameters + (index * PARAM_SIZE));
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
