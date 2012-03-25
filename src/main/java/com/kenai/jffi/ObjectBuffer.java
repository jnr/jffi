/*
 * Copyright (C) 2008, 2009 Wayne Meissner
 *
 * This file is part of jffi.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 
 * Alternatively, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kenai.jffi;

/**
 * Holds objects the native code must handle - such as primitive arrays
 */
final class ObjectBuffer {
    /** Copy the array contents to native memory before calling the function */
    public static final int IN = 0x1;

    /** After calling the function, reload the array contents from native memory */
    public static final int OUT = 0x2;

    /** Append a NUL byte to the array contents after copying to native memory */
    public static final int ZERO_TERMINATE = 0x4;

    /** Pin the array memory and pass the JVM memory pointer directly to the function */
    public static final int PINNED = 0x8;

    /** For OUT arrays, clear the temporary native memory area */
    public static final int CLEAR = 0x10;

    /*
     * WARNING: The following flags cannot be altered without recompiling the native code 
     */
    static final int INDEX_SHIFT = 16;
    static final int INDEX_MASK = 0x00ff0000;
    static final int TYPE_SHIFT = 24;
    static final int TYPE_MASK = 0xff << TYPE_SHIFT;
    static final int PRIM_MASK = 0x0f << TYPE_SHIFT;
    static final int FLAGS_SHIFT = 0;
    static final int FLAGS_MASK = 0xff;

    static final int ARRAY = 0x10 << TYPE_SHIFT;
    static final int BUFFER = 0x20 << TYPE_SHIFT;
    static final int JNI = 0x40 << TYPE_SHIFT;
    
    static final int BYTE = 0x1 << TYPE_SHIFT;
    static final int SHORT = 0x2 << TYPE_SHIFT;
    static final int INT = 0x3 << TYPE_SHIFT;
    static final int LONG = 0x4 << TYPE_SHIFT;
    static final int FLOAT = 0x5 << TYPE_SHIFT;
    static final int DOUBLE = 0x6 << TYPE_SHIFT;
    static final int BOOLEAN = 0x7 << TYPE_SHIFT;
    static final int CHAR = 0x8 << TYPE_SHIFT;

    /* NOTE: The JNI types can overlap the primitive type, since they are mutually exclusive */
    /** The JNIEnv address */
    public static final int JNIENV = 0x1 << TYPE_SHIFT;

    /** The jobject handle */
    public static final int JNIOBJECT = 0x2 << TYPE_SHIFT;

    /** The objects stored in this buffer */
    private Object[] objects;

    /** 
     * The flags/offset/length descriptor array.
     *
     * Along with each object, a 3-tuple is stored in the descriptor array.
     *
     * The first element of the tuple stores a mask of the type, parameter index and array flags
     * The second element stores the offset within the array the data starts.
     * The third element stores the length of data.
     */
    private int[] info;

    /** The index of the next descriptor storage slot */
    private int infoIndex = 0;

    /** The index of the next object storage slot */
    private int objectIndex = 0;

    ObjectBuffer() {
        objects = new Object[1];
        info = new int[objects.length * 3];
    }

    ObjectBuffer(int objectCount) {
        objects = new Object[objectCount];
        info = new int[objectCount * 3];
    }



    /**
     * Gets the number of objects stored in this <tt>ObjectBuffer</tt>.
     *
     * @return the number of objects already stored.
     */
    final int objectCount() {
        return objectIndex;
    }

    /**
     * Gets the object descriptor array.
     *
     * @return An array of integers describing the objects stored.
     */
    final int[] info() {
        return info;
    }

    /**
     * Gets the array of stored objects.
     *
     * @return An array of objects stored in this buffer.
     */
    final Object[] objects() {
        return objects;
    }
    /** Ensures that sufficient space is available to insert at least one more object */
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

    /**
     * Encodes the native object flags for an array.
     *
     * @param ioflags The array flags (IN, OUT) for the object.
     * @param type The type of the object.
     * @param index The parameter index the object should be passed as.
     * @return A bitmask of flags.
     */
    static final int makeObjectFlags(int ioflags, int type, int index) {
        return (ioflags & FLAGS_MASK) | ((index << INDEX_SHIFT) & INDEX_MASK) | type;
    }

    /**
     * Encodes the native object flags for an NIO Buffer.
     *
     * @param index The parameter index of the buffer.
     * @return A bitmask of flags.
     */
    static final int makeBufferFlags(int index) {
        return ((index << INDEX_SHIFT) & INDEX_MASK) | BUFFER;
    }

    private static final int makeJNIFlags(int index, int type) {
        return ((index << INDEX_SHIFT) & INDEX_MASK) | JNI | type;
    }

    /**
     * Adds a java byte array as a pointer parameter.
     *
     * @param array The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT, NULTERMINATE)
     */
    public void putArray(int index, byte[] array, int offset, int length, int flags) {
        putObject(array, offset, length, makeObjectFlags(flags, BYTE | ARRAY, index));
    }

    /**
     * Adds a java short array as a pointer parameter.
     *
     * @param array The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public void putArray(int index, short[] array, int offset, int length, int flags) {
        putObject(array, offset, length, makeObjectFlags(flags, SHORT | ARRAY, index));
    }

    /**
     * Adds a java int array as a pointer parameter.
     *
     * @param array The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public void putArray(int index, int[] array, int offset, int length, int flags) {
        putObject(array, offset, length, makeObjectFlags(flags, INT | ARRAY, index));
    }

    /**
     * Adds a java long array as a pointer parameter.
     *
     * @param array The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public void putArray(int index, long[] array, int offset, int length, int flags) {
        putObject(array, offset, length, makeObjectFlags(flags, LONG | ARRAY, index));
    }

    /**
     * Adds a java float array as a pointer parameter.
     *
     * @param array The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public void putArray(int index, float[] array, int offset, int length, int flags) {
        putObject(array, offset, length, makeObjectFlags(flags, FLOAT | ARRAY, index));
    }

    /**
     * Adds a java double array as a pointer parameter.
     *
     * @param array The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public void putArray(int index, double[] array, int offset, int length, int flags) {
        putObject(array, offset, length, makeObjectFlags(flags, DOUBLE | ARRAY, index));
    }
    
    /**
     * Adds a java boolean array as a pointer parameter.
     *
     * @param array The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public void putArray(int index, boolean[] array, int offset, int length, int flags) {
        putObject(array, offset, length, makeObjectFlags(flags, BOOLEAN | ARRAY, index));
    }
    
    /**
     * Adds a java char array as a pointer parameter.
     *
     * @param array The java array to use as the pointer parameter.
     * @param offset The offset from the start of the array.
     * @param length The length of the array to use.
     * @param flags The flags to use (IN, OUT)
     */
    public void putArray(int index, char[] array, int offset, int length, int flags) {
        putObject(array, offset, length, makeObjectFlags(flags, CHAR | ARRAY, index));
    }

    /**
     * Adds a java direct buffer as a pointer parameter.
     * @param buffer The buffer to use as a pointer argument.
     * @param offset An offset to add to the buffer native address.
     * @param length The length of the buffer to use.
     */
    public void putDirectBuffer(int index, java.nio.Buffer obj, int offset, int length) {
        putObject(obj, offset, length, makeBufferFlags(index));
    }

    /**
     * Put the address of the current JNIEnv into this parameter position
     *
     * @param index The index of the parameter.
     */
    public void putJNI(int index, Object obj, int type) {
        putObject(obj, 0, 0, makeJNIFlags(index, type));
    }

    void putObject(Object array, int offset, int length, int flags) {
        ensureSpace();
        objects[objectIndex++] = array;
        info[infoIndex++] = flags;
        info[infoIndex++] = offset;
        info[infoIndex++] = length;
    }
}
