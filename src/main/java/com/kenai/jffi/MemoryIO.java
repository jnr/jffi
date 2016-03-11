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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Provides facilities to access native memory from java.
 */
public abstract class MemoryIO {
    /** A handle to the JNI accessor */
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    final Foreign foreign = Foreign.getInstance();

    /** The address mask used to truncate 32bit addresses contained in long values */
    private static final long ADDRESS_MASK = Platform.getPlatform().addressMask();

    /** Holds a single instance of <tt>MemoryIO</tt> */
    private static final class SingletonHolder {
        private static final MemoryIO INSTANCE = newMemoryIO();
    }

    private static final class CheckedMemorySingletonHolder {
        private static final MemoryIO INSTANCE = newNativeCheckedImpl();
    }

    /**
     * Gets an instance of <tt>MemoryIO</tt> that can be used to access native memory.
     *
     * @return A <tt>MemoryIO</tt> instance.
     */
    public static MemoryIO getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static MemoryIO getCheckedInstance() {
        return CheckedMemorySingletonHolder.INSTANCE;
    }

    /* Restrict construction of instances to subclasses defined in this class only */
    private MemoryIO() {}

    /**
     * Creates a new instance of <tt>MemoryIO</tt> optimized for the current platform.
     *
     * @return An instance of <tt>MemoryIO</tt>
     */
    private static MemoryIO newMemoryIO() {
        try {
            if (Boolean.getBoolean("jffi.memory.checked")) {
                return newNativeCheckedImpl();
            }

            // Use sun.misc.Unsafe unless explicitly disabled by the user, or not available
            return !Boolean.getBoolean("jffi.unsafe.disabled") && isUnsafeAvailable()
                    ? newUnsafeImpl() : newNativeImpl();
        } catch (Throwable t) {
            return newNativeImpl();
        }
    }
    
    /*
     * The new calls are wrapped in methods, so the classes are not referenced
     * until the method is called.  This means only one implementation class
     * is ever loaded, and hotspot can inline non-final functions implemented
     * in the subclass.
     */
    private static MemoryIO newNativeImpl() {
        return Platform.getPlatform().addressSize() == 32
                ? newNativeImpl32() : newNativeImpl64();
    }

    private static MemoryIO newNativeCheckedImpl() {
        return Foreign.isMemoryProtectionEnabled() ? new CheckedNativeImpl() : newNativeImpl();
    }

    /**
     * Creates a new JNI implementation of <tt>MemoryIO</tt> optimized for 32 bit platforms
     *
     * @return An instance of <tt>MemoryIO</tt>
     */
    private static MemoryIO newNativeImpl32() { return new NativeImpl32();}

    /**
     * Creates a new JNI implementation of <tt>MemoryIO</tt> optimized for 64 bit platforms
     *
     * @return An instance of <tt>MemoryIO</tt>
     */
    private static MemoryIO newNativeImpl64() { return new NativeImpl64();}

    /**
     * Creates a new sun.misc.Unsafe implementation of <tt>MemoryIO</tt>
     *
     * @return An instance of <tt>MemoryIO</tt>
     */
    private static MemoryIO newUnsafeImpl() {
        return Platform.getPlatform().addressSize() == 32
                ? newUnsafeImpl32() : newUnsafeImpl64();
    }

    /**
     * Creates a new sun.misc.Unsafe implementation of <tt>MemoryIO</tt> optimized for 32 bit platforms
     *
     * @return An instance of <tt>MemoryIO</tt>
     */
    private static MemoryIO newUnsafeImpl32() { return new UnsafeImpl32(); }

    /**
     * Creates a new sun.misc.Unsafe implementation of <tt>MemoryIO</tt> optimized for 64 bit platforms
     *
     * @return An instance of <tt>MemoryIO</tt>
     */
    private static MemoryIO newUnsafeImpl64() { return new UnsafeImpl64(); }

    /**
     * Reads an 8 bit integer from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A byte containing the value.
     */
    public abstract byte getByte(long address);

    /**
     * Reads a 16 bit integer from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A short containing the value.
     */
    public abstract short getShort(long address);

    /**
     * Reads a 32 bit integer from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return An int containing the value.
     */
    public abstract int getInt(long address);

    /**
     * Reads a 64 bit integer from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A long containing the value.
     */
    public abstract long getLong(long address);

    /**
     * Reads a 32 bit floating point value from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A float containing the value.
     */
    public abstract float getFloat(long address);

    /**
     * Reads a 64 bit floating point value from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A double containing the value.
     */
    public abstract double getDouble(long address);

    /**
     * Reads a native memory address from a native memory location.
     *
     * @param address The memory location to get the value from.
     * @return A long containing the value.
     */
    public abstract long getAddress(long address);

    /**
     * Writes an 8 bit integer value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    public abstract void putByte(long address, byte value);

    /**
     * Writes a 16 bit integer value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    public abstract void putShort(long address, short value);

    /**
     * Writes a 32 bit integer value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    public abstract void putInt(long address, int value);

    /**
     * Writes a 64 bit integer value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    public abstract void putLong(long address, long value);

    /**
     * Writes a 32 bit floating point value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    public abstract void putFloat(long address, float value);

    /**
     * Writes a 64 bit floating point value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    public abstract void putDouble(long address, double value);

    /**
     * Writes a native memory address value to a native memory location.
     *
     * @param address The memory location to put the value.
     * @param value The value to write to memory.
     */
    public abstract void putAddress(long address, long value);

    /**
     * Copies contents of a native memory location to another native memory location.
     *
     * @param src The source memory address.
     * @param dst The destination memory address.
     * @param size The number of bytes to copy.
     */
    public final void copyMemory(long src, long dst, long size) {
        if (dst + size <= src || src + size <= dst) {
            // Use intrinsic copyMemory if regions do not overlap
            _copyMemory(src, dst, size);
        } else {
            memmove(dst, src, size);
        }
    }

    /**
     * Copies contents of a native memory location to another native memory location.
     *
     * @param src The source memory address.
     * @param dst The destination memory address.
     * @param size The number of bytes to copy.
     */
    abstract void _copyMemory(long src, long dst, long size);

    /**
     * Sets a region of native memory to a specific byte value.
     *
     * @param address The address of start of the native memory.
     * @param size The number of bytes to set.
     * @param value The value to set the native memory to.
     */
    public abstract void setMemory(long address, long size, byte value);


    /**
     * Copies bytes from one memory location to another.
     *
     * The memory areas
     *
     * @param dst The destination memory address.
     * @param src The source memory address.
     * @param size The number of bytes to copy.
     */
    public abstract void memcpy(long dst, long src, long size);

    /**
     * Copies potentially overlapping memory areas.
     *
     * @param dst The destination memory address.
     * @param src The source memory address.
     * @param size The number of bytes to copy.
     */
    public abstract void memmove(long dst, long src, long size);

    /**
     * Sets a region of native memory to a specific byte value.
     *
     * @param address The address of start of the native memory.
     * @param value The value to set the native memory to.
     * @param size The number of bytes to set.
     */
    public final void memset(long address, int value, long size) {
        setMemory(address, size, (byte) value);
    }

     /**
     * Gets the address of a byte value in a native memory region.
     *
     * @param address The native memory address to start searching.
     * @param value The value to search for.
     * @param size The size of the native memory region being searched.
     * @return The address of the value, or 0 (zero) if not found.
     */
    public abstract long memchr(long address, int value, long size);

    /**
     * Writes a java byte array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    public abstract void putByteArray(long address, byte[] data, int offset, int length);

    /**
     * Reads a java byte array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    public abstract void getByteArray(long address, byte[] data, int offset, int length);

    /**
     * Writes a java char array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    public abstract void putCharArray(long address, char[] data, int offset, int length);
    
    /**
     * Reads a java char array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    public abstract void getCharArray(long address, char[] data, int offset, int length);

    /**
     * Writes a java short array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    public abstract void putShortArray(long address, short[] data, int offset, int length);
    
    /**
     * Reads a java short array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    public abstract void getShortArray(long address, short[] data, int offset, int length);

    /**
     * Writes a java int array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    public abstract void putIntArray(long address, int[] data, int offset, int length);

    /**
     * Reads a java int array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    public abstract void getIntArray(long address, int[] data, int offset, int length);

    /**
     * Writes a java long array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    public abstract void putLongArray(long address, long[] data, int offset, int length);

    /**
     * Reads a java long array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    public abstract void getLongArray(long address, long[] data, int offset, int length);

    /**
     * Writes a java double array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    public abstract void putFloatArray(long address, float[] data, int offset, int length);
  
    /**
     * Reads a java float array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    public abstract void getFloatArray(long address, float[] data, int offset, int length);

    /**
     * Writes a java double array to native memory.
     *
     * @param address The native memory address to copy the array to.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying from.
     * @param length The number of array elements to copy.
     */
    public abstract void putDoubleArray(long address, double[] data, int offset, int length);

    /**
     * Reads a java double array from native memory.
     *
     * @param address The native memory address to copy the array from.
     * @param data The java array to copy.
     * @param offset The offset within the array to start copying to.
     * @param length The number of array elements to copy.
     */
    public abstract void getDoubleArray(long address, double[] data, int offset, int length);

    /**
     * Allocates native memory.
     *
     * @param size The number of bytes of memory to allocate
     * @param clear Whether the memory should be cleared (each byte set to zero).
     * @return The native address of the allocated memory.
     */
    public final long allocateMemory(long size, boolean clear) {
        return Foreign.allocateMemory(size, clear) & ADDRESS_MASK;
    }

    /**
     * Releases memory allocated via {@link #allocateMemory} back to the system.
     *
     * @param address The address of the memory to release.
     */
    public final void freeMemory(long address) {
        Foreign.freeMemory(address);
    }

    /**
     * Gets the length of a native ascii or utf-8 string.
     *
     * @param address The native address of the string.
     * @return The length of the string, in bytes.
     */
    public abstract long getStringLength(long address);

    /**
     * Reads a byte array from native memory, stopping when a zero byte is found.
     *
     * This can be used to read ascii or utf-8 strings from native memory.
     *
     * @param address The address to read the data from.
     * @return The byte array containing a copy of the native data.  Any zero
     * byte is stripped from the end.
     */
    public abstract byte[] getZeroTerminatedByteArray(long address);

    /**
     * Reads a byte array from native memory, stopping when a zero byte is found,
     * or the maximum length is reached.
     *
     * This can be used to read ascii or utf-8 strings from native memory.
     *
     * @param address The address to read the data from.
     * @param maxlen The limit of the memory area to scan for a zero byte.
     * @return The byte array containing a copy of the native data.  Any zero
     * byte is stripped from the end.
     */
    public abstract byte[] getZeroTerminatedByteArray(long address, int maxlen);

    @Deprecated
    public final byte[] getZeroTerminatedByteArray(long address, long maxlen) {
        return getZeroTerminatedByteArray(address, (int) maxlen);
    }

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
    public abstract void putZeroTerminatedByteArray(long address, byte[] data, int offset, int length);

    /**
     * Finds the location of a byte value in a native memory region.
     *
     * @param address The native memory address to start searching from.
     * @param value The value to search for.
     * @return The offset from the memory address of the value, if found, else -1 (minus one).
     */
    public final long indexOf(long address, byte value) {
        final long location = memchr(address, value, Integer.MAX_VALUE);
        return location != 0 ? location - address : -1;
    }

    /**
     * Finds the location of a byte value in a native memory region.
     *
     * @param address The native memory address to start searching from.
     * @param value The value to search for.
     * @param maxlen The maximum number of bytes to search.
     * @return The offset from the memory address of the value, if found, else -1 (minus one).
     */
    public final long indexOf(long address, byte value, int maxlen) {
        final long location = memchr(address, value, maxlen);
        return location != 0 ? location - address : -1;
    }

    /**
     * Creates a new Direct ByteBuffer for a native memory region.
     *
     * @param address The start of the native memory region.
     * @param capacity The size of the native memory region.
     * @return A ByteBuffer representing the native memory region.
     */
    public final java.nio.ByteBuffer newDirectByteBuffer(long address, int capacity) {
        return foreign.newDirectByteBuffer(address, capacity);
    }

    /**
     * Gets the native memory address of a direct ByteBuffer
     *
     * @param buffer A direct ByteBuffer to get the address of.
     * @return The native memory address of the buffer contents, or null if not a direct buffer.
     */
    public final long getDirectBufferAddress(java.nio.Buffer buffer) {
        return foreign.getDirectBufferAddress(buffer);
    }


    /**
     * An implementation of MemoryIO using JNI methods.
     */
    private static abstract class NativeImpl extends MemoryIO {

        public final byte getByte(long address) {
            return Foreign.getByte(address);
        }
        public final short getShort(long address) {
            return Foreign.getShort(address);
        }
        public final int getInt(long address) {
            return Foreign.getInt(address);
        }
        public final long getLong(long address) {
            return Foreign.getLong(address);
        }
        public final float getFloat(long address) {
            return Foreign.getFloat(address);
        }
        public final double getDouble(long address) {
            return Foreign.getDouble(address);
        }
        public final void putByte(long address, byte value) {
            Foreign.putByte(address, value);
        }
        public final void putShort(long address, short value) {
            Foreign.putShort(address, value);
        }
        public final void putInt(long address, int value) {
            Foreign.putInt(address, value);
        }
        public final void putLong(long address, long value) {
            Foreign.putLong(address, value);
        }
        public final void putFloat(long address, float value) {
            Foreign.putFloat(address, value);
        }
        public final void putDouble(long address, double value) {
            Foreign.putDouble(address, value);
        }
        public final void setMemory(long address, long size, byte value) {
            Foreign.setMemory(address, size, value);
        }
        public final void _copyMemory(long src, long dst, long size) {
            Foreign.copyMemory(src, dst, size);
        }
        public final void memcpy(long dst, long src, long size) {
            Foreign.memcpy(dst, src, size);
        }
        public final void memmove(long dst, long src, long size) {
            Foreign.memmove(dst, src, size);
        }
        public final long memchr(long address, int value, long size) {
            return Foreign.memchr(address, value, size);
        }
        public final void putByteArray(long address, byte[] data, int offset, int length) {
            Foreign.putByteArray(address, data, offset, length);
        }
        public final void getByteArray(long address, byte[] data, int offset, int length) {
            Foreign.getByteArray(address, data, offset, length);
        }
        public final void putCharArray(long address, char[] data, int offset, int length) {
            Foreign.putCharArray(address, data, offset, length);
        }
        public final void getCharArray(long address, char[] data, int offset, int length) {
            Foreign.getCharArray(address, data, offset, length);
        }
        public final void putShortArray(long address, short[] data, int offset, int length) {
            Foreign.putShortArray(address, data, offset, length);
        }
        public final void getShortArray(long address, short[] data, int offset, int length) {
            Foreign.getShortArray(address, data, offset, length);
        }
        public final void putIntArray(long address, int[] data, int offset, int length) {
            Foreign.putIntArray(address, data, offset, length);
        }
        public final void getIntArray(long address, int[] data, int offset, int length) {
            Foreign.getIntArray(address, data, offset, length);
        }
        public final void putLongArray(long address, long[] data, int offset, int length) {
            Foreign.putLongArray(address, data, offset, length);
        }
        public final void getLongArray(long address, long[] data, int offset, int length) {
            Foreign.getLongArray(address, data, offset, length);
        }
        public final void putFloatArray(long address, float[] data, int offset, int length) {
            Foreign.putFloatArray(address, data, offset, length);
        }
        public final void getFloatArray(long address, float[] data, int offset, int length) {
            Foreign.getFloatArray(address, data, offset, length);
        }
        public final void putDoubleArray(long address, double[] data, int offset, int length) {
            Foreign.putDoubleArray(address, data, offset, length);
        }
        public final void getDoubleArray(long address, double[] data, int offset, int length) {
            Foreign.getDoubleArray(address, data, offset, length);
        }
        public final long getStringLength(long address) {
            return Foreign.strlen(address);
        }
        public final byte[] getZeroTerminatedByteArray(long address) {
            return Foreign.getZeroTerminatedByteArray(address);
        }
        public final byte[] getZeroTerminatedByteArray(long address, int maxlen) {
            return Foreign.getZeroTerminatedByteArray(address, maxlen);
        }
        public final void putZeroTerminatedByteArray(long address, byte[] data, int offset, int length) {
            Foreign.putZeroTerminatedByteArray(address, data, offset, length);
        }

    }

    /**
     * A 32 bit optimized implementation of <tt>MemoryIO</tt> using JNI.
     */
    private static final class NativeImpl32 extends NativeImpl {
        public final long getAddress(long address) {
            // Mask with ADDRESS_MASK to cancel out any sign extension
            return ((long) Foreign.getInt(address)) & ADDRESS_MASK;
        }
        public final void putAddress(long address, long value) {
            Foreign.putInt(address, (int) value);
        }
    }

    private static final class CheckedNativeImpl extends MemoryIO {

        public final byte getByte(long address) {
            return Foreign.getByteChecked(address);
        }
        public final short getShort(long address) {
            return Foreign.getShortChecked(address);
        }
        public final int getInt(long address) {
            return Foreign.getIntChecked(address);
        }
        public final long getLong(long address) {
            return Foreign.getLongChecked(address);
        }
        public final float getFloat(long address) {
            return Foreign.getFloatChecked(address);
        }
        public final double getDouble(long address) {
            return Foreign.getDoubleChecked(address);
        }
        public final void putByte(long address, byte value) {
            Foreign.putByteChecked(address, value);
        }
        public final void putShort(long address, short value) {
            Foreign.putShortChecked(address, value);
        }
        public final void putInt(long address, int value) {
            Foreign.putIntChecked(address, value);
        }
        public final void putLong(long address, long value) {
            Foreign.putLongChecked(address, value);
        }
        public final void putFloat(long address, float value) {
            Foreign.putFloatChecked(address, value);
        }
        public final void putDouble(long address, double value) {
            Foreign.putDoubleChecked(address, value);
        }
        public final void setMemory(long address, long size, byte value) {
            Foreign.setMemoryChecked(address, size, value);
        }
        public final void _copyMemory(long src, long dst, long size) {
            Foreign.copyMemoryChecked(src, dst, size);
        }
        public final long getAddress(long address) {
            return Foreign.getAddressChecked(address) & ADDRESS_MASK;
        }
        public final void putAddress(long address, long value) {
            Foreign.putAddressChecked(address, value);
        }
        public final void memcpy(long dst, long src, long size) {
            Foreign.memcpyChecked(dst, src, size);
        }
        public final void memmove(long dst, long src, long size) {
            Foreign.memmoveChecked(dst, src, size);
        }
        public final long memchr(long address, int value, long size) {
            return Foreign.memchrChecked(address, value, size);
        }
        public final void putByteArray(long address, byte[] data, int offset, int length) {
            Foreign.putByteArrayChecked(address, data, offset, length);
        }
        public final void getByteArray(long address, byte[] data, int offset, int length) {
            Foreign.getByteArrayChecked(address, data, offset, length);
        }
        public final void putCharArray(long address, char[] data, int offset, int length) {
            Foreign.putCharArrayChecked(address, data, offset, length);
        }
        public final void getCharArray(long address, char[] data, int offset, int length) {
            Foreign.getCharArrayChecked(address, data, offset, length);
        }
        public final void putShortArray(long address, short[] data, int offset, int length) {
            Foreign.putShortArrayChecked(address, data, offset, length);
        }
        public final void getShortArray(long address, short[] data, int offset, int length) {
            Foreign.getShortArrayChecked(address, data, offset, length);
        }
        public final void putIntArray(long address, int[] data, int offset, int length) {
            Foreign.putIntArrayChecked(address, data, offset, length);
        }
        public final void getIntArray(long address, int[] data, int offset, int length) {
            Foreign.getIntArrayChecked(address, data, offset, length);
        }
        public final void putLongArray(long address, long[] data, int offset, int length) {
            Foreign.putLongArrayChecked(address, data, offset, length);
        }
        public final void getLongArray(long address, long[] data, int offset, int length) {
            Foreign.getLongArrayChecked(address, data, offset, length);
        }
        public final void putFloatArray(long address, float[] data, int offset, int length) {
            Foreign.putFloatArrayChecked(address, data, offset, length);
        }
        public final void getFloatArray(long address, float[] data, int offset, int length) {
            Foreign.getFloatArrayChecked(address, data, offset, length);
        }
        public final void putDoubleArray(long address, double[] data, int offset, int length) {
            Foreign.putDoubleArrayChecked(address, data, offset, length);
        }
        public final void getDoubleArray(long address, double[] data, int offset, int length) {
            Foreign.getDoubleArrayChecked(address, data, offset, length);
        }
        public final long getStringLength(long address) {
            return Foreign.strlenChecked(address);
        }
        public final byte[] getZeroTerminatedByteArray(long address) {
            return Foreign.getZeroTerminatedByteArrayChecked(address);
        }
        public final byte[] getZeroTerminatedByteArray(long address, int maxlen) {
            return Foreign.getZeroTerminatedByteArrayChecked(address, maxlen);
        }
        public final void putZeroTerminatedByteArray(long address, byte[] data, int offset, int length) {
            Foreign.putZeroTerminatedByteArrayChecked(address, data, offset, length);
        }
    }

    /**
     * A 64 bit optimized implementation of <tt>MemoryIO</tt> using JNI.
     */
    private static final class NativeImpl64 extends NativeImpl {
        public final long getAddress(long address) {
            return Foreign.getLong(address);
        }
        public final void putAddress(long address, long value) {
            Foreign.putLong(address, value);
        }
    }

    /**
     * An implementation of <tt>MemoryIO</tt> using sun.misc.Unsafe
     */
    private static abstract class UnsafeImpl extends MemoryIO {
        protected static sun.misc.Unsafe unsafe = sun.misc.Unsafe.class.cast(getUnsafe());
        private static Object getUnsafe() {
            try {
                Class sunUnsafe = Class.forName("sun.misc.Unsafe");
                Field f = sunUnsafe.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                return f.get(sunUnsafe);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        public final byte getByte(long address) {
            return unsafe.getByte(address);
        }
        public final short getShort(long address) {
            return unsafe.getShort(address);
        }
        public final int getInt(long address) {
            return unsafe.getInt(address);
        }
        public final long getLong(long address) {
            return unsafe.getLong(address);
        }
        public final float getFloat(long address) {
            return unsafe.getFloat(address);
        }
        public final double getDouble(long address) {
            return unsafe.getDouble(address);
        }
        public final void putByte(long address, byte value) {
            unsafe.putByte(address, value);
        }
        public final void putShort(long address, short value) {
            unsafe.putShort(address, value);
        }
        public final void putInt(long address, int value) {
            unsafe.putInt(address, value);
        }
        public final void putLong(long address, long value) {
            unsafe.putLong(address, value);
        }
        public final void putFloat(long address, float value) {
            unsafe.putFloat(address, value);
        }
        public final void putDouble(long address, double value) {
            unsafe.putDouble(address, value);
        }
        public final void _copyMemory(long src, long dst, long size) {
            unsafe.copyMemory(src, dst, size);
        }
        public final void setMemory(long src, long size, byte value) {
            unsafe.setMemory(src, size, value);
        }

        public final void memcpy(long dst, long src, long size) {
            Foreign.memcpy(dst, src, size);
        }
        public final void memmove(long dst, long src, long size) {
            Foreign.memmove(dst, src, size);
        }
        public final long memchr(long address, int value, long size) {
            return Foreign.memchr(address, value, size);
        }
        public final void putByteArray(long address, byte[] data, int offset, int length) {
            Foreign.putByteArray(address, data, offset, length);
        }
        public final void getByteArray(long address, byte[] data, int offset, int length) {
            Foreign.getByteArray(address, data, offset, length);
        }
        public final void putCharArray(long address, char[] data, int offset, int length) {
            Foreign.putCharArray(address, data, offset, length);
        }
        public final void getCharArray(long address, char[] data, int offset, int length) {
            Foreign.getCharArray(address, data, offset, length);
        }
        public final void putShortArray(long address, short[] data, int offset, int length) {
            Foreign.putShortArray(address, data, offset, length);
        }
        public final void getShortArray(long address, short[] data, int offset, int length) {
            Foreign.getShortArray(address, data, offset, length);
        }
        public final void putIntArray(long address, int[] data, int offset, int length) {
            Foreign.putIntArray(address, data, offset, length);
        }
        public final void getIntArray(long address, int[] data, int offset, int length) {
            Foreign.getIntArray(address, data, offset, length);
        }
        public final void putLongArray(long address, long[] data, int offset, int length) {
            Foreign.putLongArray(address, data, offset, length);
        }
        public final void getLongArray(long address, long[] data, int offset, int length) {
            Foreign.getLongArray(address, data, offset, length);
        }
        public final void putFloatArray(long address, float[] data, int offset, int length) {
            Foreign.putFloatArray(address, data, offset, length);
        }
        public final void getFloatArray(long address, float[] data, int offset, int length) {
            Foreign.getFloatArray(address, data, offset, length);
        }
        public final void putDoubleArray(long address, double[] data, int offset, int length) {
            Foreign.putDoubleArray(address, data, offset, length);
        }
        public final void getDoubleArray(long address, double[] data, int offset, int length) {
            Foreign.getDoubleArray(address, data, offset, length);
        }
        public final long getStringLength(long address) {
            return Foreign.strlen(address);
        }
        public final byte[] getZeroTerminatedByteArray(long address) {
            return Foreign.getZeroTerminatedByteArray(address);
        }
        public final byte[] getZeroTerminatedByteArray(long address, int maxlen) {
            return Foreign.getZeroTerminatedByteArray(address, maxlen);
        }
        public final void putZeroTerminatedByteArray(long address, byte[] data, int offset, int length) {
            Foreign.putZeroTerminatedByteArray(address, data, offset, length);
        }
    }

    /**
     * A 32 bit optimized implementation of <tt>MemoryIO</tt> using sun.misc.Unsafe
     */
    private static class UnsafeImpl32 extends UnsafeImpl {
        public final long getAddress(long address) {
            return ((long) unsafe.getInt(address)) & ADDRESS_MASK;
        }
        public final void putAddress(long address, long value) {
            unsafe.putInt(address, (int) value);
        }
    }

    /**
     * A 64 bit optimized implementation of <tt>MemoryIO</tt> using sun.misc.Unsafe
     */
    private static class UnsafeImpl64 extends UnsafeImpl {
        public final long getAddress(long address) {
            return unsafe.getLong(address);
        }
        public final void putAddress(long address, long value) {
            unsafe.putLong(address, value);
        }
    }


    /**
     * Verifies that there is are accessor functions (get,put) for a particular
     * primitive type in the sun.misc.Unsafe class.
     *
     * @param unsafeClass The class of sun.misc.Unsafe
     * @param primitive The class of the primitive type.
     * @throws NoSuchMethodException If no accessors for that primitive type exist.
     */
    @SuppressWarnings("unchecked")
    private static void verifyAccessor(Class unsafeClass, Class primitive) throws NoSuchMethodException {
        String primitiveName = primitive.getSimpleName();
        String typeName = primitiveName.substring(0, 1).toUpperCase() + primitiveName.substring(1);
        Method get = unsafeClass.getDeclaredMethod("get" + typeName, new Class[] { long.class });
        if (!get.getReturnType().equals(primitive)) {
            throw new NoSuchMethodException("Incorrect return type for " + get.getName());
        }
        unsafeClass.getDeclaredMethod("put" + typeName, new Class[] { long.class, primitive});
    }

    /**
     * Determines the best Unsafe implementation to use.  Some platforms (e.g. gcj)
     * do not have all the methods that sun.misc.Unsafe does, so we need to check for them.
     *
     * This also handles the case where sun.misc.Unsafe vanishes from future versions
     * of the JVM.
     * @return true if sun.misc.Unsafe is available and usable
     */
    @SuppressWarnings("unchecked")
    static boolean isUnsafeAvailable() {
        try {
            Class sunClass = Class.forName("sun.misc.Unsafe");

            //
            // Verify that all the accessor methods we need are there
            //
            Class[] primitiveTypes = { byte.class, short.class, int.class, long.class, float.class, double.class };
            for (Class type : primitiveTypes) {
                verifyAccessor(sunClass, type);
            }
            //
            // Check for any other methods we need
            //
            sunClass.getDeclaredMethod("getAddress", new Class[] { long.class });
            sunClass.getDeclaredMethod("putAddress", new Class[] { long.class, long.class });
            sunClass.getDeclaredMethod("allocateMemory", new Class[] { long.class });
            sunClass.getDeclaredMethod("freeMemory", new Class[] { long.class });
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }
}
