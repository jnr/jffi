/*
 * Copyright (C) 2007-2009 Wayne Meissner
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

import java.nio.Buffer;

/**
 * A parameter buffer used when invoking a function
 */
public abstract class InvocationBuffer {
    /**
     * Adds an 8 bit integer parameter.
     *
     * @param value An 8 bit integer value to use as the parameter.
     */
    public abstract void putByte(final int value);

    /**
     * Adds a 16 bit integer parameter.
     *
     * @param value A 16 bit integer value to use as the parameter.
     */
    public abstract void putShort(final int value);

    /**
     * Adds a 32 bit integer parameter.
     *
     * @param value A 32 bit integer value to use as the parameter.
     */
    public abstract void putInt(final int value);

    /**
     * Adds a 64 bit integer parameter.
     *
     * @param value A 64 bit integer value to use as the parameter.
     */
    public abstract void putLong(final long value);

    /**
     * Adds a 32 bit floating point parameter.
     *
     * @param value A 32 bit floating point value to use as the parameter.
     */
    public abstract void putFloat(final float value);

    /**
     * Adds a 64 bit floating point parameter.
     *
     * @param value A 64 bit floating point value to use as the parameter.
     */
    public abstract void putDouble(final double value);

    /**
     * Adds a native address parameter.
     *
     * @param value A native address value to use as the parameter.
     */
    public abstract void putAddress(final long value);

    /**
     * Adds a java byte array as a pointer parameter.
     *
     * @param value The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public abstract void putArray(final byte[] value, int offset, int length, int flags);

    /**
     * Adds a java short array as a pointer parameter.
     *
     * @param value The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public abstract void putArray(final short[] value, int offset, int length, int flags);

    /**
     * Adds a java int array as a pointer parameter.
     *
     * @param value The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public abstract void putArray(final int[] value, int offset, int length, int flags);

    /**
     * Adds a java long array as a pointer parameter.
     *
     * @param value The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public abstract void putArray(final long[] value, int offset, int length, int flags);

    /**
     * Adds a java float array as a pointer parameter.
     *
     * @param value The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public abstract void putArray(final float[] value, int offset, int length, int flags);

    /**
     * Adds a java double array as a pointer parameter.
     *
     * @param value The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public abstract void putArray(final double[] value, int offset, int length, int flags);

    /**
     * Adds a java direct buffer as a pointer parameter.
     * @param buffer The buffer to use as a pointer argument.
     * @param offset An offset to add to the buffer native address.
     * @param length The length of the buffer to use.
     */
    public abstract void putDirectBuffer(final Buffer buffer, int offset, int length);

    /**
     * Adds a struct or union as a parameter.
     *
     * This passes the struct or union by value, not by reference.
     * 
     * @param struct A java byte array with the struct contents.
     * @param offset The offset from the start of the array.
     */
    public abstract void putStruct(final byte[] struct, int offset);

    /**
     * Adds a struct or union as a parameter.
     *
     * This passes the struct or union by value, not by reference.
     *
     * @param struct The native address to use as the struct contents.
     */
    public abstract void putStruct(final long struct);
}
