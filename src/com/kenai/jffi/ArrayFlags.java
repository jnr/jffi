
package com.kenai.jffi;

public final class ArrayFlags {
    public static final int IN = ObjectBuffer.IN;
    public static final int OUT = ObjectBuffer.OUT;
    public static final int PINNED = ObjectBuffer.PINNED;
    public static final int NULTERMINATE = ObjectBuffer.ZERO_TERMINATE;
    public static final boolean isOut(int flags) {
        return (flags & (OUT | IN)) != IN;
    }
    public static final boolean isIn(int flags) {
        return (flags & (OUT | IN)) != OUT;
    }
}
