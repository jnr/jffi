
package com.kenai.jffi;

public abstract class MemoryIO {
    private static final class SingletonHolder {
        static {
            Init.init();
        }
        private static final MemoryIO INSTANCE = getImpl();
    }
    public static MemoryIO getMemoryIO() {
        return SingletonHolder.INSTANCE;
    }
    private MemoryIO() {}
    
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
    public native void putByteArray(long address, byte[] data, int offset, int length);
    public native void getByteArray(long address, byte[] data, int offset, int length);
    public native void putCharArray(long address, char[] data, int offset, int length);
    public native void getCharArray(long address, char[] data, int offset, int length);
    public native void putShortArray(long address, short[] data, int offset, int length);
    public native void getShortArray(long address, short[] data, int offset, int length);
    public native void putIntArray(long address, int[] data, int offset, int length);
    public native void getIntArray(long address, int[] data, int offset, int length);
    public native void getLongArray(long address, long[] data, int offset, int length);
    public native void putLongArray(long address, long[] data, int offset, int length);
    public native void getFloatArray(long address, float[] data, int offset, int length);
    public native void putFloatArray(long address, float[] data, int offset, int length);
    public native void getDoubleArray(long address, double[] data, int offset, int length);
    public native void putDoubleArray(long address, double[] data, int offset, int length);

    public long indexOf(long address, byte value) {
        return memchr(address, value, Integer.MAX_VALUE);
    }
    public long indexOf(long address, byte value, int maxlen) {
        return memchr(address, value, maxlen);
    }
    private native long memchr(long address, int value, long len);
    private static final class Native extends MemoryIO {
        public native byte getByte(long address);
        public native short getShort(long address);
        public native int getInt(long address);
        public native long getLong(long address);
        public native float getFloat(long address);
        public native double getDouble(long address);
        public native long getAddress(long address);
        public native void putByte(long address, byte value);
        public native void putShort(long address, short value);
        public native void putInt(long address, int value);
        public native void putLong(long address, long value);
        public native void putFloat(long address, float value);
        public native void putDouble(long address, double value);
        public native void putAddress(long address, long value);
        public native void setMemory(long address, long size, byte value);
        public native void copyMemory(long src, long dst, long size);
    }
    private static final MemoryIO getImpl() {
        return new Native();
    }
}
