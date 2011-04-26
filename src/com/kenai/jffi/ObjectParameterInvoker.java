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
        return new NativeObjectParameterInvoker();
    }
    
    static ObjectParameterInvoker newHeapInvoker() {
        return new HeapObjectParameterInvoker();
    }
    
    abstract public long invokeN1O1rN(Function function, 
            long n1, 
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN2O1rN(Function function, 
            long n1, long n2,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN2O2rN(Function function,
            long n1, long n2,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
            Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);
    
    abstract public long invokeN3O1rN(Function function, 
            long n1, long n2, long n3,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN3O2rN(Function function, 
            long n1, long n2, long n3,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
            Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);
    
    abstract public long invokeN4O1rN(Function function, 
            long n1, long n2, long n3, long n4,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN4O2rN(Function function, 
            long n1, long n2, long n3, long n4,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
            Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);
    
    abstract public long invokeN5O1rN(Function function, 
            long n1, long n2, long n3, long n4, long n5,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN5O2rN(Function function, 
            long n1, long n2, long n3, long n4, long n5,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
            Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);
    
    abstract public long invokeN6O1rN(Function function, 
            long n1, long n2, long n3, long n4, long n5, long n6,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags);
    
    abstract public long invokeN6O2rN(Function function, 
            long n1, long n2, long n3, long n4, long n5, long n6,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
            Object o2, int o2off, int o2len, ObjectParameterInfo o2flags);
}
