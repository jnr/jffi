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

public abstract class Foreign {
    private static final class SingletonHolder {
        static {
            Init.init();
        }
        private static final Foreign INSTANCE = Platform.is64()
                ? LP64.INSTANCE : ILP32.INSTANCE;
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

    public abstract int invokeVrI(Function function);
    public abstract int invokeIrI(Function function, int arg1);
    public abstract int invokeIIrI(Function function, int arg1, int arg2);
    public abstract int invokeIIIrI(Function function, int arg1, int arg2, int arg3);

    private static final native int invoke32VrI(int function);
    private static final native int invoke64VrI(long function);
    private static final native int invoke32IrI(int function, int arg1);
    private static final native int invoke64IrI(long function, int arg1);
    private static final native int invoke32IIrI(int function, int arg1, int arg2);
    private static final native int invoke64IIrI(long function, int arg1, int arg2);
    private static final native int invoke32IIIrI(int function, int arg1, int arg2, int arg3);
    private static final native int invoke64IIIrI(long function, int arg1, int arg2, int arg3);

    public int invokeInt(Function function, HeapInvocationBuffer buffer) {
        return invokeArrayInt32(function.getAddress64(), buffer.array());
    }
    private static native int invokeArrayInt32(long function, byte[] buffer);

    public long invokeLong(Function function, HeapInvocationBuffer buffer) {
        return invokeArrayInt64(function.getAddress64(), buffer.array());
    }
    private static native long invokeArrayInt64(long function, byte[] buffer);

    public float invokeFloat(Function function, HeapInvocationBuffer buffer) {
        return invokeArrayFloat(function.getAddress64(), buffer.array());
    }
    private static native float invokeArrayFloat(long function, byte[] buffer);

    public double invokeDouble(Function function, HeapInvocationBuffer buffer) {
        return invokeArrayDouble(function.getAddress64(), buffer.array());
    }
    private static native double invokeArrayDouble(long function, byte[] buffer);

    public abstract long invokeAddress(Function function, HeapInvocationBuffer buffer);
    private static final class ILP32 extends Foreign {
        private static final Foreign INSTANCE = new ILP32();

        public final int invokeVrI(Function function) {
            return invoke32VrI(function.getAddress32());
        }
        public int invokeIrI(Function function, int arg1) {
            return invoke32IrI(function.getAddress32(), arg1);
        }
        public int invokeIIrI(Function function, int arg1, int arg2) {
            return invoke32IIrI(function.getAddress32(), arg2, arg1);
        }
        public int invokeIIIrI(Function function, int arg1, int arg2, int arg3) {
            return invoke32IIIrI(function.getAddress32(), arg1, arg2, arg3);
        }
        public long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return invokeArrayInt32(function.getAddress64(), buffer.array());
        }
    }
    private static final class LP64 extends Foreign {
        private static final Foreign INSTANCE = new LP64();
        
        public final int invokeVrI(Function function) {
            return invoke64VrI(function.getAddress64());
        }
        public int invokeIrI(Function function, int arg1) {
            return invoke64IrI(function.getAddress64(), arg1);
        }
        public int invokeIIrI(Function function, int arg1, int arg2) {
            return invoke64IIrI(function.getAddress64(), arg2, arg1);
        }
        public int invokeIIIrI(Function function, int arg1, int arg2, int arg3) {
            return invoke64IIIrI(function.getAddress64(), arg1, arg2, arg3);
        }
        public long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return invokeArrayInt64(function.getAddress64(), buffer.array());
        }
    }
}
