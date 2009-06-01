/*
 * Copyright (C) 2007, 2008 Wayne Meissner
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
 * Interface to  the foreign function interface.
 */
package com.kenai.jffi;

import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;

final class Foreign {
    private static final class SingletonHolder {
        static {
            Init.init();
        }
        private static final Foreign INSTANCE = new Foreign();
    }
    public static final Foreign getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private Foreign() {}
    public final static int VERSION_MAJOR = 0;
    public final static int VERSION_MINOR = 5;
    public final static int VERSION_MICRO = 0;

    public final static int TYPE_VOID = 0;
    public final static int TYPE_FLOAT = 2;
    public final static int TYPE_DOUBLE = 3;
    public final static int TYPE_LONGDOUBLE = 4;
    public final static int TYPE_UINT8 = 5;
    public final static int TYPE_SINT8 = 6;
    public final static int TYPE_UINT16 = 7;
    public final static int TYPE_SINT16 = 8;
    public final static int TYPE_UINT32 = 9;
    public final static int TYPE_SINT32 = 10;
    public final static int TYPE_UINT64 = 11;
    public final static int TYPE_SINT64 = 12;
    public final static int TYPE_STRUCT = 13;
    public final static int TYPE_POINTER = 14;

    public final static int TYPE_UCHAR = 101;
    public final static int TYPE_SCHAR = 102;
    public final static int TYPE_USHORT = 103;
    public final static int TYPE_SSHORT = 104;
    public final static int TYPE_UINT = 105;
    public final static int TYPE_SINT = 106;
    public final static int TYPE_ULONG = 107;
    public final static int TYPE_SLONG = 108;

    /** Perform  lazy  binding. Only resolve symbols as needed */
    public static final int RTLD_LAZY   = 0x00001;

    /** Resolve all symbols when loading the library */
    public static final int RTLD_NOW    = 0x00002;

    /** Symbols in this library are not made availabl to other libraries */
    public static final int RTLD_LOCAL  = 0x00004;

    /** All symbols in the library are made available to other libraries */
    public static final int RTLD_GLOBAL = 0x00008;

    /**
     * Gets the native stub library version.
     *
     * @return The version in the form of (VERSION_MAJOR << 16 | VERSION_MINOR << 8 | VERSION_MICRO)
     */
    final native int getVersion();

    /**
     * Opens a dynamic library.
     *
     * This is a very thin wrapper around the native dlopen(3) call.
     *
     * @param name The name of the dynamic library to open.  Pass null to get a
     * handle to the current process.
     * @param flags The flags to dlopen.  A bitmask of {@link RTLD_LAZY}, {@link RTLD_NOW},
     * {@link RTLD_LOCAL}, {@link RTLD_GLOBAL}
     * @return A native handle to the dynamic library.
     */
    final native long dlopen(String name, int flags);

    /**
     * Closes a dynamic library opened by {@link dlopen}.
     *
     * @param handle The dynamic library handle returned by {@dlopen}
     */
    final native void dlclose(long handle);

    /**
     * Locates the memory address of a dynamic library symbol.
     *
     * @param handle A dynamic library handle obtained from {@dlopen}
     * @param name The name of the symbol.
     * @return The address where the symbol in loaded in memory.
     */
    final native long dlsym(long handle, String name);

    /**
     * Gets the last error raised by {@dlopen} or {@dlsym}
     *
     * @return The error string.
     */
    final native String dlerror();

    final native long allocateMemory(long size, boolean clear);
    final native void freeMemory(long address);

    final native long newFunction(long address, long returnType, long[] paramTypes, int convention);
    final native void freeFunction(long handle);
    final native boolean isRawParameterPackingEnabled();
    final native int getFunctionRawParameterSize(long handle);
    final native int getLastError();
    final native long newClosure(Object proxy, Method closureMethod, long returnType, long[] paramTypes, int convention);
    final native void freeClosure(long handle);

    /**
     * Gets the address of the ffi_type structure for the builtin type
     *
     * @param type The FFI type enum value
     * @return The address of the ffi_type struct for this type, or <tt>null</tt>
     */
    final native long lookupBuiltinType(int type);

    /**
     * Gets the native size of the type
     *
     * @param handle Address of the type structure
     * @return The native size of the type
     */
    final native int getTypeSize(long handle);

    /**
     * Gets the minimum required alignment of the FFI type
     *
     * @param handle Address of the type structure
     * @return The minimum required alignment
     */
    final native int getTypeAlign(long handle);

    /**
     * Gets the primitive type enum for the FFI type
     *
     * @param handle Address of the type structure
     * @return The builtin primitive type of the type structure
     */
    final native int getTypeType(long handle);

    /**
     * Allocates a new FFI struct or union layout
     *
     * @param fields An array of ffi_type pointers desccribing the fields of the struct
     * @param isUnion If true, then fields are all positioned at offset=0, else
     * fiels are sequentially positioned.
     * @return The native address of the ffi_type structure for the new struct layout
     */
    final native long newStruct(long[] fields, boolean isUnion);

    /**
     * Frees a FFI struct handle allocated via {@linkl #newStruct}.
     *
     * @param handle The FFI struct handle
     */
    final native void freeStruct(long handle);

    /**
     * Invokes a function with no arguments, and returns a 32 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @return A 32 bit integer value.
     */
    final native int invokeVrI(long functionContext);

    /**
     * Invokes a function with one integer argument, and returns a 32 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    final native int invokeIrI(long functionContext, int arg1);

    /**
     * Invokes a function with two integer arguments, and returns a 32 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    final native int invokeIIrI(long functionContext, int arg1, int arg2);

    /**
     * Invokes a function with three integer arguments, and returns a 32 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @param arg3 The third 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    final native int invokeIIIrI(long functionContext, int arg1, int arg2, int arg3);
    
    /**
     * Invokes a function with no arguments, and returns a 64 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @return A 64 bit integer value.
     */
    final native long invokeVrL(long function);

    /**
     * Invokes a function with one 64 bit integer argument, and returns a 64 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    final native long invokeLrL(long function, long arg1);

    /**
     * Invokes a function with two 64 bit integer arguments, and returns a 64 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 64 bit integer argument.
     * @param arg2 The second 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    final native long invokeLLrL(long function, long arg1, long arg2);

    /**
     * Invokes a function with three 64 bit integer arguments, and returns a 64 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 64 bit integer argument.
     * @param arg2 The second 64 bit integer argument.
     * @param arg3 The third 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    final native long invokeLLLrL(long function, long arg1, long arg2, long arg3);

    /**
     * Invokes a function that returns a 32 bit integer.
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param buffer A byte array containing the aguments to the function.
     * @return A 32 bit integer value.
     */
    final native int invokeArrayReturnInt(long function, byte[] buffer);

    /**
     * Invokes a function that returns a 64 bit integer.
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param buffer A byte array containing the aguments to the function.
     * @return A 64 bit integer value.
     */
    final native long invokeArrayReturnLong(long function, byte[] buffer);

    /**
     * Invokes a function that returns a 32 bit floating point value.
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param buffer A byte array containing the aguments to the function.
     * @return A 32 bit floating point value.
     */
    final native float invokeArrayReturnFloat(long function, byte[] buffer);

    /**
     * Invokes a function that returns a 64 bit floating point value.
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param buffer A byte array containing the aguments to the function.
     * @return A 64 bit floating point value.
     */
    final native double invokeArrayReturnDouble(long function, byte[] buffer);

    /**
     * Invokes a function and pack the return value into a byte array.
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param buffer A byte array containing the aguments to the function.
     */
    final native void invokeArrayReturnStruct(long function, byte[] paramBuffer, byte[] returnBuffer, int offset);

    /**
     * Invokes a function that returns a java object.
     *
     * This is only useful when calling JNI functions directly.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param buffer A byte array containing the aguments to the function.
     */
    final native Object invokeArrayWithObjectsReturnObject(long function, byte[] paramBuffer,
            int objectCount, int[] objectInfo, Object[] objects);

    /* ---------------------------------------------------------------------- */
    final native int invokeArrayWithObjectsInt32(long function, byte[] buffer, int objectCount, int[] objectInfo, Object[] objects);
    final native long invokeArrayWithObjectsInt64(long function, byte[] buffer, int objectCount, int[] objectInfo, Object[] objects);
    final native float invokeArrayWithObjectsFloat(long function, byte[] buffer, int objectCount, int[] objectInfo, Object[] objects);
    final native double invokeArrayWithObjectsDouble(long function, byte[] buffer, int objectCount, int[] objectInfo, Object[] objects);
    final native void invokeArrayWithObjectsReturnStruct(long function, byte[] buffer, int objectCount, int[] objectInfo, Object[] objects,
            byte[] returnBuffer, int returnBufferOffset);
    /* ---------------------------------------------------------------------- */
    final native int invokeArrayO1Int32(long function, byte[] buffer, Object o1, int o1Info, int o1off, int o1len);
    final native int invokeArrayO2Int32(long function, byte[] buffer, Object o1, int o1Info, int o1off, int o1len,
            Object o2, int o2info, int o2off, int o2len);

    /* ---------------------------------------------------------------------- */

    /**
     * Invokes a function, with the parameters loaded into native memory buffers,
     * and the function result is stored in a native memory buffer.
     *
     * @param functionContext The address of the function context structure from {@link #newFunction}.
     * @param returnBuffer The address of the native buffer to place the result
     * of the function call in.
     * @param parameters An array of addresses of the function parameters.
     */
    final native void invokePointerParameterArray(long functionContext,
            long returnBuffer, long[] parameters);

    final native byte getByte(long address);
    final native short getShort(long address);
    final native int getInt(long address);
    final native long getLong(long address);
    final native float getFloat(long address);
    final native double getDouble(long address);
    final native long getAddress(long address);
    final native void putByte(long address, byte value);
    final native void putShort(long address, short value);
    final native void putInt(long address, int value);
    final native void putLong(long address, long value);
    final native void putFloat(long address, float value);
    final native void putDouble(long address, double value);
    final native void putAddress(long address, long value);
    final native void setMemory(long address, long size, byte value);
    final native void copyMemory(long src, long dst, long size);
    final native void putByteArray(long address, byte[] data, int offset, int length);
    final native void getByteArray(long address, byte[] data, int offset, int length);
    final native void putCharArray(long address, char[] data, int offset, int length);
    final native void getCharArray(long address, char[] data, int offset, int length);
    final native void putShortArray(long address, short[] data, int offset, int length);
    final native void getShortArray(long address, short[] data, int offset, int length);
    final native void putIntArray(long address, int[] data, int offset, int length);
    final native void getIntArray(long address, int[] data, int offset, int length);
    final native void getLongArray(long address, long[] data, int offset, int length);
    final native void putLongArray(long address, long[] data, int offset, int length);
    final native void getFloatArray(long address, float[] data, int offset, int length);
    final native void putFloatArray(long address, float[] data, int offset, int length);
    final native void getDoubleArray(long address, double[] data, int offset, int length);
    final native void putDoubleArray(long address, double[] data, int offset, int length);
    final native long memchr(long address, int value, long len);
    final native long strlen(long address);

    /**
     * Copies a zero (nul) terminated by array from native memory.
     *
     * This method will search for a zero byte, starting from <tt>address</tt>
     * and stop once a zero byte is encountered.  The returned byte array does not
     * contain the terminating zero byte.
     *
     * @param address The address to copy the array from
     * @return A byte array containing the bytes copied from native memory.
     */
    final native byte[] getZeroTerminatedByteArray(long address);

    /**
     * Copies a zero (nul) terminated by array from native memory.
     *
     * This method will search for a zero byte, starting from <tt>address</tt>
     * and stop once a zero byte is encountered.  The returned byte array does not
     * contain the terminating zero byte.
     *
     * @param address The address to copy the array from
     * @param maxlen The maximum number of bytes to search for the nul terminator
     * @return A byte array containing the bytes copied from native memory.
     */
    final native byte[] getZeroTerminatedByteArray(long address, int maxlen);

    /**
     * Copies a java byte array to native memory and appends a NUL terminating byte.
     *
     * <b>Note</b> A total of length + 1 bytes is written to native memory.
     *
     * @param address The address to copy to.
     * @param data The byte array to copy to native memory
     * @param offset The offset within the byte array to begin copying from
     * @param length The number of bytes to copy to native memory
     */
    final native void putZeroTerminatedByteArray(long address, byte[] data, int offset, int length);

    final native ByteBuffer newDirectByteBuffer(long address, int capacity);
    final native long getDirectBufferAddress(Buffer buffer);

    final native int getJNIVersion();
    final native long getJavaVM();
    final native void fatalError(String msg);
    final native Class defineClass(String name, Object loader, byte[] buf, int off, int len);
    final native Class defineClass(String name, Object loader, ByteBuffer buf);
    final native Object allocObject(Class clazz);

    final native int registerNatives(Class clazz, long methods,  int methodCount);
    final native int unregisterNatives(Class clazz);
    
    
}
