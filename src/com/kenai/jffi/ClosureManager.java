
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
        Proxy proxy = new Proxy(closure, returnType, parameterTypes);

        long handle = Foreign.getInstance().newClosure(proxy, Proxy.METHOD,
                returnType.handle(), Type.nativeHandles(parameterTypes), 0);
        if (handle == 0) {
            throw new RuntimeException("Failed to create native closure");
        }

        return new Handle(handle, returnType, parameterTypes);
    }

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
         * Keep references to the return and parameter types so they do not get
         * garbage collected until the closure does.
         */
        final Type returnType;
        final Type[] parameterTypes;

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
         * @param closure The closure to call when this proxy is invoked
         * @param returnType The native return type of the closure
         * @param parameterTypes The parameterTypes of the closure
         */
        Proxy(Closure closure, Type returnType, Type[] parameterTypes) {
            this.closure = closure;
            this.returnType = returnType;
            this.parameterTypes = (Type[]) parameterTypes.clone();
        }

        /**
         * Invoked by the native closure trampoline to execute the java side of
         * the closure.
         *
         * @param retvalAddress The address of the native return value buffer
         * @param paramAddress The address of the native parameter buffer.
         */
        void invoke(long retvalAddress, long paramAddress) {
            closure.invoke(new DirectBuffer(returnType, parameterTypes, retvalAddress, paramAddress));
        }
    }

    /**
     * Implementation of the {@link Closure.Buffer} interface to read/write
     * parameter and return value data in native memory
     */
    private static final class DirectBuffer implements Closure.Buffer {
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        private static final NativeWordIO WordIO = NativeWordIO.getInstance();
        private static final int PARAM_SIZE = Platform.getPlatform().addressSize() / 8;
        private final long retval, parameters;

        /* Keep references to the return and parameter types to prevent garbage collection */
        private final Type returnType;
        private final Type[] parameterTypes;

        public DirectBuffer(Type returnType, Type[] parameterTypes, long retval, long parameters) {
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
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
            WordIO.put(retval, value);
        }

        public final void setShortReturn(short value) {
            WordIO.put(retval, value);
        }

        public final void setIntReturn(int value) {
            WordIO.put(retval, value);
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

        public void setStructReturn(long value) {
            IO.copyMemory(value, retval, returnType.size());
        }

        public void setStructReturn(byte[] data, int offset) {
            IO.putByteArray(retval, data, offset, returnType.size());
        }
    }

    /**
     * Reads annd writes data types that are smaller than the size of a native
     * long, as a native long for compatibility with FFI.
     */
    private static abstract class NativeWordIO {
        public static final NativeWordIO getInstance() {
            return Platform.getPlatform().addressSize() == 32
                    ? NativeWordIO32.INSTANCE : NativeWordIO64.INSTANCE;
        }

        /**
         * Writes a native long argument to native memory.
         *
         * @param address The address to write the value at
         * @param value The value to write.
         */
        abstract void put(long address, int value);

        /**
         * Reads a native long argument from native memory.
         * @param address The memory address to read the value from
         * @return An integer
         */
        abstract int get(long address);
    }

    private static final class NativeWordIO32 extends NativeWordIO {
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        static final NativeWordIO INSTANCE = new NativeWordIO32();

        void put(long address, int value) {
            IO.putInt(address, value);
        }

        int get(long address) {
            return IO.getInt(address);
        }
    }

    private static final class NativeWordIO64 extends NativeWordIO {

        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        static final NativeWordIO INSTANCE = new NativeWordIO64();

        void put(long address, int value) {
            IO.putLong(address, value);
        }

        int get(long address) {
            return (int) IO.getLong(address);
        }
    }
}
