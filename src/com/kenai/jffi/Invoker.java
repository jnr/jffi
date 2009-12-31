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
 * Provides native function invocation facilities.
 */
public abstract class Invoker {

    /** The size in bits of a native memory address */
    private static final long ADDRESS_SIZE = Platform.getPlatform().addressSize();

    /** A mask to apply to native memory addresses to cancel sign extension */
    private static final long ADDRESS_MASK = Platform.getPlatform().addressMask();
    
    private final Foreign foreign = Foreign.getInstance();

    /** Lazy initialization singleton holder */
    private static final class SingletonHolder {
        private static final Invoker INSTANCE = ADDRESS_SIZE == 64
                ? LP64.INSTANCE : ILP32.INSTANCE;
    }

    /**
     * Gets the <tt>Invoker</tt> singleton.
     *
     * @return An instance of <tt>Invoker</tt>.
     */
    public static final Invoker getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /** Creates a new <tt>Invoker</tt> */
    private Invoker() {}

    /**
     * Invokes a function with no arguments, and returns a 32 bit integer.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @return A 32 bit integer value.
     */
    public final int invokeVrI(Function function) {
        return foreign.invokeVrI(function.getContextAddress());
    }

    /**
     * Invokes a function with no arguments, and returns a 32 bit integer.
     *
     * This method does not save the errno value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @return A 32 bit integer value.
     */
    public final int invokeNoErrnoVrI(Function function) {
        return foreign.invokeNoErrnoVrI(function.getContextAddress());
    }

    /**
     * Invokes a function with one integer argument, and returns a 32 bit integer.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param arg1 A 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    public final int invokeIrI(Function function, int arg1) {
        return foreign.invokeIrI(function.getContextAddress(), arg1);
    }

    /**
     * Invokes a function with one integer argument, and returns a 32 bit integer.
     *
     * This method does not save the errno value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param arg1 A 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    public final int invokeNoErrnoIrI(Function function, int arg1) {
        return foreign.invokeNoErrnoIrI(function.getContextAddress(), arg1);
    }

    /**
     * Invokes a function with two integer arguments, and returns a 32 bit integer.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    public final int invokeIIrI(Function function, int arg1, int arg2) {
        return foreign.invokeIIrI(function.getContextAddress(), arg1, arg2);
    }

    /**
     * Invokes a function with two integer arguments, and returns a 32 bit integer.
     *
     * This method does not save the errno value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    public final int invokeNoErrnoIIrI(Function function, int arg1, int arg2) {
        return foreign.invokeNoErrnoIIrI(function.getContextAddress(), arg1, arg2);
    }

    /**
     * Invokes a function with three integer arguments, and returns a 32 bit integer.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @param arg3 The third 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    public final int invokeIIIrI(Function function, int arg1, int arg2, int arg3) {
        return foreign.invokeIIIrI(function.getContextAddress(), arg1, arg2, arg3);
    }

    /**
     * Invokes a function with three integer arguments, and returns a 32 bit integer.
     *
     * This method does not save the errno value.
     * 
     * @param function The <tt>Function</tt> to invoke.
     * @param arg1 The first 32 bit integer argument.
     * @param arg2 The second 32 bit integer argument.
     * @param arg3 The third 32 bit integer argument.
     * @return A 32 bit integer value.
     */
    public final int invokeNoErrnoIIIrI(Function function, int arg1, int arg2, int arg3) {
        return foreign.invokeNoErrnoIIIrI(function.getContextAddress(), arg1, arg2, arg3);
    }

    /**
     * Invokes a function with no arguments, and returns a 64 bit integer.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @return A 64 bit integer value.
     */
    public final long invokeVrL(Function function) {
        return foreign.invokeVrL(function.getContextAddress());
    }

    /**
     * Invokes a function with one 64 bit integer argument, and returns a 64 bit integer.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param arg1 The 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    public final long invokeLrL(Function function, long arg1) {
        return foreign.invokeLrL(function.getContextAddress(), arg1);
    }

    /**
     * Invokes a function with two 64 bit integer arguments, and returns a 64 bit integer.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param arg1 The first 64 bit integer argument.
     * @param arg2 The second 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    public final long invokeLLrL(Function function, long arg1, long arg2) {
        return foreign.invokeLLrL(function.getContextAddress(), arg1, arg2);
    }

    /**
     * Invokes a function with three 64 bit integer arguments, and returns a 64 bit integer.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param arg1 The first 64 bit integer argument.
     * @param arg2 The second 64 bit integer argument.
     * @param arg3 The third 64 bit integer argument.
     * @return A 64 bit integer value.
     */
    public final long invokeLLLrL(Function function, long arg1, long arg2, long arg3) {
        return foreign.invokeLLLrL(function.getContextAddress(), arg1, arg2, arg3);
    }

    /**
     * Invokes a function and returns a native memory address.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param buffer A buffer containing the arguments to the function.
     * @return A native memory address.
     */
    public abstract long invokeAddress(Function function, HeapInvocationBuffer buffer);

    /**
     * Invokes a function and returns a 32 bit integer value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param buffer A buffer containing the arguments to the function.
     * @return A native memory address.
     */
    public final int invokeInt(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? invokeArrayWithObjectsInt32(function, buffer, objectBuffer)
                : foreign.invokeArrayReturnInt(function.getContextAddress(), buffer.array());
    }

    /**
     * Invokes a function and returns a 64 bit integer value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param buffer A buffer containing the arguments to the function.
     * @return A native memory address.
     */
    public final long invokeLong(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? foreign.invokeArrayWithObjectsInt64(function.getContextAddress(), buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects())
                : foreign.invokeArrayReturnLong(function.getContextAddress(), buffer.array());
    }

    /**
     * Invokes a function and returns a 32 bit floating point value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param buffer A buffer containing the arguments to the function.
     * @return A native memory address.
     */
    public final float invokeFloat(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? foreign.invokeArrayWithObjectsFloat(function.getContextAddress(), buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects())
                : foreign.invokeArrayReturnFloat(function.getContextAddress(), buffer.array());
    }

    /**
     * Invokes a function and returns a 64 bit floating point value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param buffer A buffer containing the arguments to the function.
     * @return A native memory address.
     */
    public final double invokeDouble(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? foreign.invokeArrayWithObjectsDouble(function.getContextAddress(), buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects())
                : foreign.invokeArrayReturnDouble(function.getContextAddress(), buffer.array());
    }

    /**
     * Invokes a function that returns a C struct by value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param buffer The parameter buffer.
     * @return A byte array with the return value encoded in native byte order.
     */
    public final byte[] invokeStruct(Function function, HeapInvocationBuffer buffer) {
        byte[] returnBuffer = new byte[function.getReturnType().size()];

        invokeStruct(function, buffer, returnBuffer, 0);

        return returnBuffer;
    }

    /**
     * Invokes a function that returns a C struct by value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param buffer The parameter buffer.
     * @param returnBuffer The output buffer to place the return value in.
     * @param offset The offset within returnBuffer to place the return value.
     */
    public final void invokeStruct(Function function, HeapInvocationBuffer buffer, byte[] returnBuffer, int offset) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        if (objectBuffer != null) {
            foreign.invokeArrayWithObjectsReturnStruct(function.getContextAddress(),
                    buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects(),
                    returnBuffer, offset);
        } else {
            foreign.invokeArrayReturnStruct(function.getContextAddress(), buffer.array(), returnBuffer, offset);
        }
    }

    public final Object invokeObject(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return foreign.invokeArrayWithObjectsReturnObject(function.getContextAddress(),
                buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects());
    }

    /**
     * Invokes a function, with the parameters loaded into native memory buffers,
     * and the function result is stored in a native memory buffer.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param returnBuffer The address of the native buffer to place the result
     * of the function call in.
     * @param parameters An array of addresses of the function parameters.
     */
    public final void invoke(Function function, long returnBuffer, long[] parameters) {
        foreign.invokePointerParameterArray(function.getContextAddress(), returnBuffer, parameters);
    }

    /**
     * Convenience method to pass the objects and object descriptor array down as
     * normal arguments, so hotspot can optimize it.  This is faster than the native
     * code pulling the objects and descriptors out of arrays.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param buffer A buffer containing the arguments to the function.
     * @param objectBuffer A buffer containing objects to be passed to the native function.
     * @return A 32 bit integer value.
     */
    private final int invokeArrayWithObjectsInt32(Function function, HeapInvocationBuffer buffer,
            ObjectBuffer objectBuffer) {
        Object[] objects = objectBuffer.objects();
        int[] info = objectBuffer.info();
        int objectCount = objectBuffer.objectCount();

        switch (objectCount) {
            case 1:
                return foreign.invokeArrayO1Int32(function.getContextAddress(), buffer.array(),
                        objects[0], info[0], info[1], info[2]);
            case 2:
                return foreign.invokeArrayO2Int32(function.getContextAddress(), buffer.array(),
                        objects[0], info[0], info[1], info[2],
                        objects[1], info[3], info[4], info[5]);
        }

        return foreign.invokeArrayWithObjectsInt32(function.getContextAddress(), buffer.array(),
            objectCount, info, objects);
    }

    /**
     * A 32 bit invoker implementation
     */
    private static final class ILP32 extends Invoker {
        private static final Invoker INSTANCE = new ILP32();

        public final long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return ((long)invokeInt(function, buffer)) & ADDRESS_MASK;
        }
    }


    /**
     * A 64 bit invoker implementation
     */
    private static final class LP64 extends Invoker {
        private static final Invoker INSTANCE = new LP64();
        
        public long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return invokeLong(function, buffer);
        }
    }
}
