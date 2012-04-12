package com.kenai.jffi;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Provide a factory for invokedynamic handles that are bound to a particular call context+function
 */
public final class InvokeDynamicSupport {
    private InvokeDynamicSupport() {}

    public static final class Invoker {
        private final Method method;
        private final Object methodHandle;

        Invoker(Method method, Object methodHandle) {
            this.method = method;
            this.methodHandle = methodHandle;
        }

        public Object getMethodHandle() {
            return methodHandle;
        }

        public Method getMethod() {
            return method;
        }
    }

    public static Invoker getFastNumericInvoker(CallContext callContext, long function) {

        Platform.CPU cpu = Platform.getPlatform().getCPU();

        if (!(callContext.getReturnType() instanceof Type.Builtin)) {
            return null;
        }

        if ((callContext.flags & Foreign.F_STDCALL) != 0) {
            return null;
        }

        if (callContext.getParameterCount() > 6) {
            return null;
        }

        boolean isFastInt = false, isFastLong = false;

        switch (callContext.getReturnType().type()) {
            case Foreign.TYPE_SINT8:
            case Foreign.TYPE_UINT8:
            case Foreign.TYPE_SINT16:
            case Foreign.TYPE_UINT16:
            case Foreign.TYPE_SINT32:
            case Foreign.TYPE_UINT32:
                isFastInt = true;
                isFastLong = cpu.dataModel == 64;
                break;

           case Foreign.TYPE_POINTER:
                isFastInt = cpu.dataModel == 32;
                isFastLong = cpu.dataModel == 64;
                break;

            case Foreign.TYPE_SINT64:
            case Foreign.TYPE_UINT64:
                isFastLong = true;
                break;

            case Foreign.TYPE_STRUCT:
                return null;

            case Foreign.TYPE_VOID:
                isFastInt = isFastLong = true;
                break;
        }

        isFastInt &= (cpu == Platform.CPU.I386 || cpu == Platform.CPU.X86_64);
        isFastLong &= (cpu == Platform.CPU.I386 || cpu == Platform.CPU.X86_64);

        for (int i = 0; i < callContext.getParameterCount() && (isFastInt || isFastLong); i++) {
            if (!(callContext.getParameterType(i) instanceof Type.Builtin)) {
                return null;
            }

            switch (callContext.getParameterType(i).type()) {
                case Foreign.TYPE_SINT8:
                case Foreign.TYPE_UINT8:
                case Foreign.TYPE_SINT16:
                case Foreign.TYPE_UINT16:
                case Foreign.TYPE_SINT32:
                case Foreign.TYPE_UINT32:
                    isFastLong &= cpu.dataModel == 64;
                    break;

                case Foreign.TYPE_SINT64:
                case Foreign.TYPE_UINT64:
                    isFastInt = false;
                    break;

                case Foreign.TYPE_POINTER:
                    isFastInt &= cpu.dataModel == 32;
                    isFastLong &= cpu.dataModel == 64;
                    break;

                case Foreign.TYPE_STRUCT:
                    return null;

                default:
                    isFastInt = isFastLong = false;
                    break;
            }
        }

        Class nativeIntClass = isFastInt ? int.class : long.class;
        String methodName = (isFastInt ? "invokeI" : isFastLong ? "invokeL" : "invokeN") + callContext.getParameterCount();

        if ((callContext.flags & Foreign.F_NOERRNO) != 0 && (isFastInt || isFastLong)) {
            methodName += "NoErrno";
        }

        Class[] params = new Class[2 + callContext.getParameterCount()];
        params[0] = long.class;
        params[1] = long.class;
        Arrays.fill(params, 2, params.length, nativeIntClass);

        try {
            Method method = Foreign.class.getDeclaredMethod(methodName, params);
            JSR292 jsr292 = JSR292.INSTANCE;
            Object methodHandle = jsr292.insertArguments(jsr292.unreflect(method), 0, callContext.getAddress(), function);

            return new Invoker(method, methodHandle);

        } catch (Throwable ex) {
            return null;
        }
    }

    static final class JSR292 {
        static final JSR292 INSTANCE = getInstance();

        static boolean isAvailable() {
            return INSTANCE != null;
        }

        private static JSR292 getInstance() {
            try {
                Class lookupClass = Class.forName("java.lang.invoke.MethodHandles$Lookup");
                Class methodHandlesClass = Class.forName("java.lang.invoke.MethodHandles");
                Class methodHandleClass = Class.forName("java.lang.invoke.MethodHandle");
                Method lookupMethod = methodHandlesClass.getDeclaredMethod("lookup");
                Method unreflect = lookupClass.getDeclaredMethod("unreflect", Method.class);
                Method insertArguments = methodHandlesClass.getDeclaredMethod("insertArguments",
                        methodHandleClass, int.class, Object[].class);
                Object lookup = lookupMethod.invoke(methodHandlesClass);
                return new JSR292(lookup, unreflect, methodHandlesClass, insertArguments);
            } catch (Throwable t) {
                return null;
            }
        }

        private final Object lookup;
        private final Method unreflect;
        private final Class methodHandles;
        private final Method insertArguments;

        JSR292(Object lookup, Method unreflect, Class methodHandles, Method insertArguments) {
            this.lookup = lookup;
            this.unreflect = unreflect;
            this.methodHandles = methodHandles;
            this.insertArguments = insertArguments;
        }

        public Object unreflect(Method m) throws Exception {
            return unreflect.invoke(lookup, m);
        }

        public Object insertArguments(Object methodHandle, int index, Object... values) throws Exception {
            return insertArguments.invoke(methodHandles, methodHandle, index, values);
        }
    }

}
