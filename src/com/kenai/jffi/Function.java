/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jffi;

/**
 *
 * @author wayne
 */
public final class Function {
    private final int address32;
    private final Address handle;
    public Function(Address address, NativeType returnType, NativeType[] paramTypes) {
        int[] nativeParamTypes = new int[paramTypes.length];
        for (int i = 0; i < paramTypes.length; ++i) {
            nativeParamTypes[i] = paramTypes[i].value();
        }
        long h = Foreign.getForeign().newFunction(address.nativeAddress(), returnType.value(), nativeParamTypes, 0);
        handle = new Address(h);
        address32 = (int) handle.nativeAddress();
    }

    final long getAddress64() {
        return handle.nativeAddress();
    }
    final int getAddress32() {
        return address32;
    }
}
