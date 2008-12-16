/*
 * Copyright (C) 2007, 2008 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Interface to  the foreign function interface.
 */
package com.kenai.jffi;

public final class Foreign {
    private static final class SingletonHolder {
        static {
            Init.init();
        }
        private static final Foreign INSTANCE = new Foreign();
    }
    public static final Foreign getForeign() {
        return SingletonHolder.INSTANCE;
    }
    private Foreign() {}
    public native long dlopen(String name, int flags);
    public native void dlclose(long handle);
    public native long dlsym(long handle, String name);

    public native long allocateMemory(long size, boolean clear);
    public native void freeMemory(long address);
    
    public native long newCallContext(int returnType, int[] paramTypes, int convention);
    public native void freeCallContext(long handle);

    public final int callVrI(CallContext ctx, Address function) {
        return callVrI(ctx.getAddress(), function.nativeAddress());
    }
    public final int callIrI(CallContext ctx, Address function, int a1) {
        return callIrI(ctx.getAddress(), function.nativeAddress(), a1);
    }
    public final native int callVrI(long ctx, long function);
    public final native int callIrI(long ctx, long function, int a1);
    public final native int callIIrI(long ctx, long function, int a1, int a2);
    public final native int callIIIrI(long ctx, long function, int a1, int a2, int a3);
}
