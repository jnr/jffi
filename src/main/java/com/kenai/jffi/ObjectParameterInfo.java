package com.kenai.jffi;

public final class ObjectParameterInfo {
    private final int parameterIndex;
    private final int objectInfo;

    
    public static ObjectParameterInfo create(int parameterIndex, ObjectType objectType, 
            ComponentType componentType, int ioflags) {

        return new ObjectParameterInfo(parameterIndex, objectType, componentType, ioflags);
    }

    /* Stop ArrayFlags from being intantiated */
    private ObjectParameterInfo(int parameterIndex, ObjectType objectType, 
            ComponentType componentType, int ioflags) {
        
        this.parameterIndex = parameterIndex;
        this.objectInfo = ObjectBuffer.makeObjectFlags(ioflags,
                objectType.value | componentType.value, parameterIndex);
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

    public static final class ObjectType {
        final int value;

        ObjectType(int type) {
            this.value = type;
        }
    }
    
    public static final ObjectType ARRAY = new ObjectType(ObjectBuffer.ARRAY);
    public static final ObjectType BUFFER = new ObjectType(ObjectBuffer.BUFFER);
    
    public static final class ComponentType {
        final int value;

        ComponentType(int type) {
            this.value = type;
        }
    }
    
    public static final ComponentType BYTE = new ComponentType(ObjectBuffer.BYTE);
    public static final ComponentType SHORT = new ComponentType(ObjectBuffer.SHORT);
    public static final ComponentType INT = new ComponentType(ObjectBuffer.INT);
    public static final ComponentType LONG = new ComponentType(ObjectBuffer.LONG);
    public static final ComponentType FLOAT = new ComponentType(ObjectBuffer.FLOAT);
    public static final ComponentType DOUBLE = new ComponentType(ObjectBuffer.DOUBLE);
    public static final ComponentType BOOLEAN = new ComponentType(ObjectBuffer.BOOLEAN);
    public static final ComponentType CHAR = new ComponentType(ObjectBuffer.CHAR);
    
    final int asObjectInfo() {
        return objectInfo;
    }

    public final int getParameterIndex() {
        return parameterIndex;
    }
}
