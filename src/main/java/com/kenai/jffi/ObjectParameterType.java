package com.kenai.jffi;

import java.util.EnumSet;

/**
 * Describes the type of an object parameter (e.g. byte array, byte buffer)
 */
public final class ObjectParameterType {
    final int typeInfo;

    public static ObjectParameterType create(ObjectType objectType,
                                             ComponentType componentType) {

        if (objectType == ObjectType.ARRAY) {
            return TypeCache.arrayTypeCache[componentType.ordinal()];

        } else if (objectType == ObjectType.BUFFER) {
            return TypeCache.bufferTypeCache[componentType.ordinal()];
        }

        return new ObjectParameterType(objectType.value | componentType.value);
    }

    ObjectParameterType(int typeInfo) {
        this.typeInfo = typeInfo;
    }

    ObjectParameterType(ObjectType objectType, ComponentType componentType) {
        this.typeInfo = objectType.value | componentType.value;
    }

    private static final class TypeCache {
        static final ObjectParameterType[] arrayTypeCache;
        static final ObjectParameterType[] bufferTypeCache;
        static {
            EnumSet<ComponentType> componentTypes = EnumSet.allOf(ComponentType.class);
            arrayTypeCache = new ObjectParameterType[componentTypes.size()];
            bufferTypeCache = new ObjectParameterType[componentTypes.size()];
            for (ComponentType componentType : componentTypes) {
                arrayTypeCache[componentType.ordinal()] = new ObjectParameterType(ARRAY, componentType);
                bufferTypeCache[componentType.ordinal()] = new ObjectParameterType(BUFFER, componentType);
            }
        }
    }

    static final ObjectParameterType INVALID = new ObjectParameterType(0);
    static final ObjectParameterType NONE = new ObjectParameterType(0);

    public static enum ObjectType {
        ARRAY(ObjectBuffer.ARRAY),
        BUFFER(ObjectBuffer.BUFFER);

        final int value;

        ObjectType(int type) {
            this.value = type;
        }
    }

    public static final ObjectType ARRAY = ObjectType.ARRAY;
    public static final ObjectType BUFFER = ObjectType.BUFFER;

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

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ObjectParameterType && typeInfo == ((ObjectParameterType) o).typeInfo);
    }

    @Override
    public int hashCode() {
        return typeInfo;
    }
}
