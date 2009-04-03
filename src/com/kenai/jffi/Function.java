
package com.kenai.jffi;

public final class Function {
    private final int address32;
    private final long address;
    private final int parameterCount;
    private final int rawParameterSize;
    final Type returnType;
    final Type[] paramTypes;
    
    public Function(long address, Type returnType, Type[] paramTypes, CallingConvention convention) {

        final long h = Foreign.getInstance().newFunction(address,
                returnType.handle(), Type.nativeHandles(paramTypes),
                convention == CallingConvention.STDCALL ? 1 : 0);
        if (h == 0) {
            throw new RuntimeException("Failed to create native function");
        }

        //
        // Keep references to the return and parameter types so they do not get
        // garbage collected
        //
        this.returnType = returnType;
        this.paramTypes = (Type[]) paramTypes.clone();

        this.parameterCount = paramTypes.length;
        this.rawParameterSize = Foreign.getInstance().getFunctionRawParameterSize(h);
        this.address = h;
        this.address32 = (int) h;
    }
    public Function(long address, Type returnType, Type[] paramTypes) {
        this(address, returnType, paramTypes, CallingConvention.DEFAULT);
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
    final Type getReturnType() {
        return returnType;
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
