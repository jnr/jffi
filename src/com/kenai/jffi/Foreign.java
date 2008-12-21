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

    public native long newFunction(long address, int returnType, int[] paramTypes, int convention);
    public native void freeFunction(long handle);

//    public long newClosure(Closure closure, int returnType, int[] paramTypes, int convention);
//    public native void freeClosure(long handle);

    
    final native int invoke32VrI(int function);
    final native int invoke64VrI(long function);
    final native int invoke32IrI(int function, int arg1);
    final native int invoke64IrI(long function, int arg1);
    final native int invoke32IIrI(int function, int arg1, int arg2);
    final native int invoke64IIrI(long function, int arg1, int arg2);
    final native int invoke32IIIrI(int function, int arg1, int arg2, int arg3);
    final native int invoke64IIIrI(long function, int arg1, int arg2, int arg3);
    final native int invokeArrayInt32(long function, byte[] buffer);
    final native long invokeArrayInt64(long function, byte[] buffer);
    final native float invokeArrayFloat(long function, byte[] buffer);
    final native double invokeArrayDouble(long function, byte[] buffer);
}
