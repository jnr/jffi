package com.kenai.jffi.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Cleaner {

    private Cleaner() {
        throw new UnsupportedOperationException();
    }

    private static final Method createMethod;
    private static final Method cleanMethod;
    private static final Method registerMethod;
    static {
        Method method;
        try {
            method = Class.forName("java.lang.ref.Cleaner").getDeclaredMethod("register", Object.class, Runnable.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            method = null;
        }
        registerMethod = method;
        if (registerMethod == null) {
            try {
                method = Class.forName("sun.misc.Cleaner").getDeclaredMethod("create", Object.class, Runnable.class);
            } catch (NoSuchMethodException | ClassNotFoundException ignored) {
            }
            createMethod = method;
            try {
                method = Class.forName("java.lang.ref.Cleaner.Cleanable").getDeclaredMethod("clean");
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            }
            cleanMethod = method;
        }
        else {
            try {
                method = Class.forName("java.lang.ref.Cleaner").getDeclaredMethod("create");
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                method = null;
            }
            createMethod = method;
            try {
                method = Class.forName("sun.misc.Cleaner").getDeclaredMethod("clean");
            } catch (NoSuchMethodException | ClassNotFoundException ex) {
                method = null;
            }
            cleanMethod = method;
        }
    }

    private static final Object cleanerLock = registerMethod == null ? null : new Object();
    private static volatile Object cleaner;

    private static Object getCleaner(Object object, Runnable cleanup) {
        if (registerMethod == null) { // JDK 1.8
            try {
                return createMethod.invoke(null, object, cleanup);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
        else { // JDK 9
            if (cleaner == null) synchronized (cleanerLock) {
                if (cleaner == null) {
                    try {
                        cleaner = createMethod.invoke(null);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            try {
                return registerMethod.invoke(cleaner, object, cleanup);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static Runnable register(Object object, Runnable cleanup) {
        Object cleaner = getCleaner(object, cleanup);
        return new Runnable() {
            @Override
            public void run() {
                try {
                    cleanMethod.invoke(cleaner);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

}
