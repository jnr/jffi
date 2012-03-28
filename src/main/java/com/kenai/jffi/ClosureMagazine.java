package com.kenai.jffi;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public final class ClosureMagazine {
    /** A handle to the foreign interface to keep it alive as long as this object is alive */
    private final Foreign foreign;
    /** keep a reference to the call context, to avoid GC whilst the magazine is in use */
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final CallContext callContext;
    private final long magazineAddress;
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    public static ClosureMagazine newClosureMagazine(CallContext callContext, Method method) {
        Foreign foreign = Foreign.getInstance();
        long magazine = foreign.newClosureMagazine(callContext.getAddress(), method,
                !Closure.Buffer.class.isAssignableFrom(method.getParameterTypes()[0]));
        if (magazine == 0L) {
            throw new RuntimeException("could not allocate new closure magazine");
        }

        return new ClosureMagazine(foreign, callContext, magazine);
    }

    private ClosureMagazine(Foreign foreign, CallContext callContext, long magazineAddress) {
        this.foreign = foreign;
        this.callContext = callContext;
        this.magazineAddress = magazineAddress;
    }

    public long allocate(Object proxy) {
        return foreign.closureMagazineGet(magazineAddress, proxy);
    }

    public void dispose() {
        if (magazineAddress != 0L && !disposed.getAndSet(true)) {
            foreign.freeClosureMagazine(magazineAddress);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (magazineAddress != 0L && !disposed.getAndSet(true)) {
                foreign.freeClosureMagazine(magazineAddress);
            }
        } catch (Throwable t) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "exception when freeing " + getClass() + ": %s", t.getLocalizedMessage());
        } finally {
            super.finalize();
        }
    }

}
