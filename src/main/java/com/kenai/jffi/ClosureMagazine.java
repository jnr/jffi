package com.kenai.jffi;

import com.kenai.jffi.internal.Cleaner;

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
    private final Disposer disposer;

    ClosureMagazine(Foreign foreign, CallContext callContext, long magazineAddress) {
        this.foreign = foreign;
        this.callContext = callContext;
        this.magazineAddress = magazineAddress;

        this.disposer = new Disposer(foreign, magazineAddress);

        Cleaner.register(this, disposer);
    }

    public Closure.Handle allocate(Object proxy) {
        long closureAddress = foreign.closureMagazineGet(magazineAddress, proxy);
        return closureAddress != 0L
            ? new Handle(this, closureAddress, MemoryIO.getInstance().getAddress(closureAddress))
            : null;
    }

    public void dispose() {
        disposer.run();
    }

    /**
     * Frees the native magazine, at most once. It must not reference the {@link ClosureMagazine}, or the magazine
     * would stay reachable from its own cleanup action and never be freed.
     */
    private static final class Disposer implements Runnable {
        private final Foreign foreign;
        private final long magazineAddress;
        private final AtomicBoolean disposed = new AtomicBoolean();

        Disposer(Foreign foreign, long magazineAddress) {
            this.foreign = foreign;
            this.magazineAddress = magazineAddress;
        }

        @Override
        public void run() {
            try {
                if (magazineAddress != 0L && disposed.compareAndSet(false, true)) {
                    foreign.freeClosureMagazine(magazineAddress);
                }
            } catch (Throwable t) {
                Logger.getLogger(ClosureMagazine.class.getName()).log(Level.WARNING,
                        "exception when freeing " + ClosureMagazine.class + ": %s", t.getLocalizedMessage());
            }
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
