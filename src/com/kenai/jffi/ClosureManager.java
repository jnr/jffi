
package com.kenai.jffi;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Allocates and manages the lifecycle of native closures (aka callbacks).rm hs
 */
public class ClosureManager {
    private static final Object lock = new Object();

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

    public final Closure.Handle newClosure(Closure closure, CallContext ctx) {
        Proxy proxy = new Proxy(closure, ctx);

        long handle = 0;
        synchronized (lock) {
            handle = Foreign.getInstance().newClosure(proxy, Proxy.METHOD,
                ctx.nativeReturnType, ctx.nativeParameterTypes, ctx.flags);
        }
        if (handle == 0) {
            throw new RuntimeException("Failed to create native closure");
        }

        return new Handle(handle, ctx);
    }

    /**
     * Manages the lifecycle of a native closure.
     *
     * Implements {@link Closure.Handle} interface.
     */
    private static final class Handle implements Closure.Handle {
        /** Store a reference to the MemoryIO accessor here for easy access */
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();

        private final AtomicBoolean released = new AtomicBoolean(false);
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
         * Keep references to the return and parameter types so they do not get
         * garbage collected until the closure does.
         */
        private final CallContext ctx;

        /**
         * Creates a new Handle to lifecycle manager the native closure.
         *
         * @param handle The address of the native closure structure.
         */
        Handle(long handle, CallContext ctx) {
            this.handle = handle;
            cbAddress = IO.getAddress(handle);
            this.ctx = ctx;
        }

        public long getAddress() {
            return cbAddress;
        }

        public void setAutoRelease(boolean autorelease) {
            this.autorelease = autorelease;
        }

        public void free() { dispose(); }

        public void dispose() {
            if (released.getAndSet(true)) {
                throw new IllegalStateException("Closure already released");
            }
            synchronized (lock) {
                Foreign.getInstance().freeClosure(handle);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                if (autorelease && !released.getAndSet(true)) {
                    synchronized (lock) {
                        Foreign.getInstance().freeClosure(handle);
                    }
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
        final CallContext ctx;

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
        Proxy(Closure closure, CallContext ctx) {
            this.closure = closure;
            this.ctx = ctx;
        }

        /**
         * Invoked by the native closure trampoline to execute the java side of
         * the closure.
         *
         * @param retvalAddress The address of the native return value buffer
         * @param paramAddress The address of the native parameter buffer.
         */
        void invoke(long retvalAddress, long paramAddress) {
            closure.invoke(new DirectClosureBuffer(ctx, retvalAddress, paramAddress));
        }
    }
}
