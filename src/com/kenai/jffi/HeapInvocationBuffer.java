
package com.kenai.jffi;

import java.nio.ByteOrder;

/**
 * An implementation of {@link InvocationBuffer} that packs its parameters onto
 * a java heap allocated buffer.
 */
public final class HeapInvocationBuffer implements InvocationBuffer {
    private static final int FFI_SIZEOF_ARG = Platform.getPlatform().addressSize() / 8;
    private static final int PARAM_SIZE = 8;
    private static final Encoder encoder = getEncoder();
    private final Function function;
    private final byte[] buffer;
    private ObjectBuffer objectBuffer = null;
    private int paramOffset = 0;
    private int paramIndex = 0;

    /**
     * Creates a new instance of <tt>HeapInvocationBuffer</tt>.
     *
     * @param function The function that this buffer is going to be used with.
     */
    public HeapInvocationBuffer(Function function) {
        this.function = function;
        buffer = new byte[encoder.getBufferSize(function)];
    }
    
    /**
     * Gets the backing array of this <tt>InvocationBuffer</tt>
     *
     * @return The backing array for this buffer.
     */
    byte[] array() {
        return buffer;
    }

    /**
     * Gets the object buffer used to store java heap array parameters
     *
     * @return An <tt>ObjectBuffer</tt>
     */
    ObjectBuffer objectBuffer() {
        return objectBuffer;
    }
    
    public final void putByte(final int value) {
        paramOffset = encoder.putByte(buffer, paramOffset, value);
        ++paramIndex;
    }

    public final void putShort(final int value) {
        paramOffset = encoder.putShort(buffer, paramOffset, value);
        ++paramIndex;
    }

    public final void putInt(final int value) {
        paramOffset = encoder.putInt(buffer, paramOffset, value);
        ++paramIndex;
    }

    public final void putLong(final long value) {
        paramOffset = encoder.putLong(buffer, paramOffset, value);
        ++paramIndex;
    }

    public final void putFloat(final float value) {
        paramOffset = encoder.putFloat(buffer, paramOffset, value);
        ++paramIndex;
    }

    public final void putDouble(final double value) {
        paramOffset = encoder.putDouble(buffer, paramOffset, value);
        ++paramIndex;
    }

    public final void putAddress(final long value) {
        paramOffset = encoder.putAddress(buffer, paramOffset, value);
        ++paramIndex;
    }

    private final ObjectBuffer getObjectBuffer() {
        if (objectBuffer == null) {
            objectBuffer = new ObjectBuffer();
        }

        return objectBuffer;
    }

    public final void putArray(final byte[] array, int offset, int length, int flags) {
        paramOffset = encoder.putAddress(buffer, paramOffset, 0L);
        getObjectBuffer().putArray(paramIndex++, array, offset, length, flags);
    }

    public final void putArray(final short[] array, int offset, int length, int flags) {
        paramOffset = encoder.putAddress(buffer, paramOffset, 0L);
        getObjectBuffer().putArray(paramIndex++, array, offset, length, flags);
    }

    public final void putArray(final int[] array, int offset, int length, int flags) {
        paramOffset = encoder.putAddress(buffer, paramOffset, 0L);
        getObjectBuffer().putArray(paramIndex++, array, offset, length, flags);
    }

    public final void putArray(final long[] array, int offset, int length, int flags) {
        paramOffset = encoder.putAddress(buffer, paramOffset, 0L);
        getObjectBuffer().putArray(paramIndex++, array, offset, length, flags);
    }

    public final void putArray(final float[] array, int offset, int length, int flags) {
        paramOffset = encoder.putAddress(buffer, paramOffset, 0L);
        getObjectBuffer().putArray(paramIndex++, array, offset, length, flags);
    }

    public final void putArray(final double[] array, int offset, int length, int flags) {
        paramOffset = encoder.putAddress(buffer, paramOffset, 0L);
        getObjectBuffer().putArray(paramIndex++, array, offset, length, flags);
    }

    public final void putDirectBuffer(final java.nio.Buffer value, int offset, int length) {
        paramOffset = encoder.putAddress(buffer, paramOffset, 0L);
        getObjectBuffer().putDirectBuffer(paramIndex++, value, offset, length);
    }

    public final void putStruct(final byte[] struct, int offset) {
        final Type type = function.getParameterType(paramIndex);

        if (encoder.isRaw()) {
            paramOffset = FFI_ALIGN(paramOffset, type.align);
            System.arraycopy(struct, offset, buffer, paramOffset, type.size);
            paramOffset = FFI_ALIGN(paramOffset + type.size, FFI_SIZEOF_ARG);
        } else {
            paramOffset = encoder.putAddress(buffer, paramOffset, 0L);
            getObjectBuffer().putArray(paramIndex, struct, offset, type.size, ObjectBuffer.IN);
        }
        ++paramIndex;
    }

    public final void putStruct(final long struct) {
        final Type type = function.getParameterType(paramIndex);

        if (encoder.isRaw()) {
            paramOffset = FFI_ALIGN(paramOffset, type.align);
            MemoryIO.getInstance().getByteArray(struct, buffer, paramOffset, type.size);
            paramOffset = FFI_ALIGN(paramOffset + type.size, FFI_SIZEOF_ARG);
        } else {
            paramOffset = encoder.putAddress(buffer, paramOffset, struct);
        }
        ++paramIndex;
    }

    public final void putJNIEnvironment() {
        paramOffset = encoder.putAddress(buffer, paramOffset, 0L);
        getObjectBuffer().putJNI(paramIndex++, ObjectBuffer.JNIENV);
    }

    private static final Encoder getEncoder() {
        if (Platform.getPlatform().getCPU() == Platform.CPU.I386) {
            return Foreign.getInstance().isRawParameterPackingEnabled()
                    ? newI386RawEncoder()
                    : newLE32Encoder();
        } else if (Platform.getPlatform().addressSize() == 64) {
            return ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)
                    ? newBE64Encoder() : newLE64Encoder();
        } else {
            return ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)
                    ? newBE32Encoder() : newLE32Encoder();
        }
    }
    private static final Encoder newI386RawEncoder() {
        return new I386RawEncoder();
    }
    private static final Encoder newLE32Encoder() {
        return new DefaultEncoder(LE32ArrayIO.INSTANCE);
    }
    private static final Encoder newLE64Encoder() {
        return new DefaultEncoder(LE64ArrayIO.INSTANCE);
    }
    private static final Encoder newBE32Encoder() {
        return new DefaultEncoder(BE32ArrayIO.INSTANCE);
    }
    private static final Encoder newBE64Encoder() {
        return new DefaultEncoder(BE64ArrayIO.INSTANCE);
    }

    /**
     * Encodes java data types into native parameter frames
     */
    private static abstract class Encoder {
        /** Returns true if this <tt>Encoder</tt> is a raw encoder */
        public abstract boolean isRaw();

        /** Gets the size in bytes of the buffer required for the function */
        public abstract int getBufferSize(Function function);

        /**
         * Encodes a byte value into the byte array.
         *
         * @param buffer The destination byte buffer to place the encoded value.
         * @param offset The offset within the destination buffer to place the value.
         * @param value The value to encode.
         * @return The number of bytes consumed in encoding the value.
         */
        public abstract int putByte(byte[] buffer, int offset, int value);

        /**
         * Encodes a short value into the byte array.
         *
         * @param buffer The destination byte buffer to place the encoded value.
         * @param offset The offset within the destination buffer to place the value.
         * @param value The value to encode.
         * @return The number of bytes consumed in encoding the value.
         */
        public abstract int putShort(byte[] buffer, int offset, int value);

        /**
         * Encodes an int value into the byte array.
         *
         * @param buffer The destination byte buffer to place the encoded value.
         * @param offset The offset within the destination buffer to place the value.
         * @param value The value to encode.
         * @return The number of bytes consumed in encoding the value.
         */
        public abstract int putInt(byte[] buffer, int offset, int value);

        /**
         * Encodes a long value into the byte array.
         *
         * @param buffer The destination byte buffer to place the encoded value.
         * @param offset The offset within the destination buffer to place the value.
         * @param value The value to encode.
         * @return The number of bytes consumed in encoding the value.
         */
        public abstract int putLong(byte[] buffer, int offset, long value);

        /**
         * Encodes a float value into the byte array.
         *
         * @param buffer The destination byte buffer to place the encoded value.
         * @param offset The offset within the destination buffer to place the value.
         * @param value The value to encode.
         * @return The number of bytes consumed in encoding the value.
         */
        public abstract int putFloat(byte[] buffer, int offset, float value);

        /**
         * Encodes a double value into the byte array.
         *
         * @param buffer The destination byte buffer to place the encoded value.
         * @param offset The offset within the destination buffer to place the value.
         * @param value The value to encode.
         * @return The number of bytes consumed in encoding the value.
         */
        public abstract int putDouble(byte[] buffer, int offset, double value);

        /**
         * Encodes a native memory address value into the byte array.
         *
         * @param buffer The destination byte buffer to place the encoded value.
         * @param offset The offset within the destination buffer to place the value.
         * @param value The value to encode.
         * @return The number of bytes consumed in encoding the value.
         */
        public abstract int putAddress(byte[] buffer, int offset, long value);
    }

    /**
     * Packs arguments into a byte array in the format compliant with the
     * i386 sysv ABI, so the buffer can be copied directly onto the stack and
     * used.
     */
    private static final class I386RawEncoder extends Encoder {
        private static final ArrayIO IO = LE32ArrayIO.INSTANCE;

        public final boolean isRaw() {
            return true;
        }

        public final int getBufferSize(Function function) {
            return function.getRawParameterSize();
        }
        public final int putByte(byte[] buffer, int offset, int value) {
            IO.putByte(buffer, offset, value); return offset + 4;
        }
        public final int putShort(byte[] buffer, int offset, int value) {
            IO.putShort(buffer, offset, value); return offset + 4;
        }
        public final int putInt(byte[] buffer, int offset, int value) {
            IO.putInt(buffer, offset, value); return offset + 4;
        }
        public final int putLong(byte[] buffer, int offset, long value) {
            IO.putLong(buffer, offset, value); return offset + 8;
        }
        public final int putFloat(byte[] buffer, int offset, float value) {
            IO.putFloat(buffer, offset, value); return offset + 4;
        }
        public final int putDouble(byte[] buffer, int offset, double value) {
            IO.putDouble(buffer, offset, value); return offset + 8;
        }
        public final int putAddress(byte[] buffer, int offset, long value) {
            IO.putAddress(buffer, offset, value); return offset + 4;
        }
    }
    private static final class DefaultEncoder extends Encoder {
        private final ArrayIO io;

        public DefaultEncoder(ArrayIO io) {
            this.io = io;
        }

        public final boolean isRaw() {
            return false;
        }
        
        public final int getBufferSize(Function function) {
            return function.getParameterCount() * PARAM_SIZE;
        }
        public final int putByte(byte[] buffer, int offset, int value) {
            io.putByte(buffer, offset, value); return offset + PARAM_SIZE;
        }
        public final int putShort(byte[] buffer, int offset, int value) {
            io.putShort(buffer, offset, value); return offset + PARAM_SIZE;
        }
        public final int putInt(byte[] buffer, int offset, int value) {
            io.putInt(buffer, offset, value); return offset + PARAM_SIZE;
        }
        public final int putLong(byte[] buffer, int offset, long value) {
            io.putLong(buffer, offset, value); return offset + PARAM_SIZE;
        }
        public final int putFloat(byte[] buffer, int offset, float value) {
            io.putFloat(buffer, offset, value); return offset + PARAM_SIZE;
        }
        public final int putDouble(byte[] buffer, int offset, double value) {
            io.putDouble(buffer, offset, value); return offset + PARAM_SIZE;
        }
        public final int putAddress(byte[] buffer, int offset, long value) {
            io.putAddress(buffer, offset, value); return offset + PARAM_SIZE;
        }
    }

    private static abstract class ArrayIO {
        public abstract void putByte(byte[] buffer, int offset, int value);
        public abstract void putShort(byte[] buffer, int offset, int value);
        public abstract void putInt(byte[] buffer, int offset, int value);
        public abstract void putLong(byte[] buffer, int offset, long value);
        public final void putFloat(byte[] buffer, int offset, float value) {
            putInt(buffer, offset, Float.floatToRawIntBits(value));
        }
        public final void putDouble(byte[] buffer, int offset, double value) {
            putLong(buffer, offset, Double.doubleToRawLongBits(value));
        }
        public abstract void putAddress(byte[] buffer, int offset, long value);
    }

    /**
     * Base class for all little-endian architecture array encoders.
     */
    private static abstract class LittleEndianArrayIO extends ArrayIO {
        public final void putByte(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
        }
        public final void putShort(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
        }
        public final void putInt(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
        }
        public final void putLong(byte[] buffer, int offset, long value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
            buffer[offset + 4] = (byte) (value >> 32);
            buffer[offset + 5] = (byte) (value >> 40);
            buffer[offset + 6] = (byte) (value >> 48);
            buffer[offset + 7] = (byte) (value >> 56);
        }
    }

    /**
     * Little endian, 32 bit implementation of <tt>ArrayIO</tt>
     */
    private static final class LE32ArrayIO extends LittleEndianArrayIO {
        static final ArrayIO INSTANCE = new LE32ArrayIO();
        public final void putAddress(byte[] buffer, int offset, long value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
        }
    }

    /**
     * Little endian, 64 bit implementation of <tt>ArrayIO</tt>
     */
    private static final class LE64ArrayIO extends LittleEndianArrayIO {
        static final ArrayIO INSTANCE = new LE64ArrayIO();
        public final void putAddress(byte[] buffer, int offset, long value) {
            putLong(buffer, offset, value);
        }
    }

    /**
     * Base class for all big-endian architecture array encoders.
     */
    private static abstract class BigEndianArrayIO extends ArrayIO {

        public final void putByte(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
        }

        public final void putShort(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte) (value >> 8);
            buffer[offset + 1] = (byte) value;
            
        }

        public final void putInt(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte) (value >> 24);
            buffer[offset + 1] = (byte) (value >> 16);
            buffer[offset + 2] = (byte) (value >> 8);
            buffer[offset + 3] = (byte) value;
        }

        public final void putLong(byte[] buffer, int offset, long value) {
            buffer[offset + 0] = (byte) (value >> 56);
            buffer[offset + 1] = (byte) (value >> 48);
            buffer[offset + 2] = (byte) (value >> 40);
            buffer[offset + 3] = (byte) (value >> 32);
            buffer[offset + 4] = (byte) (value >> 24);
            buffer[offset + 5] = (byte) (value >> 16);
            buffer[offset + 6] = (byte) (value >> 8);
            buffer[offset + 7] = (byte) value;
        }
    }

    /**
     * Big endian, 32 bit array encoder
     */
    private static final class BE32ArrayIO extends BigEndianArrayIO {
        static final ArrayIO INSTANCE = new BE32ArrayIO();
        public void putAddress(byte[] buffer, int offset, long value) {
            buffer[offset + 0] = (byte) (value >> 24);
            buffer[offset + 1] = (byte) (value >> 16);
            buffer[offset + 2] = (byte) (value >> 8);
            buffer[offset + 3] = (byte) value;
        }

    }

    /**
     * Big endian, 64 bit array encoder
     */
    private static final class BE64ArrayIO extends BigEndianArrayIO {
        static final ArrayIO INSTANCE = new BE64ArrayIO();
        public void putAddress(byte[] buffer, int offset, long value) {
            putLong(buffer, offset, value);
        }
    }

    /**
     * Aligns an address to a boundary
     *
     * @param v The address to roundup
     * @param a The boundary to align to.
     * @return The aligned address.
     */
    private static final int FFI_ALIGN(int v, int a) {
        return ((v - 1) | (a - 1)) + 1;
    }

}
