/*
 * Copyright (C) 2009 Wayne Meissner
 *
 * This file is part of jffi.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 
 * Alternatively, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kenai.jffi;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ClosurePool {

    private final Set<Magazine> magazines = Collections.synchronizedSet(new HashSet<Magazine>());
    private final ConcurrentLinkedQueue<Handle> freeQueue = new ConcurrentLinkedQueue<Handle>();
    private final ConcurrentLinkedQueue<Handle> partialQueue = new ConcurrentLinkedQueue<Handle>();

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
            useMagazine(magazine);
        } else {
            // If the magazine was empty during recycling, it means all the closures
            // allocated from it set autorelease=false, so we cannot re-use it.
            // Let GC clean it up.
            magazines.remove(magazine);
        }
    }

    void recycle(Magazine.Slot slot, MagazineHolder holder) {
        partialQueue.add(new Handle(slot, holder));
    }

    private void useMagazine(Magazine m) {
        MagazineHolder h = new MagazineHolder(this, m);

        ArrayList<Handle> handles = new ArrayList<Handle>();
        Magazine.Slot s;
        ConcurrentLinkedQueue<Handle> q = m.isFull() ? freeQueue : partialQueue;
        while ((s = m.get()) != null) {
            handles.add(new Handle(s, h));
        }

        q.addAll(handles);
    }

    public Closure.Handle newClosureHandle(Closure closure) {
        Handle h = partialQueue.poll();
        if (h == null) {
            h = freeQueue.poll();
        }
        if (h == null) {
            h = allocateNewHandle();
        }

        h.slot.proxy.closure = closure;

        return h;
    }

    private Handle allocateNewHandle() {
        Handle h;

        while ((h = partialQueue.poll()) == null && (h = freeQueue.poll()) == null) {
            Magazine m = new Magazine(callContext);
            useMagazine(m);
            magazines.add(m);
        }

        return h;
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
        final MagazineHolder holder;
        final Magazine.Slot slot;
        private volatile boolean disposed;

        /**
         * Creates a new Handle to lifecycle manager the native closure.
         *
         * @param slot THe magazine slot this handle belongs to
         * @param holder The magazine holder containing this handle
         */
        Handle(Magazine.Slot slot, MagazineHolder holder) {
            this.slot = slot;
            this.holder = holder;
        }

        public long getAddress() {
            if (disposed) {
                throw new RuntimeException("trying to access disposed closure handle");
            }
            return slot.codeAddress;
        }

        public void setAutoRelease(boolean autorelease) {
            if (!disposed) {
                slot.autorelease = autorelease;
            }
        }

        @Deprecated
        public void free() {
            dispose();
        }

        public synchronized void dispose() {
            if (!disposed) {
                disposed = true;
                slot.autorelease = true;
                slot.proxy.closure = NULL_CLOSURE;
                holder.pool.recycle(slot, holder);
            }
        }
    }

    private static final class Magazine {
        /** Store a reference to the MemoryIO accessor here for easy access */
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        /** A handle to the foreign interface to keep it alive as long as this object is alive */
        @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
        private final Foreign foreign = Foreign.getInstance();

        private final CallContext ctx;
        private final long magazine;
        private final Slot[] slots;
        private int next;
        private int freeCount;

        Magazine(CallContext ctx) {
            this.ctx = ctx;
            this.magazine = foreign.newClosureMagazine(ctx.getAddress(), Proxy.METHOD, false);
            ArrayList<Slot> slots = new ArrayList<Slot>();

            for (;;) {
                long h;
                Proxy proxy = new Proxy(ctx);
                if ((h = foreign.closureMagazineGet(magazine, proxy)) == 0) {
                    break;
                }

                Slot s = new Slot(h, proxy);
                slots.add(s);
            }

            this.slots = new Slot[slots.size()];
            slots.toArray(this.slots);
            next = 0;
            freeCount = this.slots.length;
        }

        Slot get() {
            while (freeCount > 0 && next < slots.length) {
                Slot s = slots[next++];
                if (s.autorelease) {
                    freeCount--;
                    return s;
                }
            }
            return null;
        }

        boolean isFull() {
            return slots.length == freeCount;
        }

        boolean isEmpty() {
            return freeCount < 1;
        }

        void recycle() {
            for (int i = 0; i < slots.length; i++) {
                Slot s = slots[i];
                if (s.autorelease) {
                    freeCount++;
                    s.proxy.closure = NULL_CLOSURE;
                }
            }
            next = 0;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                boolean release = true;
                //
                // If any of the closures allocated from this magazine set autorelease=false
                // then this magazine cannot be freed, so just let it leak
                //
                for (int i = 0; i < slots.length; i++) {
                    if (!slots[i].autorelease) {
                        release = false;
                        break;
                    }
                }

                if (magazine != 0 && release) {
                    foreign.freeClosureMagazine(magazine);
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
            final long codeAddress;

            final Proxy proxy;
            volatile boolean autorelease;

            public Slot(long handle, Proxy proxy) {
                this.handle = handle;
                this.proxy = proxy;
                this.autorelease = true;
                codeAddress = IO.getAddress(handle);
            }
        }
    }

    private static final class MagazineHolder {
        final ClosurePool pool;
        final Magazine magazine;

        public MagazineHolder(ClosurePool pool, Magazine magazine) {
            this.pool = pool;
            this.magazine = magazine;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                pool.recycle(magazine);
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
         * Gets the Method to be invoked by native code
         *
         * @return The method to be invoked by native code
         */
        private static Method getMethod() {
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
        public void invoke(long retvalAddress, long paramAddress) {
            closure.invoke(new DirectClosureBuffer(callContext, retvalAddress, paramAddress));
        }
    }
    private static final Closure NULL_CLOSURE = new Closure() {

        public void invoke(Buffer buffer) {
        }
    };
}
