package com.kenai.jffi;

public final class Util {
    private Util() {}

    /**
     * Aligns an address to a boundary
     *
     * @param v The address to roundup
     * @param a The boundary to align to.
     * @return The aligned address.
     */
    static int ffi_align(int v, int a) {
        return ((v - 1) | (a - 1)) + 1;
    }
}
