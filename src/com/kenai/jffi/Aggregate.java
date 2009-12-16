
package com.kenai.jffi;

public abstract class Aggregate extends Type {
    private volatile boolean disposed;

    public Aggregate(long handle) {
        super(handle);
    }

    public synchronized final void dispose() {
        if (disposed) {
            throw new RuntimeException("native handle already freed");
        }

        disposed = true;
        Foreign.getInstance().freeStruct(handle);        
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!disposed) {
                Foreign.getInstance().freeStruct(handle);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            super.finalize();
        }
    }
}
