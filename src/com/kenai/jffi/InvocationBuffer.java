
package com.kenai.jffi;

import java.nio.Buffer;

/**
 * A parameter buffer used when invoking a function
 */
public interface InvocationBuffer {
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
}
