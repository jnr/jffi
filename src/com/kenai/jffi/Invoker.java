
package com.kenai.jffi;

public abstract class Invoker {
    protected final Foreign foreign = Foreign.getForeign();
    private static final class SingletonHolder {
        private static final Invoker INSTANCE = Platform.is64()
                ? getLP64() : getILP32();
    }
    public static final Invoker getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private Invoker() {}

    public abstract int invokeVrI(Function function);
    public abstract int invokeIrI(Function function, int arg1);
    public abstract int invokeIIrI(Function function, int arg1, int arg2);
    public abstract int invokeIIIrI(Function function, int arg1, int arg2, int arg3);
    public abstract long invokeAddress(Function function, HeapInvocationBuffer buffer);

    public int invokeInt(Function function, HeapInvocationBuffer buffer) {
        return foreign.invokeArrayInt32(function.getAddress64(), buffer.array());
    }

    public long invokeLong(Function function, HeapInvocationBuffer buffer) {
        return foreign.invokeArrayInt64(function.getAddress64(), buffer.array());
    }

    public float invokeFloat(Function function, HeapInvocationBuffer buffer) {
        return foreign.invokeArrayFloat(function.getAddress64(), buffer.array());
    }
    
    public double invokeDouble(Function function, HeapInvocationBuffer buffer) {
        return foreign.invokeArrayDouble(function.getAddress64(), buffer.array());
    }

    private static final Invoker getILP32() {
        return ILP32.INSTANCE;
    }
    private static final class ILP32 extends Invoker {
        private static final Invoker INSTANCE = new ILP32();

        public final int invokeVrI(Function function) {
            return foreign.invoke32VrI(function.getAddress32());
        }
        public int invokeIrI(Function function, int arg1) {
            return foreign.invoke32IrI(function.getAddress32(), arg1);
        }
        public int invokeIIrI(Function function, int arg1, int arg2) {
            return foreign.invoke32IIrI(function.getAddress32(), arg2, arg1);
        }
        public int invokeIIIrI(Function function, int arg1, int arg2, int arg3) {
            return foreign.invoke32IIIrI(function.getAddress32(), arg1, arg2, arg3);
        }
        public long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return foreign.invokeArrayInt32(function.getAddress64(), buffer.array());
        }
    }
    private static final Invoker getLP64() {
        return LP64.INSTANCE;
    }
    private static final class LP64 extends Invoker {
        private static final Invoker INSTANCE = new LP64();

        public final int invokeVrI(Function function) {
            return foreign.invoke64VrI(function.getAddress64());
        }
        public int invokeIrI(Function function, int arg1) {
            return foreign.invoke64IrI(function.getAddress64(), arg1);
        }
        public int invokeIIrI(Function function, int arg1, int arg2) {
            return foreign.invoke64IIrI(function.getAddress64(), arg2, arg1);
        }
        public int invokeIIIrI(Function function, int arg1, int arg2, int arg3) {
            return foreign.invoke64IIIrI(function.getAddress64(), arg1, arg2, arg3);
        }
        public long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return foreign.invokeArrayInt64(function.getAddress64(), buffer.array());
        }
    }
}
