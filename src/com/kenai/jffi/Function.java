
package com.kenai.jffi;

public final class Function {
    private final int address32;
    private final long address64;
    private final int parameterCount;
    private final int rawParameterSize;
    
    public Function(Address address, Type returnType, Type[] paramTypes, CallingConvention convention) {
        int[] nativeParamTypes = new int[paramTypes.length];
        for (int i = 0; i < paramTypes.length; ++i) {
            nativeParamTypes[i] = paramTypes[i].value();
        }
        final long h = Foreign.getInstance().newFunction(address.nativeAddress(),
                returnType.value(), nativeParamTypes,
                convention == CallingConvention.STDCALL ? 1 : 0);
        if (h == 0) {
            throw new RuntimeException("Failed to create native function");
        }
        parameterCount = nativeParamTypes.length;
        rawParameterSize = Foreign.getInstance().getFunctionRawParameterSize(h);
        address64 = h;
        address32 = (int) h;
    }
    public Function(Address address, Type returnType, Type[] paramTypes) {
        this(address, returnType, paramTypes, CallingConvention.DEFAULT);
    }
    final int getParameterCount() {
        return parameterCount;
    }
    final int getRawParameterSize() {
        return rawParameterSize;
    }
    final long getAddress64() {
        return address64;
    }
    final int getAddress32() {
        return address32;
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            if (address64 != 0) {
                Foreign.getInstance().freeFunction(address64);
            }
        } finally {
            super.finalize();
        }
    }
}
