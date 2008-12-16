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
 * Native parameter and return types.
 */
public enum NativeType {
    /* Note: These must match the native FFI types */
    VOID(0),
    INT(1),
    FLOAT(2),
    DOUBLE(3),
    LONGDOUBLE(4),
    UINT8(5),
    SINT8(6),
    UINT16(7),
    SINT16(8),
    UINT32(9),
    SINT32(10),
    UINT64(11),
    SINT64(12),
    STRUCT(13),
    POINTER(14);
    private final int value;

    private NativeType(int value) {
        this.value = value;
    }
    public int value() {
        return value;
    }
}
