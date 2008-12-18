
package com.kenai.jffi;

public interface InvocationBuffer {
    public abstract void putInt8(final int value);
    public abstract void putInt16(final int value);
    public abstract void putInt32(final int value);
    public abstract void putInt64(final long value);
    public abstract void putFloat(final float value);
    public abstract void putDouble(final double value);
    
}
