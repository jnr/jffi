
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

        long handle = pool.newClosure(new ClosureProxy(closure, callContext));
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
        return ClosurePool.newInstance(callContext, ClosureProxy.METHOD);
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
}
