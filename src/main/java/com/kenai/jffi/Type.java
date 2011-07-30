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

import java.util.List;

/**
 * Native parameter and return types.
 */
public abstract class Type {
    /** The native void type */
    public static final Type VOID = builtin(NativeType.VOID);

    /** The native float type */
    public static final Type FLOAT = builtin(NativeType.FLOAT);

    /** The native double type */
    public static final Type DOUBLE = builtin(NativeType.DOUBLE);

    /** The native long double type */
    public static final Type LONGDOUBLE = builtin(NativeType.LONGDOUBLE);

    /** The native unsigned 8 bit integer type */
    public static final Type UINT8 = builtin(NativeType.UINT8);

    /** The native signed 8 bit integer type */
    public static final Type SINT8 = builtin(NativeType.SINT8);

    /** The native unsigned 16 bit integer type */
    public static final Type UINT16 = builtin(NativeType.UINT16);

    /** The native signed 16 bit integer type */
    public static final Type SINT16 = builtin(NativeType.SINT16);

    /** The native unsigned 32 bit integer type */
    public static final Type UINT32 = builtin(NativeType.UINT32);

    /** The native signed 32 bit integer type */
    public static final Type SINT32 = builtin(NativeType.SINT32);
    /** The native unsigned 64 bit integer type */
    public static final Type UINT64 = builtin(NativeType.UINT64);

    /** The native signed 64 bit integer type */
    public static final Type SINT64 = builtin(NativeType.SINT64);

    /** The native memory address type */
    public static final Type POINTER = builtin(NativeType.POINTER);

    /** The native unsigned char type */
    public static final Type UCHAR = UINT8;

    /** The native signed char type */
    public static final Type SCHAR = SINT8;

    /** The native unsigned short integer type */
    public static final Type USHORT = UINT16;

    /** The native signed short integer type */
    public static final Type SSHORT = SINT16;

    /** The native unsigned integer type */
    public static final Type UINT = UINT32;

    /** The native signed integer type */
    public static final Type SINT = SINT32;

    /** The native unsigned long integer type */
    public static final Type ULONG = builtin(NativeType.ULONG);

    /** The native signed long integer type */
    public static final Type SLONG = builtin(NativeType.SLONG);

    /** The native unsigned long long integer type */
    public static final Type ULONG_LONG = UINT64;

    /** The native signed long long integer type */
    public static final Type SLONG_LONG = SINT64;
    
    /**
     * Gets the FFI type enum value for this <tt>Type</tt>
     *
     * @return An integer representing the FFI type.
     */
    public abstract int type();
    /**
     * Gets the native address of the ffi_type struct for this <tt>Type</tt>
     *
     * @return  The address of the ffi_type structure
     */
    abstract long handle();

    /**
     * Gets the size of this type.
     *
     * @return The size of this type, in bytes.
     */
    public abstract int size();

    /**
     * Gets the alignment of this type.
     *
     * @return The alignment of this type, in bytes.
     */
    public abstract int alignment();

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Type) && ((Type) obj).handle() == handle();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (int) (this.handle() ^ (this.handle() >>> 32));
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
     * Converts a list of <tt>Type</tt> objects into an array of pointers to
     * ffi_type structures.
     *
     * @param types A list of <tt>Type</tt> objects
     * @return An array of native ffi_type handles.
     */
    final static long[] nativeHandles(List<Type> types) {

        long[] nativeTypes = new long[types.size()];
        for (int i = 0; i < nativeTypes.length; ++i) {
            nativeTypes[i] = types.get(i).handle();
        }

        return nativeTypes;
    }


    /**
     * Creates a <tt>Type</tt> instance for builtin types.
     *
     * @param type The builtin type enum.
     * @return A <tt>Type</tt> instance.
     */
    private static final Type builtin(NativeType nativeType) {
        return new Builtin(nativeType);
    }

    /**
     * Types that are built-in to libffi.
     */
    static final class Builtin extends Type {
        private final NativeType nativeType;

        private Builtin(NativeType nativeType) {
            this.nativeType = nativeType;
        }
        
        public final int type() {
            return BuiltinTypeInfo.find(nativeType).type;
        }
        
        public final long handle() {
            return BuiltinTypeInfo.find(nativeType).handle;
        }
        
        public final int size() {
            return BuiltinTypeInfo.find(nativeType).size;
        }

        public final int alignment() {
            return BuiltinTypeInfo.find(nativeType).alignment;
        }
    }

    /**
     * This is a lazy loaded cache of builtin type info, so we can still have
     * Type.VOID as a public static variable without it causing the
     * native library to load.
     */
    private static final class BuiltinTypeInfo {
        public static final BuiltinTypeInfo[] typeMap;

        /** The FFI type of this type */
        final int type;
        /** The size in bytes of this type */
        final int size;
        /** The minimum alignment of this type */
        final int alignment;
        /** The address of this type's ffi_type structure */
        final long handle;

        static {
            NativeType[] nativeTypes = NativeType.values();
            typeMap = new BuiltinTypeInfo[nativeTypes.length];
            for (int i = 0; i < typeMap.length; ++i) {
                long h = Foreign.getInstance().lookupBuiltinType(nativeTypes[i].ffiType);
                if (h == 0L) {
                    throw new RuntimeException("invalid native type " + nativeTypes[i]);
                }
                
                typeMap[i] = new BuiltinTypeInfo(h);
            }
        }

        static final BuiltinTypeInfo find(NativeType t) {
            return typeMap[t.ordinal()];
        }

        private BuiltinTypeInfo(long handle) {
            if (handle == 0L) {
                throw new NullPointerException("null ffi_type handle");
            }
            this.handle = handle;
            this.type = Foreign.getInstance().getTypeType(handle);
            this.size = Foreign.getInstance().getTypeSize(handle);
            this.alignment = Foreign.getInstance().getTypeAlign(handle);
        }
    }
}
