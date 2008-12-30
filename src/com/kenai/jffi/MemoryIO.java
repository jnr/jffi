
package com.kenai.jffi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class MemoryIO {
    private final Foreign foreign = Foreign.getInstance();
    private static final long ADDRESS_MASK = Platform.getPlatform().addressMask();
    private static final class SingletonHolder {
        private static final MemoryIO INSTANCE = getImpl();
    }
    public static MemoryIO getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private MemoryIO() {}
    private static final MemoryIO getImpl() {
        try {
            return !Boolean.getBoolean("jffi.unsafe.disable") && isUnsafeAvailable()
                    ? newUnsafeImpl() : newNativeImpl();
        } catch (Throwable t) {
            return newNativeImpl();
        }
    }
    private static final MemoryIO newNativeImpl() {
        return new NativeImpl();
    }
    private static final MemoryIO newUnsafeImpl() {
        return new UnsafeImpl();
    }
    
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
    public final long allocateMemory(long size, boolean clear) {
        return foreign.allocateMemory(size, clear);
    }
    public final void freeMemory(long address) {
        foreign.freeMemory(address);
    }
    public final long getStringLength(long address) {
        return foreign.strlen(address);
    }
    public final long indexOf(long address, byte value) {
        return foreign.memchr(address, value, Integer.MAX_VALUE);
    }
    public final long indexOf(long address, byte value, int maxlen) {
        return foreign.memchr(address, value, maxlen);
    }

    private static final class NativeImpl extends MemoryIO {
        static { System.out.println("Loading NativeImpl"); }
        private static final Foreign foreign = Foreign.getInstance();
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
            return foreign.getAddress(address) & ADDRESS_MASK;
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
    }
    private static final class UnsafeImpl extends MemoryIO {
        static { System.out.println("Loading UnsafeImpl"); }
        private static sun.misc.Unsafe unsafe = sun.misc.Unsafe.class.cast(getUnsafe());
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
        public final long getAddress(long address) {
            return unsafe.getAddress(address);
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
        public final void putAddress(long address, long value) {
            unsafe.putAddress(address, value);
        }
        public final void copyMemory(long src, long dst, long size) {
            unsafe.copyMemory(src, dst, size);
        }
        public final void setMemory(long src, long size, byte value) {
            unsafe.setMemory(src, size, value);
        }
        
    }
    @SuppressWarnings("unchecked")
    private static final void verifyAccessor(Class unsafeClass, Class primitive) throws NoSuchMethodException {
        String primitiveName = primitive.getSimpleName();
        String typeName = primitiveName.substring(0, 1).toUpperCase() + primitiveName.substring(1);
        Method get = unsafeClass.getDeclaredMethod("get" + typeName, new Class[] { long.class });
        if (!get.getReturnType().equals(primitive)) {
            throw new NoSuchMethodException("Incorrect return type for " + get.getName());
        }
        unsafeClass.getDeclaredMethod("put" + typeName, new Class[] { long.class, primitive});
    }

    /**
     * Determines the best Unsafe implementation to use.  Some platforms (e.g. gcj)
     * do not have all the methods that sun.misc.Unsafe does, so we need to check for them.
     *
     * This also handles the case where sun.misc.Unsafe vanishes from future versions
     * of the JVM.
     * @return
     */
    @SuppressWarnings("unchecked")
    static final boolean isUnsafeAvailable() {
        try {
            Class sunClass = Class.forName("sun.misc.Unsafe");

            //
            // Verify that all the accessor methods we need are there
            //
            Class[] primitiveTypes = { byte.class, short.class, int.class, long.class, float.class, double.class };
            for (Class type : primitiveTypes) {
                verifyAccessor(sunClass, type);
            }
            //
            // Check for any other methods we need
            //
            sunClass.getDeclaredMethod("getAddress", new Class[] { long.class });
            sunClass.getDeclaredMethod("putAddress", new Class[] { long.class, long.class });
            sunClass.getDeclaredMethod("allocateMemory", new Class[] { long.class });
            sunClass.getDeclaredMethod("freeMemory", new Class[] { long.class });
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }
}
