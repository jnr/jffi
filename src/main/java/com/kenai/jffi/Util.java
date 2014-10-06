package com.kenai.jffi;

import java.util.Locale;

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

    public static boolean startsWithIgnoreCase(String s1, String s2, Locale locale) {
        return s1.startsWith(s2)
            || s1.toUpperCase(locale).startsWith(s2.toUpperCase(locale))
            || s1.toLowerCase(locale).startsWith(s2.toLowerCase(locale));
    }

    public static boolean equalsIgnoreCase(String s1, String s2, Locale locale) {
        return s1.equalsIgnoreCase(s2)
            || s1.toUpperCase(locale).equals(s2.toUpperCase(locale))
            || s1.toLowerCase(locale).equals(s2.toLowerCase(locale));
    }
}
