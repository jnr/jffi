
package com.kenai.jffi;

import java.nio.ByteOrder;

/**
 * Holds objects the native code must handle - such as primitive arrays
 */
public final class ObjectBuffer {
    static final int INDEX_SHIFT = 16;
    static final int INDEX_MASK = 0x00ff0000;
    static final int TYPE_SHIFT = 24;
    static final int TYPE_MASK = 0xff << TYPE_SHIFT;
    static final int PRIM_MASK = 0x0f << TYPE_SHIFT;
    static final int FLAGS_SHIFT = 0;
    static final int FLAGS_MASK = 0xff;
    
    public static final int IN = 0x1;
    public static final int OUT = 0x2;
    public static final int ZERO_TERMINATE = 0x4;

    static final int ARRAY = 0x10 << TYPE_SHIFT;
    static final int BUFFER = 0x20 << TYPE_SHIFT;
    
    static final int BYTE = 0x1 << TYPE_SHIFT;
    static final int SHORT = 0x2 << TYPE_SHIFT;
    static final int INT = 0x3 << TYPE_SHIFT;
    static final int LONG = 0x4 << TYPE_SHIFT;
    static final int FLOAT = 0x5 << TYPE_SHIFT;
    static final int DOUBLE = 0x6 << TYPE_SHIFT;
    
    
    private static final Encoder encoder = newEncoder();
    
    
    private Object[] objects = new Object[3];
    private int[] info = new int[objects.length * 3];
    private int infoIndex = 0;
    private int objectIndex = 0;

    final int objectCount() {
        return objectIndex;
    }
    final int[] info() {
//        for (int i = 0; i < infoIndex; ++i) {
//            System.out.printf("info[%d]=%#x\n", i, info[i]);
//        }
        return info;
    }

    final Object[] objects() {
        return objects;
    }
    private final void ensureSpace() {
        if (objects.length <= (objectIndex + 1)) {
            Object[] newObjects = new Object[objects.length << 1];
            System.arraycopy(objects, 0, newObjects, 0, objectIndex);
            objects = newObjects;
            int[] newInfo = new int[objects.length * 3];
            System.arraycopy(info, 0, newInfo, 0, objectIndex * 3);
            info = newInfo;
        }
    }
    private static final int intToNative(int value) {
        return value;
    }
    private static final int makeArrayFlags(int flags, int type, int index) {
        return (flags & FLAGS_MASK) | ((index << INDEX_SHIFT) & INDEX_MASK) | type;
    }
    public void putArray(int index, byte[] array, int offset, int length, int flags) {
        putObject(array, offset, length, makeArrayFlags(flags, BYTE | ARRAY, index));
    }
    private void putObject(Object array, int offset, int length, int flags) {
        ensureSpace();
        objects[objectIndex++] = array;
        info[infoIndex++] = intToNative(flags);
        info[infoIndex++] = intToNative(offset);
        info[infoIndex++] = intToNative(length);
    }
    private static Encoder newEncoder() {
        if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) {
            return newBigEndianEncoder();
        } else {
            return newLittleEndianEncoder();
        }
    }
    private static Encoder newBigEndianEncoder() {
        return new BigEndianEncoder();
    }
    private static Encoder newLittleEndianEncoder() {
        return new LittleEndianEncoder();
    }
    private static abstract class Encoder {
        abstract int encodeInt(int value);
    }
    private static final class BigEndianEncoder extends Encoder {
        int encodeInt(int value) { return value; }
    }
    private static final class LittleEndianEncoder extends Encoder {
        int encodeInt(int value) { return Integer.reverseBytes(value); }
    }
}
