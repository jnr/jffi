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
public abstract class Type {
    /* Note: These must match the native FFI types */
    public static final Type VOID = new Builtin(0);
    public static final Type INT = new Builtin(1);
    public static final Type FLOAT = new Builtin(2);
    public static final Type DOUBLE = new Builtin(3);
    public static final Type LONGDOUBLE = new Builtin(4);
    public static final Type UINT8 = new Builtin(5);
    public static final Type SINT8 = new Builtin(6);
    public static final Type UINT16 = new Builtin(7);
    public static final Type SINT16 = new Builtin(8);
    public static final Type UINT32 = new Builtin(9);
    public static final Type SINT32 = new Builtin(10);
    public static final Type UINT64 = new Builtin(11);
    public static final Type SINT64 = new Builtin(12);
    public static final Type STRUCT = new Builtin(13);
    public static final Type POINTER = new Builtin(14);

    protected final int type;
    protected final long handle;

    Type(int type, long handle) {
        this.type = type;
        this.handle = handle;
    }
    
    public int value() {
        return type;
    }

    public int type() {
        return type;
    }

    final long handle() {
        return handle;
    }

    public static final class Builtin extends Type {
        private Builtin(int type) {
            super(type, Foreign.getInstance().lookupBuiltinType(type));
        }
    }
}
