package com.kenai.jffi;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public final class ClosureMagazine {
    /** A handle to the foreign interface to keep it alive as long as this object is alive */
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final Foreign foreign;
    /** keep a reference to the call context, to avoid GC whilst the magazine is in use */
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final CallContext callContext;
    private final long magazineAddress;
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    ClosureMagazine(Foreign foreign, CallContext callContext, long magazineAddress) {
        this.foreign = foreign;
        this.callContext = callContext;
        this.magazineAddress = magazineAddress;
    }

    public Closure.Handle allocate(Object proxy) {
        long closureAddress = foreign.closureMagazineGet(magazineAddress, proxy);
        return closureAddress != 0L
            ? new Handle(this, closureAddress, MemoryIO.getInstance().getAddress(closureAddress))
            : null;
    }

    public void dispose() {
        if (magazineAddress != 0L && !disposed.getAndSet(true)) {
            foreign.freeClosureMagazine(magazineAddress);
        }
    }

    private static final class Handle implements Closure.Handle {
        private final ClosureMagazine magazine;
        private final long closureAddress, codeAddress;

        private Handle(ClosureMagazine magazine, long closureAddress, long codeAddress) {
            this.magazine = magazine;
            this.closureAddress = closureAddress;
            this.codeAddress = codeAddress;
        }

        public long getAddress() {
            return codeAddress;
        }

        public void setAutoRelease(boolean autorelease) {}

        public void dispose() { }

        public void free() {}
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
