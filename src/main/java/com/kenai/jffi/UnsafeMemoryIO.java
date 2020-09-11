package com.kenai.jffi;

import java.lang.reflect.Field;

/**
 * An implementation of <code>MemoryIO</code> using sun.misc.Unsafe
 */
public abstract class UnsafeMemoryIO extends MemoryIO {
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

    /**
     * A 32 bit optimized implementation of <code>MemoryIO</code> using sun.misc.Unsafe
     */
    static class UnsafeMemoryIO32 extends UnsafeMemoryIO {
        public final long getAddress(long address) {
            return ((long) unsafe.getInt(address)) & ADDRESS_MASK;
        }
        public final void putAddress(long address, long value) {
            unsafe.putInt(address, (int) value);
        }
    }

    /**
     * A 64 bit optimized implementation of <code>MemoryIO</code> using sun.misc.Unsafe
     */
    static class UnsafeMemoryIO64 extends UnsafeMemoryIO {
        public final long getAddress(long address) {
            return unsafe.getLong(address);
        }
        public final void putAddress(long address, long value) {
            unsafe.putLong(address, value);
        }
    }
}
