
package com.kenai.jffi;

public final class Function {
    private final int address32;
    private final long address;
    private final int parameterCount;
    private final int rawParameterSize;
    
    public Function(long address, Type returnType, Type[] paramTypes, CallingConvention convention) {
        int[] nativeParamTypes = new int[paramTypes.length];
        for (int i = 0; i < paramTypes.length; ++i) {
            nativeParamTypes[i] = paramTypes[i].value();
        }
        final long h = Foreign.getInstance().newFunction(address,
                returnType.value(), nativeParamTypes,
                convention == CallingConvention.STDCALL ? 1 : 0);
        if (h == 0) {
            throw new RuntimeException("Failed to create native function");
        }
        this.parameterCount = nativeParamTypes.length;
        this.rawParameterSize = Foreign.getInstance().getFunctionRawParameterSize(h);
        this.address = h;
        this.address32 = (int) h;
    }
    public Function(long address, Type returnType, Type[] paramTypes) {
        this(address, returnType, paramTypes, CallingConvention.DEFAULT);
    }
    @Deprecated
    public Function(Address address, Type returnType, Type[] paramTypes, CallingConvention convention) {
        this(address.nativeAddress(), returnType, paramTypes, convention);
    }
    @Deprecated
    public Function(Address address, Type returnType, Type[] paramTypes) {
        this(address.nativeAddress(), returnType, paramTypes, CallingConvention.DEFAULT);
    }
    final int getParameterCount() {
        return parameterCount;
    }
    final int getRawParameterSize() {
        return rawParameterSize;
    }
    final long getAddress64() {
        return address;
    }
    final int getAddress32() {
        return address32;
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            if (address != 0) {
                Foreign.getInstance().freeFunction(address);
            }
        } finally {
            super.finalize();
        }
    }
}
