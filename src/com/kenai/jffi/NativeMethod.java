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

/**
 * Represents a native implementation of a method for a class
 */
public final class NativeMethod {
    private final MemoryIO io = MemoryIO.getInstance();
    final long function;
    final long name;
    final long signature;

    /**
     * Creates a new native method wrapper.
     *
     * @param address The address of the native method.
     * @param name The name of the java method.
     * @param signature The java signature.
     */
    public NativeMethod(long address, String name, String signature) {
        this.function = address;
        this.name = nativeString(io, name);
        this.signature = nativeString(io, signature);
    }

    private static final long nativeString(MemoryIO io, String s) {
        byte[] bytes = s.getBytes();

        long memory = io.allocateMemory(bytes.length + 1, false);
        if (memory == 0L) {
            throw new OutOfMemoryError("failed to allocate memory for string");
        }

        io.putZeroTerminatedByteArray(memory, bytes, 0, bytes.length);

        return memory;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (name != 0L) {
                io.freeMemory(name);
            }
            if (signature != 0L) {
                io.freeMemory(signature);
            }
        } finally {
            super.finalize();
        }
    }
}
