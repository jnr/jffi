package com.kenai.jffi;

import com.kenai.jffi.internal.Cleaner;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
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
    private volatile int disposed;
    private static final AtomicIntegerFieldUpdater<ClosureMagazine> UPDATER = AtomicIntegerFieldUpdater.newUpdater(ClosureMagazine.class, "disposed");

    ClosureMagazine(Foreign foreign, CallContext callContext, long magazineAddress) {
        this.foreign = foreign;
        this.callContext = callContext;
        this.magazineAddress = magazineAddress;

        Cleaner.register(this, new Runnable() {
            @Override
            public void run() {
                try {
                    int disposed = UPDATER.getAndSet(ClosureMagazine.this, 1);
                    if (magazineAddress != 0L && disposed == 0) {
                        foreign.freeClosureMagazine(magazineAddress);
                    }
                } catch (Throwable t) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING,
                            "exception when freeing " + getClass() + ": %s", t.getLocalizedMessage());
                }
            }
        });
    }

    public Closure.Handle allocate(Object proxy) {
        long closureAddress = foreign.closureMagazineGet(magazineAddress, proxy);
        return closureAddress != 0L
            ? new Handle(this, closureAddress, MemoryIO.getInstance().getAddress(closureAddress))
            : null;
    }

    public void dispose() {
        int disposed = UPDATER.getAndSet(this, 1);
        if (magazineAddress != 0L && disposed == 0) {
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

}
