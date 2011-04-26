package com.kenai.jffi;

/**
 *
 */
public class HeapObjectParameterInvoker extends ObjectParameterInvoker {
    private final Foreign foreign = Foreign.getInstance();

    private static int encode(byte[] paramBuffer, int off, Type type, long n) {
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        if (type.size() <= 4) {
            return encoder.putInt(paramBuffer, off, (int) n);
        } else {
            return encoder.putLong(paramBuffer, off, n);
        }
    }
    
    public long invokeN1O1rN(Function function, 
            long n1,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        
        return foreign.invokeArrayO1Int64(function.getContextAddress(), 
                new byte[HeapInvocationBuffer.encoder.getBufferSize(function)],
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }
    
    public long invokeN2O1rN(Function function, 
            long n1, long n2,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {

        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        byte[] paramBuffer = new byte[encoder.getBufferSize(function)];
        
        int poff = 0;
        poff = encode(paramBuffer, poff, function.getParameterType(0), n1);
        poff = encode(paramBuffer, poff, function.getParameterType(1), n2);
        
        return foreign.invokeArrayO1Int64(function.getContextAddress(), 
                paramBuffer, 
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }

    public long invokeN2O2rN(Function function,
            long n1, long n2,
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, 
            Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        
        return foreign.invokeArrayO2Int64(function.getContextAddress(), 
                new byte[HeapInvocationBuffer.encoder.getBufferSize(function)],
                o1, o1flags.asObjectInfo(), o1off, o1len, 
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }

    public long invokeN3O1rN(Function function, 
        long n1, long n2, long n3,
        Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        byte[] paramBuffer = new byte[encoder.getBufferSize(function)];
        
        int poff = 0;
        poff = encode(paramBuffer, poff, function.getParameterType(0), n1);
        poff = encode(paramBuffer, poff, function.getParameterType(1), n2);
        poff = encode(paramBuffer, poff, function.getParameterType(2), n3);
        
        
        return foreign.invokeArrayO1Int64(function.getContextAddress(), 
                paramBuffer, 
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }
    
    public long invokeN3O2rN(Function function, 
        long n1, long n2, long n3, 
        Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, 
        Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        byte[] paramBuffer = new byte[encoder.getBufferSize(function)];
        
        int poff = 0;
        poff = encode(paramBuffer, poff, function.getParameterType(0), n1);
        poff = encode(paramBuffer, poff, function.getParameterType(1), n2);
        poff = encode(paramBuffer, poff, function.getParameterType(2), n3);
        
        return foreign.invokeArrayO2Int64(function.getContextAddress(), 
                paramBuffer, 
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }

    public long invokeN4O1rN(Function function, 
        long n1, long n2, long n3, long n4,
        Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        byte[] paramBuffer = new byte[encoder.getBufferSize(function)];
        
        int poff = 0;
        poff = encode(paramBuffer, poff, function.getParameterType(0), n1);
        poff = encode(paramBuffer, poff, function.getParameterType(1), n2);
        poff = encode(paramBuffer, poff, function.getParameterType(2), n3);
        poff = encode(paramBuffer, poff, function.getParameterType(3), n4);
        
        
        return foreign.invokeArrayO1Int64(function.getContextAddress(), 
                paramBuffer, 
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }
    
    public long invokeN4O2rN(Function function, 
        long n1, long n2, long n3, long n4,
        Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
        Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        byte[] paramBuffer = new byte[encoder.getBufferSize(function)];
        
        int poff = 0;
        poff = encode(paramBuffer, poff, function.getParameterType(0), n1);
        poff = encode(paramBuffer, poff, function.getParameterType(1), n2);
        poff = encode(paramBuffer, poff, function.getParameterType(2), n3);
        poff = encode(paramBuffer, poff, function.getParameterType(3), n4);
        
        
        return foreign.invokeArrayO2Int64(function.getContextAddress(), 
                paramBuffer, 
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }

    @Override
    public long invokeN5O1rN(Function function, 
            long n1, long n2, long n3, long n4, long n5, 
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        byte[] paramBuffer = new byte[encoder.getBufferSize(function)];
        
        int poff = 0;
        poff = encode(paramBuffer, poff, function.getParameterType(0), n1);
        poff = encode(paramBuffer, poff, function.getParameterType(1), n2);
        poff = encode(paramBuffer, poff, function.getParameterType(2), n3);
        poff = encode(paramBuffer, poff, function.getParameterType(3), n4);
        poff = encode(paramBuffer, poff, function.getParameterType(4), n5);
        
        return foreign.invokeArrayO1Int64(function.getContextAddress(), 
                paramBuffer, 
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }

    @Override
    public long invokeN5O2rN(Function function, 
            long n1, long n2, long n3, long n4, long n5, 
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, 
            Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        byte[] paramBuffer = new byte[encoder.getBufferSize(function)];
        
        int poff = 0;
        poff = encode(paramBuffer, poff, function.getParameterType(0), n1);
        poff = encode(paramBuffer, poff, function.getParameterType(1), n2);
        poff = encode(paramBuffer, poff, function.getParameterType(2), n3);
        poff = encode(paramBuffer, poff, function.getParameterType(3), n4);
        poff = encode(paramBuffer, poff, function.getParameterType(4), n5);
        
        
        return foreign.invokeArrayO2Int64(function.getContextAddress(), 
                paramBuffer, 
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }

    @Override
    public long invokeN6O1rN(Function function, 
            long n1, long n2, long n3, long n4, long n5, long n6, 
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        byte[] paramBuffer = new byte[encoder.getBufferSize(function)];
        
        int poff = 0;
        poff = encode(paramBuffer, poff, function.getParameterType(0), n1);
        poff = encode(paramBuffer, poff, function.getParameterType(1), n2);
        poff = encode(paramBuffer, poff, function.getParameterType(2), n3);
        poff = encode(paramBuffer, poff, function.getParameterType(3), n4);
        poff = encode(paramBuffer, poff, function.getParameterType(4), n5);
        poff = encode(paramBuffer, poff, function.getParameterType(5), n6);
        
        return foreign.invokeArrayO1Int64(function.getContextAddress(), 
                paramBuffer, 
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }

    @Override
    public long invokeN6O2rN(Function function, 
            long n1, long n2, long n3, long n4, long n5, long n6, 
            Object o1, int o1off, int o1len, ObjectParameterInfo o1flags, 
            Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        
        HeapInvocationBuffer.Encoder encoder = HeapInvocationBuffer.encoder;
        byte[] paramBuffer = new byte[encoder.getBufferSize(function)];
        
        int poff = 0;
        poff = encode(paramBuffer, poff, function.getParameterType(0), n1);
        poff = encode(paramBuffer, poff, function.getParameterType(1), n2);
        poff = encode(paramBuffer, poff, function.getParameterType(2), n3);
        poff = encode(paramBuffer, poff, function.getParameterType(3), n4);
        poff = encode(paramBuffer, poff, function.getParameterType(4), n5);
        poff = encode(paramBuffer, poff, function.getParameterType(5), n6);
        
        return foreign.invokeArrayO2Int64(function.getContextAddress(), 
                paramBuffer, 
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }
    
    

}
