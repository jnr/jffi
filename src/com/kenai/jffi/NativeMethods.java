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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Utility class to register native methods on a class
 */
public final class NativeMethods {

    /**
     * Store a link from the class to the native method holder in a weak
     * hash map, so as long as the class remains alive, the native memory for the
     * structures remains alive.
     *
     * This doesn't seem to be neccessary on sun's jvm, but best do it to be safe.
     */
    private static final Map<Class, NativeMethods> registeredMethods
            = new WeakHashMap<Class, NativeMethods>();

    private final long memory;
    private final List<NativeMethod> methods;

    private NativeMethods(long memory, List<NativeMethod> methods) {
        this.memory = memory;
        this.methods = new ArrayList<NativeMethod>(methods);
    }

    /**
     * Registers the native methods for a class.
     *
     * @param clazz The java class to register the native methods for.
     * @param methods The list of methods to attach to the class.
     */
    public static synchronized final void register(Class clazz, List<NativeMethod> methods) {
        final long ptrSize = Platform.getPlatform().addressSize() / 8;
        final MemoryIO mm = MemoryIO.getInstance();

        //
        // Each JNINativeMethod struct is 3 pointers
        // i.e.
        //   typedef struct {
        //      char *name;
        //      char *signature;
        //      void *fnPtr;
        //   } JNINativeMethod;

        long memory = mm.allocateMemory(methods.size() * 3 * ptrSize, true);
        if (memory == 0L) {
            throw new OutOfMemoryError("could not allocate native memory");
        }

        NativeMethods nm = new NativeMethods(memory, methods);

        long off = 0;
        for (NativeMethod m : methods) {
            mm.putAddress(memory + off, m.name); off += ptrSize;
            mm.putAddress(memory + off, m.signature); off += ptrSize;
            mm.putAddress(memory + off, m.function); off += ptrSize;
        }

        if (Foreign.getInstance().registerNatives(clazz, memory, methods.size()) != Foreign.JNI_OK) {
            throw new RuntimeException("failed to register native methods");
        }

        registeredMethods.put(clazz, nm);
    }

    /**
     * Removes all native method attachments for the specified class.
     *
     * @param clazz The class to unregister the native methods on.
     */
    public static synchronized final void unregister(Class clazz) {
        if (!registeredMethods.containsKey(clazz)) {
            throw new IllegalArgumentException("methods were not registered on class via NativeMethods.register");
        }

        if (Foreign.getInstance().unregisterNatives(clazz) != Foreign.JNI_OK) {
            throw new RuntimeException("failed to unregister native methods");
        }
        
        registeredMethods.remove(clazz);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            MemoryIO.getInstance().freeMemory(memory);
        } finally {
            super.finalize();
        }
    }
}
