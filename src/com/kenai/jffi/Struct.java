
package com.kenai.jffi;

/**
 * A wrapper around FFI struct types
 */
public final class Struct extends Type {
    private final Type[] fields;
    
    public Struct(Type[] fields) {
        super(Type.STRUCT.type(), newNativeStruct(fields));
        this.fields = (Type[]) fields.clone();
    }

    private static final long newNativeStruct(Type[] types) {
        long[] ffiTypes = new long[types.length];
        for (int i = 0; i < types.length; ++i) {
            ffiTypes[i] = types[i].handle();
        }
        return Foreign.getInstance().newStruct(ffiTypes);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            Foreign.getInstance().freeStruct(handle);
        } finally {
            super.finalize();
        }
    }
}
