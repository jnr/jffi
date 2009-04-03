
package com.kenai.jffi;

/**
 * Describes the layout of a C struct
 */
public final class Struct extends Type {
    /* Keep a strong reference to the field types so they do not GCed */
    private final Type[] fields;

    /**
     * Creates a new C struct layout description.
     *
     * @param fields The fields contained in the struct.
     */
    public Struct(Type[] fields) {
        super(Type.STRUCT.type(), Foreign.getInstance().newStruct(Type.nativeHandles(fields)));
        this.fields = (Type[]) fields.clone();
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
