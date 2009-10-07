/*
 * Copyright (C) 2007, 2008, 2009 Wayne Meissner
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
        private static final Foreign INSTANCE = new Foreign();
    }

    public static final Foreign getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    private Foreign() {
        Init.init();
    }

    private final static int getVersionField(String name) {
        try {
        Class c = Class.forName(Foreign.class.getPackage().getName() + ".Version");
            return (Integer) c.getField(name).get(c);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
    public final static int VERSION_MAJOR = getVersionField("MAJOR");
    public final static int VERSION_MINOR = getVersionField("MINOR");
    public final static int VERSION_MICRO = getVersionField("MICRO");

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

    /** Pages can be read */
    public static final int PROT_READ  = 0x1;

    /** Pages can be written */
    public static final int PROT_WRITE = 0x2;

    /** Pages can be executed */
    public static final int PROT_EXEC  = 0x4;

    /** Pages cannot be accessed */
    public static final int PROT_NONE  = 0x0;

    /** Share changes */
    public static final int MAP_SHARED = 0x1;

    public static final int MAP_PRIVATE = 0x2;

    /** Use the specified address */
    public static final int MAP_FIXED = 0x10;

    public static final int MAP_NORESERVE = 0x40;

    public static final int MAP_ANON = 0x100;

    public static final int MAP_ALIGN = 0x200;

    /** Code segment memory */
    public static final int MAP_TEXT = 0x400;

    /** Win32 VirtualAlloc/VirtualProtect flags */
    public static final int PAGE_NOACCESS = 0x0001;
    public static final int PAGE_READONLY = 0x0002;
    public static final int PAGE_READWRITE = 0x0004;
    public static final int PAGE_WRITECOPY = 0x0008;
    public static final int PAGE_EXECUTE           = 0x0010;
    public static final int PAGE_EXECUTE_READ      = 0x0020;
    public static final int PAGE_EXECUTE_READWRITE = 0x0040;
    public static final int PAGE_EXECUTE_WRITECOPY = 0x0080;
    public static final int MEM_COMMIT    =      0x1000;
    public static final int MEM_RESERVE   =      0x2000;
    public static final int MEM_DECOMMIT  =      0x4000;
    public static final int MEM_RELEASE   =      0x8000;
    public static final int MEM_FREE      =     0x10000;
    public static final int MEM_PRIVATE   =     0x20000;
    public static final int MEM_MAPPED    =     0x40000;
    public static final int MEM_RESET     =     0x80000;
    public static final int MEM_TOP_DOWN  =    0x100000;
    public static final int MEM_PHYSICAL  =    0x400000;
    public static final int MEM_4MB_PAGES =  0x80000000;

    /*
     * Function flags
     */
    /**
     * Default calling convention
     */
    public static final int F_DEFAULT = 0x0;

    /**
     * Windows STDCALL calling convention
     */
    public static final int F_STDCALL = 0x1;

    /**
     * Do not save errno after each call
     */
    public static final int F_NOERRNO = 0x2;

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

    /**
     * Allocates native memory.
     *
     * @param size The number of bytes of memory to allocate
     * @param clear Whether the memory should be cleared (each byte set to zero).
     * @return The native address of the allocated memory.
     */
    final native long allocateMemory(long size, boolean clear);

    /**
     * Releases memory allocated via {@link allocateMemory} back to the system.
     *
     * @param address The address of the memory to release.
     */
    final native void freeMemory(long address);

    /**
     * Gets the size of a page of memory.
     *
     * @return The size of a memory page in bytes.
     */
    final native long pageSize();

    /**
     * Calls the Unix mmap(2) function
     *
     * This method is undefined on windows.
     *
     * @param addr The desired address to map the memory at, or 0 for random address.
     * @param len The length of the memory region.
     * @param prot The protection mode for the memory region.
     * @param flags
     * @param fd
     * @param off
     * @return The address of the mapping on success, -1 on error.
     */
    final native long mmap(long addr, long len, int prot, int flags, int fd, long off);

    /**
     * Calls the Unix munmap(2) function.
     *
     * @param addr The address to unmap.
     * @param len The size of the region.
     * @return 0 on success, -1 on error.
     */

    final native int munmap(long addr, long len);

    /**
     * Calls the Unix mprotect(2) function.
     * @param addr The address to unmap.
     * @param len The size of the region.
     * @param prot The new protection mode.
     * @return 0 on success, -1 on error.
     */
    final native int mprotect(long addr, long len, int prot);


    final native long VirtualAlloc(long addr, int size, int flags, int prot);

    final native boolean VirtualFree(long addr, int size, int flags);

    final native boolean VirtualProtect(long addr, int size, int prot);
    
    /**
     * Creates a new native function context.
     *
     * @param address The address of the native function to call
     * @param returnType The return type of the function
     * @param paramTypes The types of the parameters
     * @param flags A bitmask of F_DEFAULT, F_STDCALL or F_NOERRNO
     * @return The native address of a new function context
     */
    final native long newFunction(long address, long returnType, long[] paramTypes, int flags);

    /**
     * Frees a function context created by {@link #newFunction}
     *
     * @param handle The native function context to free
     */
    final native void freeFunction(long functionContext);

    /**
     * Gets the address of the function in a function context.
     *
     * @param functionContext The function context
     * @return The address of the native function.
     */
    final native long getFunctionAddress(long functionContext);

    /**
     * Gets the size required to pack parameters for the function in libffi raw format.
     * 
     * @param functionContext The function context
     * @return The size in bytes required to pack parameters in raw format
     */
    final native int getFunctionRawParameterSize(long functionContext);

    /**
     * Creates a new native call context.
     *
     * @param returnType The return type of the function
     * @param paramTypes The types of the parameters
     * @param flags A bitmask of F_DEFAULT, F_STDCALL or F_NOERRNO
     *
     * @return The native address of a new function context
     */
    final native long newCallContext(long returnType, long[] paramTypes, int flags);
    
    /**
     * Frees a call context created by {@link #newCallContext}
     *
     * @param handle The native function context to free
     */
    final native void freeCallContext(long callContext);

    /**
     * Gets the size required to pack parameters for the function in libffi raw format.
     *
     * @param functionContext The function context
     * @return The size in bytes required to pack parameters in raw format
     */
    final native int getCallContextRawParameterSize(long callContext);
    
    final native boolean isRawParameterPackingEnabled();

    /**
     * Gets the last error returned by a native function
     *
     * @return An integer.
     */
    final native int getLastError();
    
    /**
     * Sets the native errno value
     * 
     * @param error The value to set errno to.
     */
    final native void setLastError(int error);
    
    
    final native long newClosurePool(long contextAddress, Method closureMethod);
    final native void freeClosurePool(long closurePool);

    final native long allocateClosure(long closurePool, Object proxy);
    final native void releaseClosure(long handle);

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
     * Invokes a function with no arguments, and returns a 32 bit float.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @return A 32 bit float value.
     */
    final native float invokeVrF(long functionContext);

    /**
     * Invokes a function with no arguments, and returns a 32 bit integer.
     *
     * This method does not save the errno value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @return A 32 bit integer value.
     */
    final native int invokeNoErrnoVrI(long functionContext);

    /**
     * Invokes a function with one integer argument, and returns a 32 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    final native int invokeIrI(long functionContext, int arg1);

    /**
     * Invokes a function with one integer argument, and returns a 32 bit integer.
     *
     * This method does not save the errno value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    final native int invokeNoErrnoIrI(long functionContext, int arg1);

    /**
     * Invokes a function with one integer argument, and returns a 32 bit float.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The 32 bit integer argument.
     * @return A 32 bit float value.
     */
    final native float invokeIrF(long functionContext, int arg1);

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
     * Invokes a function with two integer arguments, and returns a 32 bit float.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @return A 32 bit float value.
     */
    final native float invokeIIrF(long functionContext, int arg1, int arg2);

    /**
     * Invokes a function with two integer arguments, and returns a 32 bit integer.
     *
     * This method does not save the errno value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    final native int invokeNoErrnoIIrI(long functionContext, int arg1, int arg2);

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
     * Invokes a function with three integer arguments, and returns a 32 bit float.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @param arg3 The third 32 bit integer argument.
     * @return A 32 bit float value.
     */
    final native float invokeIIIrF(long functionContext, int arg1, int arg2, int arg3);

    /**
     * Invokes a function with three integer arguments, and returns a 32 bit integer.
     *
     * This method does not save the errno value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @param arg3 The third 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    final native int invokeNoErrnoIIIrI(long functionContext, int arg1, int arg2, int arg3);
    
    /**
     * Invokes a function with no arguments, and returns a 64 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @return A 64 bit integer value.
     */
    final native long invokeVrL(long function);

    /**
     * Invokes a function with no arguments, and returns a 64 bit float.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @return A 64 bit float value.
     */
    final native double invokeVrD(long function);

    /**
     * Invokes a function with one 64 bit integer argument, and returns a 64 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    final native long invokeLrL(long function, long arg1);

    /**
     * Invokes a function with one 64 bit integer argument, and returns a 64 bit float.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The 64 bit integer argument.
     * @return A 64 bit float value.
     */
    final native double invokeLrD(long function, long arg1);

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
     * Invokes a function with two 64 bit integer arguments, and returns a 64 bit float.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 64 bit integer argument.
     * @param arg2 The second 64 bit integer argument.
     * @return A 64 bit float value.
     */
    final native double invokeLLrD(long function, long arg1, long arg2);

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
     * Invokes a function with four 64 bit integer arguments, and returns a 64 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 64 bit integer argument.
     * @param arg2 The second 64 bit integer argument.
     * @param arg3 The third 64 bit integer argument.
     * @param arg4 The fourth 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    final native long invokeLLLLrL(long function, long arg1, long arg2, long arg3, long arg4);

    /**
     * Invokes a function with five 64 bit integer arguments, and returns a 64 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 64 bit integer argument.
     * @param arg2 The second 64 bit integer argument.
     * @param arg3 The third 64 bit integer argument.
     * @param arg4 The fourth 64 bit integer argument.
     * @param arg5 The fifth 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    final native long invokeLLLLLrL(long function, long arg1, long arg2, long arg3, long arg4, long arg5);

    /**
     * Invokes a function with six 64 bit integer arguments, and returns a 64 bit integer.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 64 bit integer argument.
     * @param arg2 The second 64 bit integer argument.
     * @param arg3 The third 64 bit integer argument.
     * @param arg4 The fourth 64 bit integer argument.
     * @param arg5 The fifth 64 bit integer argument.
     * @param arg6 The sixth 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    final native long invokeLLLLLLrL(long function, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6);

    /**
     * Invokes a function with three 64 bit integer arguments, and returns a 64 bit float.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first 64 bit integer argument.
     * @param arg2 The second 64 bit integer argument.
     * @param arg3 The third 64 bit integer argument.
     * @return A 64 bit float value.
     */
    final native double invokeLLLrD(long function, long arg1, long arg2, long arg3);

    /**
     * Invokes a function with zero numeric arguments, and returns a numeric value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @return A numeric value.
     */
    final native long invokeVrN(long function);

    /**
     * Invokes a function with one numeric arguments, and returns a numeric value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first numeric argument.
     * @return A numeric value.
     */
    final native long invokeNrN(long function, long arg1);

    /**
     * Invokes a function with two numeric arguments, and returns a numeric value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first numeric argument.
     * @param arg2 The second numeric argument.
     * @return A numeric value.
     */
    final native long invokeNNrN(long function, long arg1, long arg2);

    /**
     * Invokes a function with three numeric arguments, and returns a numeric value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first numeric argument.
     * @param arg2 The second numeric argument.
     * @param arg3 The third numeric argument.
     * @return A numeric value.
     */
    final native long invokeNNNrN(long function, long arg1, long arg2, long arg3);

    /**
     * Invokes a function with four numeric arguments, and returns a numeric value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first numeric argument.
     * @param arg2 The second numeric argument.
     * @param arg3 The third numeric argument.
     * @param arg4 The fourth numeric argument.
     * @return A numeric value.
     */
    final native long invokeNNNNrN(long function, long arg1, long arg2, long arg3, long arg4);

    /**
     * Invokes a function with five numeric arguments, and returns a numeric value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first numeric argument.
     * @param arg2 The second numeric argument.
     * @param arg3 The third numeric argument.
     * @param arg4 The fourth numeric argument.
     * @param arg5 The fifth numeric argument.
     * @return A numeric value.
     */
    final native long invokeNNNNNrN(long function, long arg1, long arg2, long arg3, long arg4, long arg5);

    /**
     * Invokes a function with six numeric arguments, and returns a numeric value.
     *
     * @param function The address of the function context structure from {@link #newFunction}.
     * @param arg1 The first numeric argument.
     * @param arg2 The second numeric argument.
     * @param arg3 The third numeric argument.
     * @param arg4 The fourth numeric argument.
     * @param arg5 The fifth numeric argument.
     * @param arg6 The sixth numeric argument.
     * @return A numeric value.
     */
    final native long invokeNNNNNNrN(long function, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6);

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
    
    final native long invokeArrayO1Int64(long function, byte[] buffer, Object o1, int o1Info, int o1off, int o1len);
    final native long invokeArrayO2Int64(long function, byte[] buffer, Object o1, int o1Info, int o1off, int o1len,
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

    /**
     * Reads an 8 bit integer from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A byte containing the value.
     */
    final native byte getByte(long address);

    /**
     * Reads a 16 bit integer from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A short containing the value.
     */
    final native short getShort(long address);

    /**
     * Reads a 32 bit integer from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return An int containing the value.
     */
    final native int getInt(long address);

    /**
     * Reads a 64 bit integer from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A long containing the value.
     */
    final native long getLong(long address);

    /**
     * Reads a 32 bit floating point value from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A float containing the value.
     */
    final native float getFloat(long address);

    /**
     * Reads a 64 bit floating point value from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A double containing the value.
     */
    final native double getDouble(long address);

    /**
     * Reads a native memory address from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A long containing the value.
     */
    final native long getAddress(long address);

    /**
     * Writes an 8 bit integer value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    final native void putByte(long address, byte value);

    /**
     * Writes a 16 bit integer value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    final native void putShort(long address, short value);

    /**
     * Writes a 32 bit integer value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    final native void putInt(long address, int value);

    /**
     * Writes a 64 bit integer value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    final native void putLong(long address, long value);

    /**
     * Writes a 32 bit floating point value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    final native void putFloat(long address, float value);

    /**
     * Writes a 64 bit floating point value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    final native void putDouble(long address, double value);

    /**
     * Writes a native memory address value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    final native void putAddress(long address, long value);

    /**
     * Sets a region of native memory to a specific byte value.
     *
     * @param address The address of start of the native memory.
     * @param size The number of bytes to set.
     * @param value The value to set the native memory to.
     */
    final native void setMemory(long address, long size, byte value);

    /**
     * Copies contents of a native memory location to another native memory location.
     *
     * @param src The source memory address.
     * @param dst The destination memory address.
     * @param size The number of bytes to copy.
     */
    final native void copyMemory(long src, long dst, long size);

    /**
     * Writes a java byte array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    final native void putByteArray(long address, byte[] data, int offset, int length);

    /**
     * Reads a java byte array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    final native void getByteArray(long address, byte[] data, int offset, int length);

    /**
     * Writes a java char array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    final native void putCharArray(long address, char[] data, int offset, int length);

    /**
     * Reads a java char array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    final native void getCharArray(long address, char[] data, int offset, int length);

    /**
     * Writes a java short array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    final native void putShortArray(long address, short[] data, int offset, int length);

    /**
     * Reads a java short array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    final native void getShortArray(long address, short[] data, int offset, int length);

    /**
     * Writes a java int array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    final native void putIntArray(long address, int[] data, int offset, int length);

    /**
     * Reads a java int array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    final native void getIntArray(long address, int[] data, int offset, int length);

    /**
     * Writes a java long array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    final native void putLongArray(long address, long[] data, int offset, int length);

    /**
     * Reads a java long array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    final native void getLongArray(long address, long[] data, int offset, int length);

    /**
     * Writes a java double array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    final native void putFloatArray(long address, float[] data, int offset, int length);

    /**
     * Reads a java float array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    final native void getFloatArray(long address, float[] data, int offset, int length);

    /**
     * Writes a java double array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    final native void putDoubleArray(long address, double[] data, int offset, int length);

    /**
     * Reads a java double array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    final native void getDoubleArray(long address, double[] data, int offset, int length);

    /**
     * Gets the address of a byte value in a native memory region.
     *
     * @param address The native memory address to start searching.
     * @param value The value to search for.
     * @param len The size of the native memory region being searched.
     * @return The address of the value, or 0 (zero) if not found.
     */
    final native long memchr(long address, int value, long len);

    /**
     * Copies potentially overlapping memory areas.
     *
     * @param dst The destination memory address.
     * @param src The source memory address.
     * @param size The number of bytes to copy.
     */
    final native void memmove(long dst, long src, long len);

    /**
     * Copies non-overlapping memory areas.
     *
     * @param dst The destination memory address.
     * @param src The source memory address.
     * @param size The number of bytes to copy.
     */
    final native void memcpy(long dst, long src, long len);


    /**
     * Gets the length of a native ascii or utf-8 string.
     *
     * @param address The native address of the string.
     * @return The length of the string, in bytes.
     */
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

    /**
     * Creates a new Direct ByteBuffer for a native memory region.
     *
     * @param address The start of the native memory region.
     * @param capacity The size of the native memory region.
     * @return A ByteBuffer representing the native memory region.
     */
    final native ByteBuffer newDirectByteBuffer(long address, int capacity);

    /**
     * Gets the native memory address of a direct ByteBuffer
     *
     * @param buffer A direct ByteBuffer to get the address of.
     * @return The native memory address of the buffer contents, or null if not a direct buffer.
     */
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
