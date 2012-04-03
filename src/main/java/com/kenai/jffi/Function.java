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

/**
 * Native function invocation context
 *
 * This class holds all the information that JFFI needs to correctly call a
 * native function.
 */
public final class Function {
    private final CallContext callContext;

    /** The address of the function */
    final long functionAddress;

    final long contextAddress;

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
     * Creates a new instance of <tt>Function</tt> with default calling convention.
     *
     * @param address The native address of the function to invoke.
     */
    public Function(long address, CallContext callContext) {
        this.functionAddress = address;
        this.callContext = callContext;
        this.contextAddress = callContext.getAddress();
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
        this.callContext = CallContext.getCallContext(returnType, paramTypes, convention, saveErrno);
        this.contextAddress = callContext.getAddress();
    }    

    /**
     * Gets the number of parameters the native function accepts.
     *
     * @return The number of parameters the native function accepts.
     */
    public final int getParameterCount() {
        return callContext.getParameterCount();
    }

    /**
     * Gets the number of bytes required to pack all the parameters this function
     * accepts, into a region of memory.
     *
     * @return The number of bytes required to store all paraameters of this function.
     */
    public final int getRawParameterSize() {
        return callContext.getRawParameterSize();
    }

    public final CallContext getCallContext() {
        return callContext;
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
        return callContext.getReturnType();
    }
    
    /**
     * Gets the type of a parameter.
     * 
     * @param index The index of the parameter in the function signature
     * @return The <tt>Type</tt> of the parameter.
     */
    public final Type getParameterType(int index) {
        return callContext.getParameterType(index);
    }

    @Deprecated
    public final void dispose() {}
}
