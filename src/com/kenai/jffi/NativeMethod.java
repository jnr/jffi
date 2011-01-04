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
    final long function;
    final String name;
    final String signature;

    /**
     * Creates a new native method wrapper.
     *
     * @param address The address of the native method.
     * @param name The name of the java method.
     * @param signature The java signature.
     */
    public NativeMethod(long address, String name, String signature) {
        this.function = address;
        this.name = name;
        this.signature = signature;
    }
}
