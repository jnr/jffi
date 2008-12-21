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

/**
 * Defines the return type and parameters of a native function call
 */
public final class CallContext {

    private final Address handle;
    public CallContext(Type returnType, Type[] paramTypes) {
        int[] nativeParamTypes = new int[paramTypes.length];
        for (int i = 0; i < paramTypes.length; ++i) {
            nativeParamTypes[i] = paramTypes[i].value();
        }
        long h = Foreign.getForeign().newCallContext(returnType.value(), nativeParamTypes, 0);
        handle = new Address(h);
    }

    long getAddress() {
        return handle.nativeAddress();
    }
}
