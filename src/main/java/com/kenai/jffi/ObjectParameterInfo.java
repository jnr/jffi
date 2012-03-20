package com.kenai.jffi;

public final class ObjectParameterInfo {
    private final int parameterIndex;
    private final int objectInfo;

    
    public static ObjectParameterInfo create(int parameterIndex, ObjectType objectType, 
            ComponentType componentType, int ioflags) {

        return new ObjectParameterInfo(parameterIndex, ioflags, objectType.value | componentType.value);
    }

    public static ObjectParameterInfo create(int parameterIndex, int ioflags) {

        return new ObjectParameterInfo(parameterIndex, ioflags, 0);
    }

    private ObjectParameterInfo(int parameterIndex, int ioflags, int typeInfo) {

        this.parameterIndex = parameterIndex;
        this.objectInfo = ObjectBuffer.makeObjectFlags(ioflags, typeInfo, parameterIndex);
    }

    /** Copy the array contents to native memory before calling the function */
    public static final int IN = ObjectBuffer.IN;

    /** After calling the function, reload the array contents from native memory */
    public static final int OUT = ObjectBuffer.OUT;

    /** Pin the array memory and pass the JVM memory pointer directly to the function */
    public static final int PINNED = ObjectBuffer.PINNED;

    /** Append a NUL byte to the array contents after copying to native memory */
    public static final int NULTERMINATE = ObjectBuffer.ZERO_TERMINATE;

    /** For OUT arrays, clear the native memory area before passing to the native function */
    public static final int CLEAR = ObjectBuffer.CLEAR;

    public static final ObjectType ARRAY = ObjectType.ARRAY;
    public static final ObjectType BUFFER = ObjectType.BUFFER;


    public static enum ObjectType {
        ARRAY(ObjectBuffer.ARRAY),
        BUFFER(ObjectBuffer.BUFFER);

        final int value;

        ObjectType(int type) {
            this.value = type;
        }
    }

    public static enum ComponentType {
        BYTE(ObjectBuffer.BYTE),
        SHORT(ObjectBuffer.SHORT),
        INT(ObjectBuffer.INT),
        LONG(ObjectBuffer.LONG),
        FLOAT(ObjectBuffer.FLOAT),
        DOUBLE(ObjectBuffer.DOUBLE),
        BOOLEAN(ObjectBuffer.BOOLEAN),
        CHAR(ObjectBuffer.CHAR);

        final int value;

        ComponentType(int type) {
            this.value = type;
        }
    }

    public static final ComponentType BYTE = ComponentType.BYTE;
    public static final ComponentType SHORT = ComponentType.SHORT;
    public static final ComponentType INT = ComponentType.INT;
    public static final ComponentType LONG = ComponentType.LONG;
    public static final ComponentType FLOAT = ComponentType.FLOAT;
    public static final ComponentType DOUBLE = ComponentType.DOUBLE;
    public static final ComponentType BOOLEAN = ComponentType.BOOLEAN;
    public static final ComponentType CHAR = ComponentType.CHAR;

    final int asObjectInfo() {
        return objectInfo;
    }

    public final int getParameterIndex() {
        return parameterIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectParameterInfo info = (ObjectParameterInfo) o;

        return objectInfo == info.objectInfo && parameterIndex == info.parameterIndex;
    }

    @Override
    public int hashCode() {
        return 31 * parameterIndex + objectInfo;
    }
}
