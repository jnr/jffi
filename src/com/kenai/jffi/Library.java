/*
 * Copyright (C) 2008, 2009 Wayne Meissner
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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a native library
 */
public final class Library {
    /** A cache of opened libraries */
    private static final Map<String, WeakReference<Library>> cache
            = new ConcurrentHashMap<String, WeakReference<Library>>();
    /** A lock used to serialize all dlopen/dlsym calls */
    private static final Object lock = new Object();

    /** Stores the last error returned by a dlopen or dlsym call */
    private static final ThreadLocal<String> lastError = new ThreadLocal<String>();

    /** A handle to the current process */
    private static final class DefaultLibrary {
        private static final Library INSTANCE = openLibrary(null, LAZY | GLOBAL);
    }

    /** Perform  lazy  binding. Only resolve symbols as needed */
    public static final int LAZY   = Foreign.RTLD_LAZY;

    /** Resolve all symbols when loading the library */
    public static final int NOW    = Foreign.RTLD_NOW;

    /** Symbols in this library are not made available to other libraries */
    public static final int LOCAL  = Foreign.RTLD_LOCAL;

    /** All symbols in the library are made available to other libraries */
    public static final int GLOBAL = Foreign.RTLD_GLOBAL;

    /** The native dl/LoadLibrary handle */
    private final long handle;

    /** The name of this <tt>Library</tt> */
    private final String name;

    /** A handle to the foreign interface to keep it alive as long as this object is alive */
    private final Foreign foreign;

    /**
     * Internal wrapper around dlopen.
     *
     * If the library open fails, then this stores the native error in a thread
     * local variable for later retrieval.
     *
     * @param name The name of the library to open
     * @param flags The flags to pass to dlopen
     * @return The native handle for the opened library, or 0 if it failed to open.
     */
    private static long dlopen(Foreign foreign, String name, int flags) {
        try {
            return foreign.dlopen(name, flags);

        } catch (UnsatisfiedLinkError ex) {
            lastError.set(ex.getMessage());
            return 0L;
        }
    }

    /**
     * Gets a handle to the default library.
     *
     * @return A <tt>Library</tt> instance representing the default library.
     */
    public static final Library getDefault() {
        return DefaultLibrary.INSTANCE;
    }

    /**
     * Gets a handle for the named library.
     *
     * @param name The name or path of the library to open.
     * @param flags The library flags (e.g. <tt>LAZY, NOW, LOCAL, GLOBAL</tt>)
     * @return A <tt>Library</tt> instance representing the named library, or
     * <tt>null</tt> if the library could not be opened.
     */
    public static final Library getCachedInstance(String name, int flags) {
        if (name == null) {
            return getDefault();
        }
        WeakReference<Library> ref = cache.get(name);
        Library lib = ref != null ? ref.get() : null;
        if (lib != null) {
            return lib;
        }
        
        lib = openLibrary(name, flags);
        if (lib == null) {
            return null;
        }
        cache.put(name, new WeakReference<Library>(lib));
        
        return lib;
    }

    /**
     * Gets a handle for the named library.
     *
     * <b>Note</b> This will not cache the instance, nor will it return a cached 
     * instance.  Only use when you really need a new handle for the library.
     *
     * @param name The name or path of the library to open.
     * @param flags The library flags (e.g. <tt>LAZY, NOW, LOCAL, GLOBAL</tt>)
     * @return A <tt>Library</tt> instance representing the named library, or
     * null if the library cannot be opened.
     */
    public static final Library openLibrary(String name, int flags) {
        // dlopen on some OS does not like flags=0, so set to sensible defaults
        if (flags == 0) {
            flags = LAZY | LOCAL;
        }

        final Foreign foreign = Foreign.getInstance();
        final long address = dlopen(foreign, name, flags);

        return address != 0L ? new Library(foreign, name, address) : null;
    }
    
    private Library(Foreign foreign, String name, long address) {
        this.foreign = foreign;
        this.name = name;
        this.handle = address;
    }

    /**
     * Gets the address of a symbol within the <tt>Library</tt>.
     * 
     * @param name The name of the symbol to locate.
     * @return The address of the symbol within the current address space.
     */
    public final long getSymbolAddress(String name) {
        try {
            return foreign.dlsym(handle, name);

        } catch (UnsatisfiedLinkError ex) {
            lastError.set(foreign.dlerror());
            return 0;
        }
    }
    
    /**
     * Gets the current error string from dlopen/LoadLibrary.
     *
     * @return A <tt>String</tt> describing the last error.
     */
    public static final String getLastError() {
        String error = lastError.get();
        return error != null ? error : "unknown";
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            if (handle != 0L) {
                foreign.dlclose(handle);
            }
        } finally {
            super.finalize();
        }
    }
}
