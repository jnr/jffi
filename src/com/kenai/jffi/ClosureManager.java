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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Allocates and manages the lifecycle of native closures (aka callbacks)
 */
public class ClosureManager {

    /**
     * ClosurePool instances are linked via a SoftReference in the lookup map, so
     * when all closure instances that that were allocated from the ClosurePool have been
     * reclaimed, and there is memory pressure, the native closure pool can be freed.
     * This will allow the CallContext instance to also be collected if it is not
     * strongly referenced elsewhere, and ejected from the {@link CallContextCache}
     */
    private final Map<CallContext, Reference<ClosurePool>> poolMap = new WeakHashMap<CallContext, Reference<ClosurePool>>();

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
    public final Closure.Handle newClosure(Closure closure, CallContext callContext) {
        ClosurePool pool = getClosurePool(callContext);

        return pool.newClosureHandle(closure);
    }

    private final synchronized ClosurePool getClosurePool(CallContext callContext) {
        Reference<ClosurePool> ref = poolMap.get(callContext);
        ClosurePool pool;
        if (ref != null && (pool = ref.get()) != null) {
            return pool;
        }

        poolMap.put(callContext, new SoftReference<ClosurePool>(pool = new ClosurePool(callContext)));

        return pool;
    }
}
