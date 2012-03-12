package com.kenai.jffi;

abstract public class ObjectParameterInvoker {
    private static final class SingletonHolder {
        static final ObjectParameterInvoker INSTANCE 
                = Foreign.getInstance().getVersion() >= 0x01000A /* version 1.0.10 */
                ? newNativeInvoker() : newHeapInvoker();
    }
    
    public static ObjectParameterInvoker getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    static ObjectParameterInvoker newNativeInvoker() {
        return new NativeObjectParameterInvoker(Foreign.getInstance());
    }
    
    static ObjectParameterInvoker newHeapInvoker() {
        return new HeapObjectParameterInvoker(Foreign.getInstance());
    }
    
    abstract public boolean isNative();

    /**
     * Invokes a function with one numeric argument (which may be a pointer), and returns a numeric value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param n1 numeric argument.
     * @param o1 array or buffer, to be passed as a pointer for the first numeric parameter.
     * @param o1off offset from the start of the array or buffer.
     * @param o1len length of the array to use.
     * @param o1flags object flags (type, direction, parameter index).
     */
    abstract public long invokeN1O1rN(Function function,
                                      long n1,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);

    /**
     * Invokes a function with two numeric arguments, and returns a numeric value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param n1 first numeric argument.
     * @param n2 The second numeric argument.
     * @param o1 An Object (array or buffer), to be passed as a pointer.
     * @param o1off offset from the start of the array or buffer.
     * @param o1len length of the array to use.
     * @param o1flags object flags (type, direction, parameter index).
     */
    abstract public long invokeN2O1rN(Function function,
                                      long n1, long n2,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);

    /**
     * Invokes a function with two numeric arguments, and returns a numeric value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param n1 first numeric argument.
     * @param n2 The second numeric argument.
     * @param o1 An Object (array or buffer), to be passed as a pointer.
     * @param o1off offset from the start of the array or buffer.
     * @param o1len length of the array to use.
     * @param o1flags object flags (type, direction, parameter index).
     * @param o2 An Object (array or buffer), to be passed as a pointer.
     * @param o2off The offset from the start of the array or buffer.
     * @param o2len The length of the array to use.
     * @param o2flags Object flags (direction, type, idx).
     */
    abstract public long invokeN2O2rN(Function function,
                                      long n1, long n2,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                      Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);

    /**
     * Invokes a function with two numeric arguments, and returns a numeric value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param n1 first numeric argument.
     * @param n2 The second numeric argument.
     * @param n3 The second numeric argument.
     * @param o1 An Object (array or buffer), to be passed as a pointer.
     * @param o1off offset from the start of the array or buffer.
     * @param o1len length of the array to use.
     * @param o1flags object flags (type, direction, parameter index).
     */
    abstract public long invokeN3O1rN(Function function,
                                      long n1, long n2, long n3,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN3O2rN(Function function,
                                      long n1, long n2, long n3,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                      Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);
    
    abstract public long invokeN3O3rN(Function function,
                                      long n1, long n2, long n3,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                      Object o2, int o2off, int o2len, ObjectParameterInfo o2flags,
                                      Object o3, int o3off, int o3len, ObjectParameterInfo o3flags);
    
    abstract public long invokeN4O1rN(Function function,
                                      long n1, long n2, long n3, long n4,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN4O2rN(Function function,
                                      long n1, long n2, long n3, long n4,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                      Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);
    
    abstract public long invokeN4O3rN(Function function,
                                      long n1, long n2, long n3, long n4,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                      Object o2, int o2off, int o2len, ObjectParameterInfo o2flags,
                                      Object o3, int o3off, int o3len, ObjectParameterInfo o3flags);
    
    abstract public long invokeN5O1rN(Function function,
                                      long n1, long n2, long n3, long n4, long n5,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN5O2rN(Function function,
                                      long n1, long n2, long n3, long n4, long n5,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                      Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);
    
    abstract public long invokeN5O3rN(Function function,
                                      long n1, long n2, long n3, long n4, long n5,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                      Object o2, int o2off, int o2len, ObjectParameterInfo o2flags,
                                      Object o3, int o3off, int o3len, ObjectParameterInfo o3flags);
    
    abstract public long invokeN6O1rN(Function function,
                                      long n1, long n2, long n3, long n4, long n5, long n6,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN6O2rN(Function function,
                                      long n1, long n2, long n3, long n4, long n5, long n6,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                      Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);
    
    abstract public long invokeN6O3rN(Function function,
                                      long n1, long n2, long n3, long n4, long n5, long n6,
                                      Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                      Object o2, int o2off, int o2len, ObjectParameterInfo o2flags,
                                      Object o3, int o3off, int o3len, ObjectParameterInfo o3flags);
}
