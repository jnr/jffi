
package com.kenai.jffi;

public abstract class Invoker {
    final Foreign foreign = Foreign.getInstance();
    final long ADDRESS_MASK = Platform.getPlatform().addressMask();
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

    public final int invokeInt(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? invokeArrayWithObjectsInt32(function, buffer, objectBuffer)
                : foreign.invokeArrayInt32(function.getAddress64(), buffer.array());
    }

    public final long invokeLong(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? foreign.invokeArrayWithObjectsInt64(function.getAddress64(), buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects())
                : foreign.invokeArrayInt64(function.getAddress64(), buffer.array());
    }

    public final float invokeFloat(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? foreign.invokeArrayWithObjectsFloat(function.getAddress64(), buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects())
                : foreign.invokeArrayFloat(function.getAddress64(), buffer.array());
    }
    
    public final double invokeDouble(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? foreign.invokeArrayWithObjectsDouble(function.getAddress64(), buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects())
                : foreign.invokeArrayDouble(function.getAddress64(), buffer.array());
    }
    
    private final int invokeArrayWithObjectsInt32(Function function, HeapInvocationBuffer buffer,
            ObjectBuffer objectBuffer) {
        Object[] objects = objectBuffer.objects();
        int[] info = objectBuffer.info();
        int objectCount = objectBuffer.objectCount();

        switch (objectCount) {
            case 1:
                return foreign.invokeArrayO1Int32(function.getAddress64(), buffer.array(),
                        objects[0], info[0], info[1], info[2]);
            case 2:
                return foreign.invokeArrayO2Int32(function.getAddress64(), buffer.array(),
                        objects[0], info[0], info[1], info[2],
                        objects[1], info[3], info[4], info[5]);
        }

        return foreign.invokeArrayWithObjectsInt32(function.getAddress64(), buffer.array(),
            objectCount, info, objects);
    }
    private static final Invoker getILP32() {
        return ILP32.INSTANCE;
    }
    private static final class ILP32 extends Invoker {
        private static final Invoker INSTANCE = new ILP32();

        public final int invokeVrI(Function function) {
            return foreign.invoke32VrI(function.getAddress32());
        }
        public final int invokeIrI(Function function, int arg1) {
            return foreign.invoke32IrI(function.getAddress32(), arg1);
        }
        public final int invokeIIrI(Function function, int arg1, int arg2) {
            return foreign.invoke32IIrI(function.getAddress32(), arg2, arg1);
        }
        public final int invokeIIIrI(Function function, int arg1, int arg2, int arg3) {
            return foreign.invoke32IIIrI(function.getAddress32(), arg1, arg2, arg3);
        }
        public final long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return (long)invokeInt(function, buffer) & ADDRESS_MASK;
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
            return invokeLong(function, buffer);
        }
    }
}
