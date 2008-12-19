/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jffi;

/**
 *
 * @author wayne
 */
public final class HeapInvocationBuffer implements InvocationBuffer {
    private static final ArrayIO io = new L32ArrayIO();
    private final byte[] buffer;
    private int paramIndex = 0;

    public HeapInvocationBuffer(int paramCount) {
        buffer = new byte[paramCount * 8];
    }
    /**
     * Gets the backing array of this <tt>InvocationBuffer</tt>
     *
     * @return The backing array for this buffer.
     */
    byte[] array() {
        return buffer;
    }
    public final void putInt8(final int value) {
        io.putInt8(buffer, paramIndex++ * 8, value);
    }
    public final void putInt16(final int value) {
        io.putInt16(buffer, paramIndex++ * 8, value);
    }
    public final void putInt32(final int value) {
        io.putInt32(buffer, paramIndex++ * 8, value);
    }
    public final void putInt64(final long value) {
        io.putInt64(buffer, paramIndex++ * 8, value);
    }
    public final void putFloat(final float value) {
        io.putFloat32(buffer, paramIndex++ * 8, value);
    }
    public final void putDouble(final double value) {
        io.putFloat64(buffer, paramIndex++ * 8, value);
    }
    public final void putAddress(final long value) {
        io.putAddress(buffer, paramIndex++ * 8, value);
    }
    private static abstract class ArrayIO {
        public abstract void putInt8(byte[] buffer, int offset, int value);
        public abstract void putInt16(byte[] buffer, int offset, int value);
        public abstract void putInt32(byte[] buffer, int offset, int value);
        public abstract void putInt64(byte[] buffer, int offset, long value);
        public final void putFloat32(byte[] buffer, int offset, float value) {
            putInt32(buffer, offset, Float.floatToRawIntBits(value));
        }
        public final void putFloat64(byte[] buffer, int offset, double value) {
            putInt64(buffer, offset, Double.doubleToRawLongBits(value));
        }
        public abstract void putAddress(byte[] buffer, int offset, long value);
    }
    private static class L32ArrayIO extends ArrayIO {
        public void putInt8(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
        }
        public void putInt16(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
        }
        public void putInt32(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
        }
        public void putInt64(byte[] buffer, int offset, long value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
            buffer[offset + 4] = (byte) (value >> 32);
            buffer[offset + 5] = (byte) (value >> 40);
            buffer[offset + 6] = (byte) (value >> 48);
            buffer[offset + 7] = (byte) (value >> 56);
        }
        public void putAddress(byte[] buffer, int offset, long value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
        }
    }

}
