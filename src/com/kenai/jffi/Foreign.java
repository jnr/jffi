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

    public abstract int callVrI(Function function);
    public abstract int callIrI(Function function, int arg1);
    private static final native int call32VrI(int function);
    private static final native int call64VrI(long function);
    private static final native int call32IrI(int function, int arg1);
    private static final native int call64IrI(long function, int arg1);

    private static final class ILP32 extends Foreign {
        private static final Foreign INSTANCE = new ILP32();

        public final int callVrI(Function function) {
            return call32VrI(function.getAddress32());
        }
        public int callIrI(Function function, int arg1) {
            return call32IrI(function.getAddress32(), arg1);
        }
    }
    private static final class LP64 extends Foreign {
        private static final Foreign INSTANCE = new LP64();
        
        public final int callVrI(Function function) {
            return call64VrI(function.getAddress64());
        }
        public int callIrI(Function function, int arg1) {
            return call64IrI(function.getAddress64(), arg1);
        }
    }
}
