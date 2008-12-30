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
/**
 * Very thin wrapper over dlopen/dlclose/dlsym
 */
package com.kenai.jffi;

public final class Library {
    private static final Object lock = new Object();
    private static final ThreadLocal<String> lastError = new ThreadLocal<String>();
    public static final int LAZY   = 0x00001;
    public static final int NOW    = 0x00002;
    public static final int LOCAL  = 0x00004;
    public static final int GLOBAL = 0x00008;

    /** The native dl/LoadLibrary handle */
    private final Address handle;

    Library(Address handle) {
        this.handle = handle;
    }

    public Library(String name, int flags) {
        long address;
        final Foreign foreign = Foreign.getInstance();
        synchronized (lock) {
            address = foreign.dlopen(name, flags);
            if (address == 0) {
                String error = String.format("Could not open [%s]: %s", name, foreign.dlerror());
                throw new UnsatisfiedLinkError(error);
            }
        }
        this.handle = new Address(address);
    }

    public final Address findSymbol(String name) {
        long address;
        final Foreign foreign = Foreign.getInstance();
        synchronized (lock) {
            address = foreign.dlsym(handle.nativeAddress(), name);
            if (address == 0) {
                lastError.set(Foreign.getInstance().dlerror());
                return null;
            }
        }
        return new Address(address);
    }

    public static final String lastError() {
        String error = lastError.get();
        return error != null ? error : "unknown";
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            if (!handle.isNull()) {
                Foreign.getInstance().dlclose(handle.longValue());
            }
        } finally {
            super.finalize();
        }
    }
}
