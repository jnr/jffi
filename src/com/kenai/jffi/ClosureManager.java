
package com.kenai.jffi;

public class ClosureManager {
    private static final class SingletonHolder {
        static final ClosureManager INSTANCE = new ClosureManager();
    }
    public static final ClosureManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private ClosureManager() { }
}
