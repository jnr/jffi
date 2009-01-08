/*
 * Copyright (C) 2008 Wayne Meissner
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a native library
 */
public final class Library {
    private static final Map<String, WeakReference<Library>> cache
            = new ConcurrentHashMap<String, WeakReference<Library>>();
    private static final Object lock = new Object();
    private static final ThreadLocal<String> lastError = new ThreadLocal<String>();

    private static final class DefaultLibrary {
        private static final Library INSTANCE = new Library(null, Library.LAZY);
    }
    public static final int LAZY   = 0x00001;
    public static final int NOW    = 0x00002;
    public static final int LOCAL  = 0x00004;
    public static final int GLOBAL = 0x00008;

    /** The native dl/LoadLibrary handle */
    private final long handle;
    /** The name of this <tt>Library</tt> */
    private final String name;
    
    private static final long dlopen(String name, int flags) {
        final Foreign foreign = Foreign.getInstance();
        synchronized (lock) {
            final long address = foreign.dlopen(name, flags);
            if (address == 0L) {
                lastError.set(foreign.dlerror());
            }
            return address;
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
        Library lib;
        if (ref != null && (lib = ref.get()) != null) {
            return lib;
        }
        final long address = dlopen(name, flags);
        if (address == 0L) {
            return null;
        }
        cache.put(name, new WeakReference<Library>(lib = new Library(name, address)));
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
     * @return A <tt>Library</tt> instance representing the named library.
     */
    public static final Library openLibrary(String name, int flags) {
        final long address = dlopen(name, flags);
        return address != 0L ? new Library(name, address) : null;
    }
    
    private Library(String name, long address) {
        this.name = name;
        this.handle = address;
    }

    @Deprecated
    public Library(String name, int flags) {
        this.name = name;
        this.handle = dlopen(name, flags);
        if (this.handle == 0L) {
            String error = String.format("Could not open [%s]: %s", name, getLastError());
            throw new UnsatisfiedLinkError(error);
        }
    }

    @Deprecated
    public final Address findSymbol(String name) {
        return new Address(getSymbolAddress(name));
    }
    /**
     * Gets the address of a symbol within the <tt>Library</tt>.
     * 
     * @param name The name of the symbol to locate.
     * @return The address of the symbol within the current address space.
     */
    public final long getSymbolAddress(String name) {
        final Foreign foreign = Foreign.getInstance();
        synchronized (lock) {
            final long address = foreign.dlsym(handle, name);
            if (address == 0L) {
                lastError.set(foreign.dlerror());
            }
            return address;
        }
    }

    @Deprecated
    public static final String lastError() {
        return getLastError();
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
                Foreign.getInstance().dlclose(handle);
            }
        } finally {
            super.finalize();
        }
    }
}
