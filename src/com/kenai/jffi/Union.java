
package com.kenai.jffi;

/**
 * Describes the layout of a C union
 */
public final class Union extends Aggregate {
    /* Keep a strong reference to the field types so they do not GCed */
    private final Type[] fields;

    /**
     * Creates a new C union layout description.
     *
     * @param fields The fields contained in the struct.
     */
    public Union(Type[] fields) {
        super(Foreign.getInstance().newStruct(Type.nativeHandles(fields), true));
        this.fields = (Type[]) fields.clone();
    }
}
