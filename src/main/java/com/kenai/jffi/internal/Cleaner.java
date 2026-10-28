package com.kenai.jffi.internal;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs cleanup actions after the objects they are registered for become phantom reachable.
 *
 * <p>The actions run on the thread that registers the next object, not on a thread of their own. A cleaner thread
 * would hold the registered actions, whose classes are loaded by the same class loader as jffi, so that class loader
 * could never be unloaded.
 *
 * <p>As on a cleaner thread, the actions run one at a time, and exceptions they throw are ignored.
 */
public final class Cleaner {

    private Cleaner() {
        throw new UnsupportedOperationException();
    }

    private static final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    private static final Set<Cleanable> cleanables = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean cleaning = new AtomicBoolean();

    private static final class Cleanable extends PhantomReference<Object> implements Runnable {
        private final Runnable cleanup;

        Cleanable(Object object, Runnable cleanup) {
            super(object, queue);
            this.cleanup = cleanup;
        }

        @Override
        public void run() {
            if (cleanables.remove(this)) {
                clear();
                cleanup.run();
            }
        }
    }

    private static void clean() {
        // Some actions register objects themselves, ClosurePool's Recycler while holding the pool's lock. If those
        // registrations ran the remaining actions, the stack would grow with every queued action, and two threads
        // running actions at the same time could each hold the lock the other's next action needs.
        if (!cleaning.compareAndSet(false, true)) {
            return;
        }
        try {
            Reference<?> ref;
            while ((ref = queue.poll()) != null) {
                try {
                    ((Cleanable) ref).run();
                } catch (Throwable ignored) {
                    // the thread that registers an object shouldn't fail because another object's action did
                }
            }
        } finally {
            cleaning.set(false);
        }
    }

    public static Runnable register(Object object, Runnable cleanup) {
        clean();
        Cleanable cleanable = new Cleanable(object, cleanup);
        cleanables.add(cleanable);
        return cleanable;
    }

}
