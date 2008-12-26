
package com.kenai.jffi;

public final class LastError {
    private final Foreign foreign = Foreign.getInstance();
    private static final class SingletonHolder {
        static final LastError INSTANCE = new LastError();
    }
    private LastError() {}
    public static final LastError getInstance() {
        return SingletonHolder.INSTANCE;
    }
    public final int getError() {
        return foreign.getLastError();
    }
}
