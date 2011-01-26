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
 * Implementation of the {@link Closure.Buffer} interface to read/write
 * parameter and return value data in native memory
 */
final class DirectClosureBuffer implements Closure.Buffer {

    private static final MemoryIO IO = MemoryIO.getInstance();
    private static final NativeWordIO WordIO = NativeWordIO.getInstance();
    private static final long PARAM_SIZE = Platform.getPlatform().addressSize() / 8;
    private final long retval;
    private final long parameters;
    /* Keep references to the return and parameter types to prevent garbage collection */
    private final CallContext callContext;

    public DirectClosureBuffer(CallContext callContext, long retval, long parameters) {
        super();
        this.callContext = callContext;
        this.retval = retval;
        this.parameters = parameters;
    }

    public final byte getByte(int index) {
        return IO.getByte(IO.getAddress(parameters + (index * PARAM_SIZE)));
    }

    public final short getShort(int index) {
        return IO.getShort(IO.getAddress(parameters + (index * PARAM_SIZE)));
    }

    public final int getInt(int index) {
        return IO.getInt(IO.getAddress(parameters + (index * PARAM_SIZE)));
    }

    public final long getLong(int index) {
        return IO.getLong(IO.getAddress(parameters + (index * PARAM_SIZE)));
    }

    public final float getFloat(int index) {
        return IO.getFloat(IO.getAddress(parameters + (index * PARAM_SIZE)));
    }

    public final double getDouble(int index) {
        return IO.getDouble(IO.getAddress(parameters + (index * PARAM_SIZE)));
    }

    public final long getAddress(int index) {
        return IO.getAddress(IO.getAddress(parameters + (index * PARAM_SIZE)));
    }

    public final long getStruct(int index) {
        return IO.getAddress(parameters + (index * PARAM_SIZE));
    }

    public final void setByteReturn(byte value) {
        WordIO.put(retval, value);
    }

    public final void setShortReturn(short value) {
        WordIO.put(retval, value);
    }

    public final void setIntReturn(int value) {
        WordIO.put(retval, value);
    }

    public final void setLongReturn(long value) {
        IO.putLong(retval, value);
    }

    public final void setFloatReturn(float value) {
        IO.putFloat(retval, value);
    }

    public final void setDoubleReturn(double value) {
        IO.putDouble(retval, value);
    }

    public final void setAddressReturn(long address) {
        IO.putAddress(retval, address);
    }

    public void setStructReturn(long value) {
        IO.copyMemory(value, retval, callContext.getReturnType().size());
    }

    public void setStructReturn(byte[] data, int offset) {
        IO.putByteArray(retval, data, offset, callContext.getReturnType().size());
    }

    /**
     * Reads annd writes data types that are smaller than the size of a native
     * long, as a native long for compatibility with FFI.
     */
    private static abstract class NativeWordIO {
        public static final NativeWordIO getInstance() {
            return Platform.getPlatform().addressSize() == 32
                    ? NativeWordIO32.INSTANCE : NativeWordIO64.INSTANCE;
        }

        /**
         * Writes a native long argument to native memory.
         *
         * @param address The address to write the value at
         * @param value The value to write.
         */
        abstract void put(long address, int value);

        /**
         * Reads a native long argument from native memory.
         * @param address The memory address to read the value from
         * @return An integer
         */
        abstract int get(long address);
    }

    private static final class NativeWordIO32 extends NativeWordIO {
        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        static final NativeWordIO INSTANCE = new NativeWordIO32();

        void put(long address, int value) {
            IO.putInt(address, value);
        }

        int get(long address) {
            return IO.getInt(address);
        }
    }

    private static final class NativeWordIO64 extends NativeWordIO {

        private static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
        static final NativeWordIO INSTANCE = new NativeWordIO64();

        void put(long address, int value) {
            IO.putLong(address, value);
        }

        int get(long address) {
            return (int) IO.getLong(address);
        }
    }
}
