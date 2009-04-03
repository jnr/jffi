
package com.kenai.jffi;

import java.nio.Buffer;

public interface InvocationBuffer {
    public abstract void putByte(final int value);
    public abstract void putShort(final int value);
    public abstract void putInt(final int value);
    public abstract void putLong(final long value);
    public abstract void putFloat(final float value);
    public abstract void putDouble(final double value);
    public abstract void putAddress(final long value);
    public abstract void putArray(final byte[] value, int offset, int length, int flags);
    public abstract void putArray(final short[] value, int offset, int length, int flags);
    public abstract void putArray(final int[] value, int offset, int length, int flags);
    public abstract void putArray(final long[] value, int offset, int length, int flags);
    public abstract void putArray(final float[] value, int offset, int length, int flags);
    public abstract void putArray(final double[] value, int offset, int length, int flags);
    public abstract void putDirectBuffer(final Buffer buffer, int offset, int length);
}
