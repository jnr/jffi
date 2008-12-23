
package com.kenai.jffi;

public final class HeapInvocationBuffer implements InvocationBuffer {
    private static final int PARAM_SIZE = 8;
    private static final Encoder encoder = getEncoder();
    private final byte[] buffer;
    private int paramIndex = 0;

    public HeapInvocationBuffer(int paramCount) {
        buffer = new byte[paramCount * PARAM_SIZE];
    }
    public HeapInvocationBuffer(Function function) {
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
            case I386: return Foreign.getInstance().isRawParameterPackingEnabled()
                    ? newI386RawEncoder()
                    : newLE32Encoder();
            case X86_64: return newLE64Encoder();
            case PPC: return newBE32Encoder();
            case SPARC: return newBE32Encoder();
            case SPARCV9: return newBE64Encoder();
            default: throw new RuntimeException("Unsupported arch " + Platform.getArch());
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
    private static abstract class Encoder {
        public abstract int getBufferSize(Function function);
        public abstract int putInt8(byte[] buffer, int offset, int value);
        public abstract int putInt16(byte[] buffer, int offset, int value);
        public abstract int putInt32(byte[] buffer, int offset, int value);
        public abstract int putInt64(byte[] buffer, int offset, long value);
        public abstract int putFloat32(byte[] buffer, int offset, float value);
        public abstract int putFloat64(byte[] buffer, int offset, double value);
        public abstract int putAddress(byte[] buffer, int offset, long value);
    }
    private static final class I386RawEncoder extends Encoder {
        private static final ArrayIO IO = LE32ArrayIO.INSTANCE;

        public final int getBufferSize(Function function) {
            return function.getRawParameterSize();
        }
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
    private static final class DefaultEncoder extends Encoder {
        private final ArrayIO io;

        public DefaultEncoder(ArrayIO io) {
            this.io = io;
        }
        public final int getBufferSize(Function function) {
            return function.getParameterCount() * PARAM_SIZE;
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
        public final void putInt8(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
        }
        public final void putInt16(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
        }
        public final void putInt32(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
        }
        public final void putInt64(byte[] buffer, int offset, long value) {
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
        static final ArrayIO INSTANCE = new LE32ArrayIO();
        public final void putAddress(byte[] buffer, int offset, long value) {
            buffer[offset] = (byte) value;
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
        }
    }
    private static final class LE64ArrayIO extends LittleEndianArrayIO {
        static final ArrayIO INSTANCE = new LE64ArrayIO();
        public final void putAddress(byte[] buffer, int offset, long value) {
            putInt64(buffer, offset, value);
        }
    }
    private static abstract class BigEndianArrayIO extends ArrayIO {
        public final void putInt8(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte) value;
        }
        public final void putInt16(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte) (value >> 8);
            buffer[offset + 1] = (byte) value;
            
        }
        public final void putInt32(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte) (value >> 24);
            buffer[offset + 1] = (byte) (value >> 16);
            buffer[offset + 2] = (byte) (value >> 8);
            buffer[offset + 3] = (byte) value;
        }
        public final void putInt64(byte[] buffer, int offset, long value) {
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
    private static final class BE32ArrayIO extends BigEndianArrayIO {
        static final ArrayIO INSTANCE = new BE32ArrayIO();
        public void putAddress(byte[] buffer, int offset, long value) {
            buffer[offset + 0] = (byte) (value >> 24);
            buffer[offset + 1] = (byte) (value >> 16);
            buffer[offset + 2] = (byte) (value >> 8);
            buffer[offset + 3] = (byte) value;
        }

    }
    private static final class BE64ArrayIO extends BigEndianArrayIO {
        static final ArrayIO INSTANCE = new BE64ArrayIO();
        public void putAddress(byte[] buffer, int offset, long value) {
            putInt64(buffer, offset, value);
        }
    }
}
