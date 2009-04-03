
package com.kenai.jffi;

/**
 * Provides access to the value of errno on unix, or GetLastError on windows.
 */
public final class LastError {

    /** Lazy-initialization singleton holder */
    private static final class SingletonHolder {
        static final LastError INSTANCE = new LastError();
    }

    /** Creates a new <tt>LastError</tt> instance */
    private LastError() {}

    /**
     * Gets the singleton instance of the <tt>LastError</tt> object.
     *
     * @return An instance of <tt>LastError</tt>
     */
    public static final LastError getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Gets the errno set by the last C function invoked by the current thread.
     *
     * @return The value of errno/GetLastError()
     */
    public final int getError() {
        return Foreign.getInstance().getLastError();
    }
}
