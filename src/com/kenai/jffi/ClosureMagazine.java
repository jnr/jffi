
package com.kenai.jffi;

import java.lang.reflect.Method;

class ClosureMagazine {
    /**
     * Keep references to the return and parameter types so they do not get
     * garbage collected until the magazine does.
     */
    private final CallContext ctx;
    private final long magazine;

    ClosureMagazine(CallContext ctx, Method method) {
        this.ctx = ctx;
        this.magazine = Foreign.getInstance().newClosureMagazine(ctx.getAddress(), method);
    }

    long get(Object closure) {
        return Foreign.getInstance().closureMagazineGet(magazine, closure);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (magazine != 0) {
                Foreign.getInstance().freeClosureMagazine(magazine);
            }
        } finally {
            super.finalize();
        }
    }

    static final class Proxy {

        static final Method METHOD = getMethod();
        volatile Closure closure;
        /**
         * Keep references to the return and parameter types so they do not get
         * garbage collected until the closure does.
         */
        final CallContext callContext;

        /**
         * Gets the
         * @return
         */
        private static final Method getMethod() {
            try {
                return ClosureProxy.class.getDeclaredMethod("invoke", new Class[]{long.class, long.class});
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }

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
            closure.invoke(new DirectClosureBuffer(callContext, retvalAddress, paramAddress));
        }
    }
}
