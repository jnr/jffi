
package com.kenai.jffi;

public final class HeapInvocationBuffer implements InvocationBuffer {
    private static final int PARAM_SIZE = 8;
    private static final Encoder encoder = getEncoder();
    private final byte[] buffer;
    private int paramIndex = 0;

    public HeapInvocationBuffer(int paramCount) {
        buffer = new byte[paramCount * PARAM_SIZE];
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
        paramIndex += encoder.putInt8(buffer, paramIndex, value);
    }
    public final void putInt16(final int value) {
        paramIndex += encoder.putInt16(buffer, paramIndex, value);
    }
    public final void putInt32(final int value) {
        paramIndex += encoder.putInt32(buffer, paramIndex, value);
    }
    public final void putInt64(final long value) {
        paramIndex += encoder.putInt64(buffer, paramIndex, value);
    }
    public final void putFloat(final float value) {
        paramIndex += encoder.putFloat32(buffer, paramIndex, value);
    }
    public final void putDouble(final double value) {
        paramIndex += encoder.putFloat64(buffer, paramIndex, value);
    }
    public final void putAddress(final long value) {
        paramIndex += encoder.putAddress(buffer, paramIndex, value);
    }
    private static final Encoder getEncoder() {
        switch (Platform.getArch()) {
            case I386: return getLE32RawEncoder();
            case X86_64: return getLE64Encoder();
            default: throw new RuntimeException("Unsupported arch " + Platform.getArch());
        }
    }
    private static final Encoder getLE32RawEncoder() {
        return LE32RawEncoder.INSTANCE;
    }
    private static final Encoder getLE32Encoder() {
        return LE32Encoder.INSTANCE;
    }
    private static final Encoder getLE64Encoder() {
        return LE64Encoder.INSTANCE;
    }
    private static abstract class Encoder {
        public abstract int putInt8(byte[] buffer, int offset, int value);
        public abstract int putInt16(byte[] buffer, int offset, int value);
        public abstract int putInt32(byte[] buffer, int offset, int value);
        public abstract int putInt64(byte[] buffer, int offset, long value);
        public abstract int putFloat32(byte[] buffer, int offset, float value);
        public abstract int putFloat64(byte[] buffer, int offset, double value);
        public abstract int putAddress(byte[] buffer, int offset, long value);
    }
    private static final class LE32RawEncoder extends Encoder {
        private static final Encoder INSTANCE = new LE32RawEncoder();
        private static final ArrayIO IO = new LE32ArrayIO();

        public final int putInt8(byte[] buffer, int offset, int value) {
            IO.putInt8(buffer, offset, value); return 4;
        }
        public final int putInt16(byte[] buffer, int offset, int value) {
            IO.putInt16(buffer, offset, value); return 4;
        }
        public final int putInt32(byte[] buffer, int offset, int value) {
            IO.putInt32(buffer, offset, value); return 4;
        }
        public final int putInt64(byte[] buffer, int offset, long value) {
            IO.putInt64(buffer, offset, value); return 8;
        }
        public final int putFloat32(byte[] buffer, int offset, float value) {
            IO.putFloat32(buffer, offset, value); return 4;
        }
        public final int putFloat64(byte[] buffer, int offset, double value) {
            IO.putFloat64(buffer, offset, value); return 8;
        }
        public final int putAddress(byte[] buffer, int offset, long value) {
            IO.putAddress(buffer, offset, value); return 4;
        }
    }
    private static abstract class LittleEndianEncoder extends Encoder {
        private final ArrayIO io;

        public LittleEndianEncoder(ArrayIO io) {
            this.io = io;
        }

        public final int putInt8(byte[] buffer, int offset, int value) {
            io.putInt8(buffer, offset, value); return PARAM_SIZE;
        }
        public final int putInt16(byte[] buffer, int offset, int value) {
            io.putInt16(buffer, offset, value); return PARAM_SIZE;
        }
        public final int putInt32(byte[] buffer, int offset, int value) {
            io.putInt32(buffer, offset, value); return PARAM_SIZE;
        }
        public final int putInt64(byte[] buffer, int offset, long value) {
            io.putInt64(buffer, offset, value); return PARAM_SIZE;
        }
        public final int putFloat32(byte[] buffer, int offset, float value) {
            io.putFloat32(buffer, offset, value); return PARAM_SIZE;
        }
        public final int putFloat64(byte[] buffer, int offset, double value) {
            io.putFloat64(buffer, offset, value); return PARAM_SIZE;
        }
        public final int putAddress(byte[] buffer, int offset, long value) {
            io.putAddress(buffer, offset, value); return PARAM_SIZE;
        }
    }
    private static final class LE32Encoder extends LittleEndianEncoder {
        private static final Encoder INSTANCE = new LE32Encoder();

        public LE32Encoder() {
            super(new LE32ArrayIO());
        }
        
    }
    private static final class LE64Encoder extends LittleEndianEncoder {
        private static final Encoder INSTANCE = new LE64Encoder();

        public LE64Encoder() {
            super(new LE64ArrayIO());
        }

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
    private static abstract class LittleEndianArrayIO extends ArrayIO {
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
    }
    private static final class LE32ArrayIO extends LittleEndianArrayIO {
        public void putAddress(byte[] buffer, int offset, long value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
        }
    }
    private static final class LE64ArrayIO extends LittleEndianArrayIO {
        public void putAddress(byte[] buffer, int offset, long value) {
            putInt64(buffer, offset, value);
        }
    }

}
