
package com.kenai.jffi;

public abstract class MemoryIO {
    protected final Foreign foreign = Foreign.getForeign();
    private static final class SingletonHolder {
        private static final MemoryIO INSTANCE = getImpl();
    }
    public static MemoryIO getMemoryIO() {
        return SingletonHolder.INSTANCE;
    }
    private MemoryIO() {}

    public abstract long allocateMemory(long size, boolean clear);
    public abstract  void freeMemory(long address);
    public abstract byte getByte(long address);
    public abstract short getShort(long address);
    public abstract int getInt(long address);
    public abstract long getLong(long address);
    public abstract float getFloat(long address);
    public abstract double getDouble(long address);
    public abstract long getAddress(long address);
    public abstract void putByte(long address, byte value);
    public abstract void putShort(long address, short value);
    public abstract void putInt(long address, int value);
    public abstract void putLong(long address, long value);
    public abstract void putFloat(long address, float value);
    public abstract void putDouble(long address, double value);
    public abstract void putAddress(long address, long value);
    public abstract void copyMemory(long src, long dst, long size);
    public abstract void setMemory(long src, long size, byte value);
    public final void putByteArray(long address, byte[] data, int offset, int length) {
        foreign.putByteArray(address, data, offset, length);
    }
    public final void getByteArray(long address, byte[] data, int offset, int length) {
        foreign.getByteArray(address, data, offset, length);
    }
    public final void putCharArray(long address, char[] data, int offset, int length) {
        foreign.putCharArray(address, data, offset, length);
    }
    public final void getCharArray(long address, char[] data, int offset, int length) {
        foreign.getCharArray(address, data, offset, length);
    }
    public final void putShortArray(long address, short[] data, int offset, int length) {
        foreign.putShortArray(address, data, offset, length);
    }
    public final void getShortArray(long address, short[] data, int offset, int length) {
        foreign.getShortArray(address, data, offset, length);
    }
    public final void putIntArray(long address, int[] data, int offset, int length) {
        foreign.putIntArray(address, data, offset, length);
    }
    public final void getIntArray(long address, int[] data, int offset, int length) {
        foreign.getIntArray(address, data, offset, length);
    }
    public final void getLongArray(long address, long[] data, int offset, int length) {
        foreign.getLongArray(address, data, offset, length);
    }
    public final void putLongArray(long address, long[] data, int offset, int length) {
        foreign.putLongArray(address, data, offset, length);
    }
    public final void getFloatArray(long address, float[] data, int offset, int length) {
        foreign.getFloatArray(address, data, offset, length);
    }
    public final void putFloatArray(long address, float[] data, int offset, int length) {
        foreign.putFloatArray(address, data, offset, length);
    }
    public final void getDoubleArray(long address, double[] data, int offset, int length) {
        foreign.getDoubleArray(address, data, offset, length);
    }
    public final void putDoubleArray(long address, double[] data, int offset, int length) {
        foreign.putDoubleArray(address, data, offset, length);
    }

    public final long indexOf(long address, byte value) {
        return foreign.memchr(address, value, Integer.MAX_VALUE);
    }
    public final long indexOf(long address, byte value, int maxlen) {
        return foreign.memchr(address, value, maxlen);
    }

    private static final class Native extends MemoryIO {
        public final byte getByte(long address) {
            return foreign.getByte(address);
        }
        public final short getShort(long address) {
            return foreign.getShort(address);
        }
        public final int getInt(long address) {
            return foreign.getInt(address);
        }
        public final long getLong(long address) {
            return foreign.getLong(address);
        }
        public final float getFloat(long address) {
            return foreign.getFloat(address);
        }
        public final double getDouble(long address) {
            return foreign.getDouble(address);
        }
        public final long getAddress(long address) {
            return foreign.getAddress(address);
        }
        public final void putByte(long address, byte value) {
            foreign.putByte(address, value);
        }
        public final void putShort(long address, short value) {
            foreign.putShort(address, value);
        }
        public final void putInt(long address, int value) {
            foreign.putInt(address, value);
        }
        public final void putLong(long address, long value) {
            foreign.putLong(address, value);
        }
        public final void putFloat(long address, float value) {
            foreign.putFloat(address, value);
        }
        public final void putDouble(long address, double value) {
            foreign.putDouble(address, value);
        }
        public final void putAddress(long address, long value) {
            foreign.putAddress(address, value);
        }
        public final void setMemory(long address, long size, byte value) {
            foreign.setMemory(address, size, value);
        }
        public final void copyMemory(long src, long dst, long size) {
            foreign.copyMemory(src, dst, size);
        }
        public final long allocateMemory(long size, boolean clear) {
            return foreign.allocateMemory(size, clear);
        }
        public final void freeMemory(long address) {
            foreign.freeMemory(address);
        }
    }
    private static final MemoryIO getImpl() {
        return new Native();
    }
}
