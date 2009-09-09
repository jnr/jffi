
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
        Foreign.getInstance().freeStruct(handle);
        disposed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            Foreign.getInstance().freeStruct(handle);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            super.finalize();
        }
    }
}
