/*
 * Copyright (C) 2008, 2009 Wayne Meissner
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
    /** The native void type */
    public static final Type VOID = builtin(Foreign.TYPE_VOID);

    /** The native float type */
    public static final Type FLOAT = builtin(Foreign.TYPE_FLOAT);

    /** The native double type */
    public static final Type DOUBLE = builtin(Foreign.TYPE_DOUBLE);

    /** The native long double type */
    public static final Type LONGDOUBLE = builtin(Foreign.TYPE_LONGDOUBLE);

    /** The native unsigned 8 bit integer type */
    public static final Type UINT8 = builtin(Foreign.TYPE_UINT8);

    /** The native signed 8 bit integer type */
    public static final Type SINT8 = builtin(Foreign.TYPE_SINT8);

    /** The native unsigned 16 bit integer type */
    public static final Type UINT16 = builtin(Foreign.TYPE_UINT16);

    /** The native signed 16 bit integer type */
    public static final Type SINT16 = builtin(Foreign.TYPE_SINT16);

    /** The native unsigned 32 bit integer type */
    public static final Type UINT32 = builtin(Foreign.TYPE_UINT32);

    /** The native signed 32 bit integer type */
    public static final Type SINT32 = builtin(Foreign.TYPE_SINT32);
    /** The native unsigned 64 bit integer type */
    public static final Type UINT64 = builtin(Foreign.TYPE_UINT64);

    /** The native signed 64 bit integer type */
    public static final Type SINT64 = builtin(Foreign.TYPE_SINT64);

    /** The native memory address type */
    public static final Type POINTER = builtin(Foreign.TYPE_POINTER);

    /** The native unsigned char type */
    public static final Type UCHAR = alias(Foreign.TYPE_UCHAR, UINT8);

    /** The native signed char type */
    public static final Type SCHAR = alias(Foreign.TYPE_SCHAR, SINT8);

    /** The native unsigned short integer type */
    public static final Type USHORT = alias(Foreign.TYPE_USHORT, UINT16, UINT32);

    /** The native signed short integer type */
    public static final Type SSHORT = alias(Foreign.TYPE_SSHORT, SINT16, SINT32);

    /** The native unsigned integer type */
    public static final Type UINT = alias(Foreign.TYPE_UINT, UINT32, UINT64);

    /** The native signed integer type */
    public static final Type SINT = alias(Foreign.TYPE_SINT, SINT32, SINT64);

    /** The native unsigned long integer type */
    public static final Type ULONG = alias(Foreign.TYPE_ULONG, UINT32, UINT64);

    /** The native signed long integer type */
    public static final Type SLONG = alias(Foreign.TYPE_SLONG, SINT32, SINT64);


    /*========================================================================*/
    /** The FFI type of this type */
    protected final int type;

    /** The size in bytes of this type */
    protected final int size;

    /** The minimum alignment of this type */
    protected final int align;

    /** The address of this type's ffi_type structure */
    protected final long handle;

    /*========================================================================*/
    /**
     * Creates a new <tt>Type</tt> object for the ffi_type structure address.
     *
     * @param handle The address of the ffi_type structure.
     */
    Type(long handle) {
        if (handle == 0L) {
            throw new NullPointerException("Invalid ffi_type handle");
        }
        this.handle = handle;
        this.type = Foreign.getInstance().getTypeType(handle);
        this.size = Foreign.getInstance().getTypeSize(handle);
        this.align = Foreign.getInstance().getTypeAlign(handle);
    }
    
    /**
     * Gets the FFI type enum value for this <tt>Type</tt>
     *
     * @return An integer representing the FFI type.
     */
    public int type() {
        return type;
    }

    /**
     * Gets the native address of the ffi_type struct for this <tt>Type</tt>
     *
     * @return  The address of the ffi_type structure
     */
    final long handle() {
        return handle;
    }

    /**
     * Gets the size of this type.
     *
     * @return The size of this type, in bytes.
     */
    public final int size() {
        return size;
    }

    /**
     * Gets the alignment of this type.
     *
     * @return The alignment of this type, in bytes.
     */
    public final int alignment() {
        return align;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Type) && ((Type) obj).handle == handle;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (int) (this.handle ^ (this.handle >>> 32));
        return hash;
    }

    /** 
     * Converts an array of <tt>Type</tt> objects into an array of pointers to
     * ffi_type structures.
     * 
     * @param types An array of <tt>Type</tt>  objects
     * @return An array of native ffi_type handles.
     */
    final static long[] nativeHandles(Type[] types) {

        long[] nativeTypes = new long[types.length];
        for (int i = 0; i < types.length; ++i) {
            nativeTypes[i] = types[i].handle();
        }

        return nativeTypes;
    }

    /**
     * Creates a <tt>Type</tt> instance for builtin types.
     *
     * @param type The builtin type enum.
     * @return A <tt>Type</tt> instance.
     */
    private static final Type builtin(int type) {
        return new Builtin(type);
    }

    /**
     * Creates an alias for a native type.
     * 
     * @param type The native type enum
     * @param existing The existing types that this type may alias to.
     * @return A Type instance representing the native type.
     */
    private static final Type alias(int type, Type... existing) {
        final long h = Foreign.getInstance().lookupBuiltinType(type);
        for (Type t : existing) {
            if (t.handle == h) {
                return t;
            }
        }
        
        return new Builtin(type);
    }

    /**
     * Types that are built-in to libffi.
     */
    static final class Builtin extends Type {
        private Builtin(int type) {
            super(Foreign.getInstance().lookupBuiltinType(type));
        }
    }
}
