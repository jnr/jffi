
package com.kenai.jffi;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Allocates and manages the lifecycle of native closures (aka callbacks)
 */
public class ClosureManager {

    /**
     * ClosurePool instances are linked via a SoftReference in the lookup map, so
     * when all closure instances that that were allocated from the ClosurePool have been
     * reclaimed, and there is memory pressure, the native closure pool can be freed.
     * This will allow the CallContext instance to also be collected if it is not
     * strongly referenced elsewhere, and ejected from the {@link CallContextCache}
     */
    private final Map<CallContext, Reference<ClosurePool>> poolMap = new WeakHashMap<CallContext, Reference<ClosurePool>>();

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
        return newClosure(closure, CallContextCache.getInstance().getCallContext(returnType, parameterTypes, convention));
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
    public final Closure.Handle newClosure(Closure closure, CallContext callContext) {
        ClosurePool pool = getClosurePool(callContext);

        long handle = pool.newClosure(new Proxy(closure, callContext));
        if (handle == 0) {
            throw new RuntimeException("Failed to create native closure");
        }
        try {
            return new Handle(handle, pool);
        } catch (Throwable t) {
            pool.releaseClosure(handle);
            throw new RuntimeException(t);
        }
    }

    private final synchronized ClosurePool getClosurePool(CallContext callContext) {
        Reference<ClosurePool> ref = poolMap.get(callContext);
        ClosurePool pool;
        if (ref != null && (pool = ref.get()) != null) {
            return pool;
        }

        poolMap.put(callContext, new SoftReference<ClosurePool>(pool = newClosurePool(callContext)));

        return pool;
    }

    private final ClosurePool newClosurePool(CallContext callContext) {
        return ClosurePool.newInstance(callContext, Proxy.METHOD);
    }

    private static final class ClosurePool {
        private final long handle;

        //
        // Since the CallContext native handle is used by the native pool code
        // a strong reference to the call context needs to be kept.
        //
        private final CallContext callContext; 

        static ClosurePool newInstance(CallContext callContext, Method m) {
            long h = Foreign.getInstance().newClosurePool(callContext.getAddress(), m);
            try {
                return new ClosurePool(h, callContext);
            } catch (Throwable t) {
                Foreign.getInstance().freeClosurePool(h);
                throw new RuntimeException(t);
            }
        }

        private ClosurePool(long handle, CallContext callContext) {
            this.handle = handle;
            this.callContext = callContext;
        }

        synchronized long newClosure(Object proxy) {
            return Foreign.getInstance().allocateClosure(handle, proxy);
        }
        
        synchronized void releaseClosure(long handle) {
            Foreign.getInstance().releaseClosure(handle);
        }

        
        @Override
        protected void finalize() throws Throwable {
            try {
                Foreign.getInstance().freeClosurePool(handle);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            } finally {
                super.finalize();
            }
        }
    }

    /**
     * Manages the lifecycle of a native closure.
     *
     * Implements {@link Closure.Handle} interface.
     */
    private static final class Handle implements Closure.Handle {
        /** Store a reference to the MemoryIO accessor here for easy access */
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();

        private volatile boolean disposed = false;
        private volatile boolean autorelease = true;

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
         * Keep references to the closure pool so it does not get garbage collected
         * until all closures using it do.
         */
        private final ClosurePool pool;
        
        /**
         * Creates a new Handle to lifecycle manager the native closure.
         *
         * @param handle The address of the native closure structure.
         * @param pool The native pool the closure was allocated from.
         */
        Handle(long handle, ClosurePool pool) {
            this.handle = handle;
            this.pool = pool;
            cbAddress = IO.getAddress(handle);
        }

        public long getAddress() {
            return cbAddress;
        }

        public void setAutoRelease(boolean autorelease) {
            this.autorelease = autorelease;
        }

        @Deprecated
        public void free() {
            dispose();
        }

        public synchronized void dispose() {
            if (disposed) {
                throw new IllegalStateException("closure already freed");
            }
            disposed = true;
            pool.releaseClosure(handle);
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                if (autorelease && !disposed) {
                    pool.releaseClosure(handle);
                }
            } catch (Throwable t) {
                t.printStackTrace(System.err);
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
        final CallContext callContext;

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
        Proxy(Closure closure, CallContext callContext) {
            this.closure = closure;
            this.callContext = callContext;
        }

        /**
         * Invoked by the native closure trampoline to execute the java side of
         * the closure.
         *
         * @param retvalAddress The address of the native return value buffer
         * @param paramAddress The address of the native parameter buffer.
         */
        void invoke(long retvalAddress, long paramAddress) {
            closure.invoke(new DirectBuffer(callContext, retvalAddress, paramAddress));
        }
    }

    /**
     * Implementation of the {@link Closure.Buffer} interface to read/write
     * parameter and return value data in native memory
     */
    private static final class DirectBuffer implements Closure.Buffer {
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        private static final long ADDRESS_MASK = Platform.getPlatform().addressMask();
        private static final NativeWordIO WordIO = NativeWordIO.getInstance();
        private static final int PARAM_SIZE = Platform.getPlatform().addressSize() / 8;
        private final long retval, parameters;

        /* Keep references to the return and parameter types to prevent garbage collection */
        private final CallContext callContext;

        public DirectBuffer(CallContext callContext, long retval, long parameters) {
            this.callContext = callContext;
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
            IO.copyMemory(value, retval, callContext.getReturnType().size());
        }

        public void setStructReturn(byte[] data, int offset) {
            IO.putByteArray(retval, data, offset, callContext.getReturnType().size());
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
