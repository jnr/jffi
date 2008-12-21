
package com.kenai.jffi;

public final class Function {
    private final int address32;
    private final long address64;

    public Function(Address address, Type returnType, Type[] paramTypes) {
        int[] nativeParamTypes = new int[paramTypes.length];
        for (int i = 0; i < paramTypes.length; ++i) {
            nativeParamTypes[i] = paramTypes[i].value();
        }
        final long h = Foreign.getForeign().newFunction(address.nativeAddress(),
                returnType.value(), nativeParamTypes, 0);
        if (h == 0) {
            throw new RuntimeException("Failed to create native function");
        }
        address64 = h;
        address32 = (int) h;
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
                Foreign.getForeign().freeFunction(address64);
            }
        } finally {
            super.finalize();
        }
    }
}
