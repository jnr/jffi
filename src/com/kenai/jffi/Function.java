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
 * Native function invocation context
 *
 * This class holds all the information that JFFI needs to correctly call a
 * native function.
 */
public final class Function implements CallInfo {
    /** The native address of the context */
    private final long contextAddress;

    /** The address of the function */
    private final long functionAddress;

    /** The number of parameters this function takes */
    private final int parameterCount;

    /** The size of buffer required when packing parameters */
    private final int rawParameterSize;

    /** The return type of this function */
    private final Type returnType;

    /** The parameter types of this function */
    private final Type[] paramTypes;

    /** Whether the native context has been freed yet */
    private volatile boolean disposed = false;

    /**
     * Creates a new instance of <tt>Function</tt> with default calling convention.
     *
     * @param address The native address of the function to invoke.
     * @param returnType The return type of the native function.
     * @param paramTypes The parameter types the function accepts.
     */
    public Function(long address, Type returnType, Type... paramTypes) {
        this(address, returnType, paramTypes, CallingConvention.DEFAULT, true);
    }

    /**
     * Creates a new instance of <tt>Function</tt>.
     *
     * <tt>Function</tt> instances created with this constructor will save the
     * C errno contents after each call.
     *
     * @param address The native address of the function to invoke.
     * @param returnType The return type of the native function.
     * @param paramTypes The parameter types the function accepts.
     * @param convention The calling convention of the function.
     */
    public Function(long address, Type returnType, Type[] paramTypes, CallingConvention convention) {
        this(address, returnType, paramTypes, convention, true);
    }

    /**
     * Creates a new instance of <tt>Function</tt>.
     *
     * @param address The native address of the function to invoke.
     * @param returnType The return type of the native function.
     * @param paramTypes The parameter types the function accepts.
     * @param convention The calling convention of the function.
     * @param saveErrno Whether the errno should be saved or not
     */
    public Function(long address, Type returnType, Type[] paramTypes, CallingConvention convention, boolean saveErrno) {

        this.functionAddress = address;
        final int flags = (!saveErrno ? Foreign.F_NOERRNO : 0)
                | (convention == CallingConvention.STDCALL ? Foreign.F_STDCALL : Foreign.F_DEFAULT);

        final long h = Foreign.getInstance().newFunction(address,
                returnType.handle(), Type.nativeHandles(paramTypes),
                flags);
        if (h == 0) {
            throw new RuntimeException("Failed to create native function");
        }
        this.contextAddress = h;

        //
        // Keep references to the return and parameter types so they do not get
        // garbage collected
        //
        this.returnType = returnType;
        this.paramTypes = (Type[]) paramTypes.clone();

        this.parameterCount = paramTypes.length;
        this.rawParameterSize = Foreign.getInstance().getFunctionRawParameterSize(h);        
    }    

    /**
     * Gets the number of parameters the native function accepts.
     *
     * @return The number of parameters the native function accepts.
     */
    public final int getParameterCount() {
        return parameterCount;
    }

    /**
     * Gets the number of bytes required to pack all the parameters this function
     * accepts, into a region of memory.
     *
     * @return The number of bytes required to store all paraameters of this function.
     */
    public final int getRawParameterSize() {
        return rawParameterSize;
    }

    /**
     * Gets the address of the function context.
     *
     * @return The address of the native function context struct.
     */
    final long getContextAddress() {
        return contextAddress;
    }

    /**
     * Gets the address of the function.
     *
     * @return The address of the native function.
     */
    public final long getFunctionAddress() {
        return functionAddress;
    }

    /**
     * Gets the native return type of this function.
     *
     * @return The native return type of this function.
     */
    public final Type getReturnType() {
        return returnType;
    }
    
    /**
     * Gets the type of a parameter.
     * 
     * @param index The index of the parameter in the function signature
     * @return The <tt>Type</tt> of the parameter.
     */
    public final Type getParameterType(int index) {
        return paramTypes[index];
    }

    public synchronized final void dispose() {
        if (disposed) {
            throw new RuntimeException("function already freed");
        }
        Foreign.getInstance().freeFunction(contextAddress);
        disposed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (contextAddress != 0 && !disposed) {
                Foreign.getInstance().freeFunction(contextAddress);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            super.finalize();
        }
    }
}
