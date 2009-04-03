
package com.kenai.jffi;

public abstract class Invoker {
    private static final long ADDRESS_SIZE = Platform.getPlatform().addressSize();
    private static final long ADDRESS_MASK = Platform.getPlatform().addressMask();
    
    final Foreign foreign = Foreign.getInstance();
    
    private static final class SingletonHolder {
        private static final Invoker INSTANCE = ADDRESS_SIZE == 64
                ? getLP64() : getILP32();
    }
    public static final Invoker getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private Invoker() {}

    public final int invokeVrI(Function function) {
        return foreign.invokeVrI(function.getContextAddress());
    }

    public final int invokeIrI(Function function, int arg1) {
        return foreign.invokeIrI(function.getContextAddress(), arg1);
    }

    public final int invokeIIrI(Function function, int arg1, int arg2) {
        return foreign.invokeIIrI(function.getContextAddress(), arg1, arg2);
    }

    public final int invokeIIIrI(Function function, int arg1, int arg2, int arg3) {
        return foreign.invokeIIIrI(function.getContextAddress(), arg1, arg2, arg3);
    }
    public abstract long invokeAddress(Function function, HeapInvocationBuffer buffer);

    public final long invokeVrL(Function function) {
        return foreign.invokeVrL(function.getContextAddress());
    }
    public final long invokeLrL(Function function, long arg1) {
        return foreign.invokeLrL(function.getContextAddress(), arg1);
    }
    public final long invokeLLrL(Function function, long arg1, long arg2) {
        return foreign.invokeLLrL(function.getContextAddress(), arg1, arg2);
    }
    public final long invokeLLLrL(Function function, long arg1, long arg2, long arg3) {
        return foreign.invokeLLLrL(function.getContextAddress(), arg1, arg2, arg3);
    }
    public final int invokeInt(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? invokeArrayWithObjectsInt32(function, buffer, objectBuffer)
                : foreign.invokeArrayInt32(function.getContextAddress(), buffer.array());
    }

    public final long invokeLong(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? foreign.invokeArrayWithObjectsInt64(function.getContextAddress(), buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects())
                : foreign.invokeArrayInt64(function.getContextAddress(), buffer.array());
    }

    public final float invokeFloat(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? foreign.invokeArrayWithObjectsFloat(function.getContextAddress(), buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects())
                : foreign.invokeArrayFloat(function.getContextAddress(), buffer.array());
    }
    
    public final double invokeDouble(Function function, HeapInvocationBuffer buffer) {
        ObjectBuffer objectBuffer = buffer.objectBuffer();
        return objectBuffer != null
                ? foreign.invokeArrayWithObjectsDouble(function.getContextAddress(), buffer.array(), objectBuffer.objectCount(), objectBuffer.info(), objectBuffer.objects())
                : foreign.invokeArrayDouble(function.getContextAddress(), buffer.array());
    }

    /**
     * Invokes a function, encoding the return value in a byte array
     *
     * @param function The function to invoke.
     * @param buffer The parameter buffer.
     * @return A byte array with the return value encoded in native byte order.
     */
    public final byte[] invokeBuffer(Function function, HeapInvocationBuffer buffer) {
        byte[] returnBuffer = new byte[function.getReturnType().size()];
        foreign.invokeArrayWithReturnBuffer(function.getContextAddress(), buffer.array(), returnBuffer, 0);

        return returnBuffer;
    }

    /**
     * Invokes a function, encoding the return value in a byte array
     *
     * @param function The function to invoke.
     * @param buffer The parameter buffer.
     * @param returnBuffer The output buffer to place the return value in.
     * @param offset The offset within returnBuffer to place the return value.
     */
    public final void invokeBuffer(Function function, HeapInvocationBuffer buffer, byte[] returnBuffer, int offset) {
        foreign.invokeArrayWithReturnBuffer(function.getContextAddress(), buffer.array(), returnBuffer, offset);
    }
    
    private final int invokeArrayWithObjectsInt32(Function function, HeapInvocationBuffer buffer,
            ObjectBuffer objectBuffer) {
        Object[] objects = objectBuffer.objects();
        int[] info = objectBuffer.info();
        int objectCount = objectBuffer.objectCount();

        switch (objectCount) {
            case 1:
                return foreign.invokeArrayO1Int32(function.getContextAddress(), buffer.array(),
                        objects[0], info[0], info[1], info[2]);
            case 2:
                return foreign.invokeArrayO2Int32(function.getContextAddress(), buffer.array(),
                        objects[0], info[0], info[1], info[2],
                        objects[1], info[3], info[4], info[5]);
        }

        return foreign.invokeArrayWithObjectsInt32(function.getContextAddress(), buffer.array(),
            objectCount, info, objects);
    }
    private static final Invoker getILP32() {
        return ILP32.INSTANCE;
    }
    private static final class ILP32 extends Invoker {
        private static final Invoker INSTANCE = new ILP32();

        public final long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return ((long)invokeInt(function, buffer)) & ADDRESS_MASK;
        }
    }
    private static final Invoker getLP64() {
        return LP64.INSTANCE;
    }
    private static final class LP64 extends Invoker {
        private static final Invoker INSTANCE = new LP64();
        
        public long invokeAddress(Function function, HeapInvocationBuffer buffer) {
            return invokeLong(function, buffer);
        }
    }
}
