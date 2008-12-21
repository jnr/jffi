
package com.kenai.jffi;

public final class Function {
    private final int address32;
    private final Address handle;

    public Function(Address address, NativeType returnType, NativeType[] paramTypes) {
        int[] nativeParamTypes = new int[paramTypes.length];
        for (int i = 0; i < paramTypes.length; ++i) {
            nativeParamTypes[i] = paramTypes[i].value();
        }
        final long h = Foreign.getForeign().newFunction(address.nativeAddress(),
                returnType.value(), nativeParamTypes, 0);
        if (h == 0) {
            throw new RuntimeException("Failed to create native function");
        }
        handle = new Address(h);
        address32 = (int) h;
    }
    
    final long getAddress64() {
        return handle.nativeAddress();
    }
    final int getAddress32() {
        return address32;
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            if (handle != null && !handle.isNull()) {
                Foreign.getForeign().freeFunction(handle.nativeAddress());
            }
        } finally {
            super.finalize();
        }
    }
}
