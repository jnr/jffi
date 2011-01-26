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
 * Represents a native closure.
 *
 * A Closure is implemented by java code to act as a C function pointer.
 */
public interface Closure {
    public void invoke(Buffer buffer);

    /**
     * An interface to the native callback parameter buffer.
     */
    public static interface Buffer {

        /**
         * Gets the value of an 8 bit integer parameter.
         *
         * @param index The parameter index
         * @return An 8 bit integer value.
         */
        byte getByte(int index);

        /**
         * Gets the value of a 16 bit integer parameter.
         *
         * @param index The parameter index
         * @return A 16 bit integer value.
         */
        short getShort(int index);

        /**
         * Gets the value of a 32 bit integer parameter.
         *
         * @param index The parameter index
         * @return A 32 bit integer value.
         */
        int getInt(int index);

        /**
         * Gets the value of a 64 bit integer parameter.
         *
         * @param index The parameter index
         * @return A 64 bit integer value.
         */
        long getLong(int index);

        /**
         * Gets the value of a 32 bit floating point parameter.
         *
         * @param index The parameter index
         * @return A 32 bit floating point value.
         */
        float getFloat(int index);

        /**
         * Gets the value of a 64 bit floating point parameter.
         *
         * @param index The parameter index
         * @return A 64 bit floating point value.
         */
        double getDouble(int index);

        /**
         * Gets the value of a native pointer parameter.
         *
         * @param index The parameter index
         * @return A native memory address.
         */
        long getAddress(int index);

        /**
         * Gets the address of a struct parameter that is passed by value.
         *
         * @param index The parameter index
         * @return A native memory address.
         */
        long getStruct(int index);

        /**
         * Sets the closure return value to an 8 bit integer value.
         *
         * @param value The 8 bit integer value to return from the closure.
         */
        void setByteReturn(byte value);

        /**
         * Sets the closure return value to a 16 bit integer value.
         *
         * @param value The 16 bit integer value to return from the closure.
         */
        void setShortReturn(short value);

        /**
         * Sets the closure return value to a 32 bit integer value.
         *
         * @param value The 32 bit integer value to return from the closure.
         */
        void setIntReturn(int value);

        /**
         * Sets the closure return value to a 64 bit integer value.
         *
         * @param value The 64 bit integer value to return from the closure.
         */
        void setLongReturn(long value);

        /**
         * Sets the closure return value to a 32 bit floating point value.
         *
         * @param value The 32 bit floating point value to return from the closure.
         */
        void setFloatReturn(float value);

        /**
         * Sets the closure return value to a 64 bit floating point value.
         *
         * @param value The 64 bit floating point value to return from the closure.
         */
        void setDoubleReturn(double value);

        /**
         * Sets the closure return value to a native pointer value.
         *
         * @param address The native pointer value to return from the closure.
         */
        void setAddressReturn(long address);

        /**
         * Sets the closure return value to the contents of a struct
         *
         * @param address The address of a native struct to return as a struct value from the closure.
         */
        void setStructReturn(long address);

        /**
         * Sets the closure return value to the contents of a struct
         *
         * @param data Struct data packed into a byte array to return as a struct value from the closure.
         * @param offset the offset within the byte array to start copying data
         */
        void setStructReturn(byte[] data, int offset);
    }

    /**
     * A Handle is allocated by the {@link ClosureManager}, as a strong reference
     * to the native closure trampoline.
     */
    public static interface Handle {
        /**
         * Gets the native code address of the closure.
         *
         * This can be passed into a native function that takes a function pointer.
         *
         * @return The native address of the closure code.
         */
        long getAddress();

        /**
         * Sets whether the closure memory should be released when the <tt>Handle</tt> is
         * garbage collected or not.
         *
         * @param autorelease If true, the closure memory is automatically managed,
         * else the closure memory must be explicitly freed.
         */
        void setAutoRelease(boolean autorelease);

        /**
         * Releases the closure memory back to the operating system.
         *
         * Although the closure trampoline memory will normally be released when
         * the <tt>Handle</tt> is garbage collected, this may not happen for some
         * time, and is non-deterministic.  This allows explicit control over
         * memory reclamation.
         */
        void dispose();

        @Deprecated
        void free();
    }
}
