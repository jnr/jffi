package com.kenai.jffi;

import java.lang.reflect.Method;

/**
 * This is a proxy passed to the native code, to be called by the
 * native trampoline code.
 */
final class ClosureProxy {

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
    private static final Method getMethod() {
        try {
            return ClosureProxy.class.getDeclaredMethod("invoke", new Class[]{long.class, long.class});
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    ClosureProxy(Closure closure, CallContext callContext) {
        super();
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
