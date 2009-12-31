/*
 * Copyright (C) 2009 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kenai.jffi;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

final class ClosurePool {

    private final List<MagazineHolder> partial = new LinkedList<MagazineHolder>();
    private final List<MagazineHolder> full = new LinkedList<MagazineHolder>();
    private final Set<Magazine> magazines = new HashSet<Magazine>();
    //
    // Since the CallContext native handle is used by the native pool code
    // a strong reference to the call context needs to be kept.
    //
    private final CallContext callContext;

    ClosurePool(CallContext callContext) {
        this.callContext = callContext;
    }

    synchronized void recycle(Magazine magazine) {
        magazine.recycle();
        if (!magazine.isEmpty()) {
            MagazineHolder h = new MagazineHolder(this, magazine);
            if (magazine.isFull()) {
                full.add(h);
            } else {
                partial.add(h);
            }
        } else {
            // If the magazine was empty during recycling, it means all the closures
            // allocated from it set autorelease=false, so we cannot re-use it.
            // Let GC clean it up.
            magazines.remove(magazine);
        }
    }

    private synchronized MagazineHolder getMagazineHolder() {
        if (!partial.isEmpty()) {
            return partial.get(0);
        } else if (!full.isEmpty()) {
            MagazineHolder h = full.remove(0);
            partial.add(h);
            return h;
        }
        Magazine m = new Magazine(callContext);
        MagazineHolder h = new MagazineHolder(this, m);
        partial.add(h);
        magazines.add(m);
        return h;
    }

    public synchronized Closure.Handle newClosureHandle(Closure closure) {
        Magazine.Slot s = null;
        MagazineHolder h = null;
        do {
            h = getMagazineHolder();
            s = h.magazine.get();
            if (s == null) {
                partial.remove(0);
            }
        } while (s == null);
        s.proxy.closure = closure;
        return new Handle(s, h);
    }

        /**
     * Manages the lifecycle of a native closure.
     *
     * Implements {@link Closure.Handle} interface.
     */
    private static final class Handle implements Closure.Handle {
        /**
         * Keep references to the closure pool so it does not get garbage collected
         * until all closures using it do.
         */
        private final MagazineHolder holder;
        private final Magazine.Slot slot;

        private volatile boolean disposed = false;

        /**
         * Creates a new Handle to lifecycle manager the native closure.
         *
         * @param handle The address of the native closure structure.
         * @param pool The native pool the closure was allocated from.
         */
        Handle(Magazine.Slot slot, MagazineHolder holder) {
            this.slot = slot;
            this.holder = holder;
        }

        public long getAddress() {
            return slot.cbAddress;
        }

        public void setAutoRelease(boolean autorelease) {
            slot.autorelease = autorelease;
        }

        @Deprecated
        public void free() {
            dispose();
        }

        public synchronized void dispose() {
            if (disposed) {
                throw new IllegalStateException("closure already disposed");
            }
            disposed = true;
            slot.autorelease = true;
        }
    }

    private static final class Magazine {
        private static final int MAX_SLOTS = 200;

        /** Store a reference to the MemoryIO accessor here for easy access */
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();

        private final CallContext ctx;

        private final List<Slot> free = new ArrayList<Slot>(MAX_SLOTS);
        private final List<Slot> all = new ArrayList<Slot>(MAX_SLOTS);

        Magazine(CallContext ctx) {
            this.ctx = ctx;
        }

        Slot get() {
            if (!free.isEmpty()) {
                return free.remove(free.size() - 1);
            }

            return all.size() < MAX_SLOTS ? newSlot() : null;
        }

        private Slot newSlot() {
            Proxy proxy = new Proxy(ctx);
            long h = Foreign.getInstance().newClosure(proxy, Proxy.METHOD, ctx.nativeReturnType, ctx.nativeParameterTypes, ctx.flags);
            if (h == 0) {
                return null;
            }
            Slot s = new Slot(h, proxy);
            all.add(s);
            return s;
        }

        boolean isFull() {
            return free.size() == all.size();
        }

        boolean isEmpty() {
            return free.isEmpty();
        }

        void recycle() {
            free.clear();
            for (Slot s : all) {
                if (s.autorelease) {
                    s.proxy.closure = NULL_CLOSURE;
                    free.add(s);
                }
            }
            all.retainAll(free);
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                for (Slot s : all) {
                    if (s.autorelease) {
                        Foreign.getInstance().freeClosure(s.handle);
                    }

                }
            } finally {
                super.finalize();
            }
        }

        static final class Slot {
            /**
             * The address of the native closure structure.
             *
             * <b>Note:</b> This is <b>NOT</b> the code address, but a pointer to the structure
             * which contains the code address.
             */
            final long handle;

            /** The code trampoline address */
            final long cbAddress;

            final Proxy proxy;
            volatile boolean autorelease;

            public Slot(long handle, Proxy proxy) {
                this.handle = handle;
                this.proxy = proxy;
                this.autorelease = true;
                cbAddress = IO.getAddress(handle);
            }
        }
    }

    private static final class MagazineHolder {
        private final WeakReference<ClosurePool> poolref;
        private final Magazine magazine;

        public MagazineHolder(ClosurePool pool, Magazine magazine) {
            this.poolref = new WeakReference<ClosurePool>(pool);
            this.magazine = magazine;
        }


        @Override
        protected void finalize() throws Throwable {
            try {
                ClosurePool pool = poolref.get();
                if (pool != null) {
                    pool.recycle(magazine);
                }
            } finally {
                super.finalize();
            }
        }
    }

    /**
     * This is a proxy passed to the native code, to be called by the
     * native trampoline code.
     */
    static final class Proxy {
        static final Method METHOD = getMethod();
        /**
         * Keep references to the return and parameter types so they do not get
         * garbage collected until the closure does.
         */
        final CallContext callContext;

        volatile Closure closure;

        /**
         * Gets the
         * @return
         */
        private static final Method getMethod() {
            try {
                return Proxy.class.getDeclaredMethod("invoke", new Class[]{long.class, long.class});
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }

        Proxy(CallContext callContext) {
            this.closure = NULL_CLOSURE;
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
    private static final Closure NULL_CLOSURE = new Closure() {

        public void invoke(Buffer buffer) {
        }
    };
}
