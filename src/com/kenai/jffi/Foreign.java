/*
 * Copyright (C) 2007, 2008 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Interface to  the foreign function interface.
 */
package com.kenai.jffi;

final class Foreign {
    private static final class SingletonHolder {
        static {
            Init.init();
        }
        private static final Foreign INSTANCE = new Foreign();
    }
    public static final Foreign getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private Foreign() {}
    final native long dlopen(String name, int flags);
    final native void dlclose(long handle);
    final native long dlsym(long handle, String name);

    final native long allocateMemory(long size, boolean clear);
    final native void freeMemory(long address);
    
    final native long newCallContext(int returnType, int[] paramTypes, int convention);
    final native void freeCallContext(long handle);

    final native long newFunction(long address, int returnType, int[] paramTypes, int convention);
    final native void freeFunction(long handle);
    final native boolean isRawParameterPackingEnabled();
    final native int getFunctionRawParameterSize(long handle);
//    public long newClosure(Closure closure, int returnType, int[] paramTypes, int convention);
//    public native void freeClosure(long handle);

    
    final native int invoke32VrI(int function);
    final native int invoke64VrI(long function);
    final native int invoke32IrI(int function, int arg1);
    final native int invoke64IrI(long function, int arg1);
    final native int invoke32IIrI(int function, int arg1, int arg2);
    final native int invoke64IIrI(long function, int arg1, int arg2);
    final native int invoke32IIIrI(int function, int arg1, int arg2, int arg3);
    final native int invoke64IIIrI(long function, int arg1, int arg2, int arg3);
    /* ---------------------------------------------------------------------- */
    final native int invokeArrayInt32(long function, byte[] buffer);
    final native long invokeArrayInt64(long function, byte[] buffer);
    final native float invokeArrayFloat(long function, byte[] buffer);
    final native double invokeArrayDouble(long function, byte[] buffer);
    /* ---------------------------------------------------------------------- */
    final native int invokeArrayWithObjectsInt32(long function, byte[] buffer, int objectCount, int[] objectInfo, Object[] objects);
    final native long invokeArrayWithObjectsInt64(long function, byte[] buffer, int objectCount, int[] objectInfo, Object[] objects);
    final native float invokeArrayWithObjectsFloat(long function, byte[] buffer, int objectCount, int[] objectInfo, Object[] objects);
    final native double invokeArrayWithObjectsDouble(long function, byte[] buffer, int objectCount, int[] objectInfo, Object[] objects);
    /* ---------------------------------------------------------------------- */

    final native byte getByte(long address);
    final native short getShort(long address);
    final native int getInt(long address);
    final native long getLong(long address);
    final native float getFloat(long address);
    final native double getDouble(long address);
    final native long getAddress(long address);
    final native void putByte(long address, byte value);
    final native void putShort(long address, short value);
    final native void putInt(long address, int value);
    final native void putLong(long address, long value);
    final native void putFloat(long address, float value);
    final native void putDouble(long address, double value);
    final native void putAddress(long address, long value);
    final native void setMemory(long address, long size, byte value);
    final native void copyMemory(long src, long dst, long size);
    final native void putByteArray(long address, byte[] data, int offset, int length);
    final native void getByteArray(long address, byte[] data, int offset, int length);
    final native void putCharArray(long address, char[] data, int offset, int length);
    final native void getCharArray(long address, char[] data, int offset, int length);
    final native void putShortArray(long address, short[] data, int offset, int length);
    final native void getShortArray(long address, short[] data, int offset, int length);
    final native void putIntArray(long address, int[] data, int offset, int length);
    final native void getIntArray(long address, int[] data, int offset, int length);
    final native void getLongArray(long address, long[] data, int offset, int length);
    final native void putLongArray(long address, long[] data, int offset, int length);
    final native void getFloatArray(long address, float[] data, int offset, int length);
    final native void putFloatArray(long address, float[] data, int offset, int length);
    final native void getDoubleArray(long address, double[] data, int offset, int length);
    final native void putDoubleArray(long address, double[] data, int offset, int length);
    final native long memchr(long address, int value, long len);
    final native long strlen(long address);
}
