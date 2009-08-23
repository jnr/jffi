
package com.kenai.jffi;

public abstract class Aggregate extends Type {

    public Aggregate(long handle) {
        super(handle);
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
