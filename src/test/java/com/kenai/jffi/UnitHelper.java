
package com.kenai.jffi;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UnitHelper {
    public enum InvokerType {
        Default,
        FastInt,
        FastLong,
        FastNumeric,
        PointerArray
    }
    public static final class Address extends java.lang.Number {

        public final int SIZE = Platform.getPlatform().addressSize();
        public final long MASK = Platform.getPlatform().addressMask();

        public final long address;

        public Address(long address) {
            this.address = address & MASK;
        }

        public Address(Closure.Handle closure) {
            this(closure.getAddress());
        }

        @Override
        public int intValue() {
            return (int) address;
        }

        @Override
        public long longValue() {
            return address;
        }

        @Override
        public float floatValue() {
            return (float) address;
        }

        @Override
        public double doubleValue() {
            return (double) address;
        }
    }


    public static final String getCLibraryName() {
        switch (Platform.getPlatform().getOS()) {
            case LINUX:
                return "libc.so.6";
            case DARWIN:
                return "libc.dylib";
            case WINDOWS:
                return "msvcrt.dll";
            case AIX:
                if (Platform.getPlatform().addressSize() == 32){
                    return "libc.a(shr.o)";
                } else {
                    return "libc.a(shr_64.o)";
                }
            default:
                return "libc.so";
        }
    }
    public static <T> T loadTestLibrary(Class<T> interfaceClass) {
        return loadTestLibrary(interfaceClass, InvokerType.Default);
    }
    public static <T> T loadTestLibrary(Class<T> interfaceClass, InvokerType invokerType) {
        String name = Platform.getPlatform().mapLibraryName("test");

        return loadLibrary(new File("build", name).getAbsolutePath(), interfaceClass, invokerType);
    }

    /**
     * Creates a new InvocationHandler mapping methods in the <tt>interfaceClass</tt>
     * to functions in the native library.
     * @param <T> the type of <tt>interfaceClass</tt>
     * @param libraryName the native library to load
     * @param interfaceClass the interface that contains the native method description
     * @return a new instance of <tt>interfaceClass</tt> that can be used to call
     * functions in the native library.
     */
    public static <T> T loadLibrary(String name, Class<T> interfaceClass, InvokerType invokerType) {
        Library lib = Library.getCachedInstance(name, Library.LAZY);
        if (lib == null) {
            throw new UnsatisfiedLinkError(String.format("Could not load '%s': %s",
                        name, Library.getLastError()));
        }
        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{ interfaceClass },
                new NativeInvocationHandler(lib, invokerType)));
    }
    private static final class LibraryHolder {
        static final Library libtest = Library.getCachedInstance(
                new File("build", Platform.getPlatform().mapLibraryName("test")).getAbsolutePath(), Library.LAZY);
    }
    public static Address findSymbol(String name) {
        final long address = LibraryHolder.libtest.getSymbolAddress(name);
        if (address == 0L) {
            throw new UnsatisfiedLinkError("Could not locate symbol '" + name + "'");
        }
        return new Address(address);
    }
    private static final class NativeInvocationHandler implements InvocationHandler {
        private final ConcurrentMap<Method, MethodInvoker> invokers
            = new ConcurrentHashMap<Method, MethodInvoker>();
        private final Library library;
        private final InvokerType invokerType;
        public NativeInvocationHandler(Library library, InvokerType invokerType) {
            this.library = library;
            this.invokerType = invokerType;
        }

        public Object invoke(Object self, Method method, Object[] argArray) throws Throwable {
            return getMethodInvoker(method).invoke(argArray);
        }
        /**
         * Gets the {@link Invoker} for a method.
         *
         * @param method the method defined in the interface class
         * @return the <tt>Invoker</tt> to use to invoke the native function
         */
        private final MethodInvoker getMethodInvoker(Method method) {
            MethodInvoker invoker = invokers.get(method);
            if (invoker != null) {
                return invoker;
            }
            invokers.put(method, invoker = createInvoker(library, method, invokerType));
            return invoker;
        }
    }
    private static final MethodInvoker createInvoker(Library library, Method method, InvokerType invokerType) {
        Class returnType = method.getReturnType();
        Class[] parameterTypes = method.getParameterTypes();
        Type ffiReturnType = convertClassToFFI(returnType);
        Type[] ffiParameterTypes = new Type[parameterTypes.length];
        for (int i = 0; i < ffiParameterTypes.length; ++i) {
            ffiParameterTypes[i] = convertClassToFFI(parameterTypes[i]);
        }
        final long address = library.getSymbolAddress(method.getName());
        if (address == 0) {
            throw new UnsatisfiedLinkError(String.format("Could not locate '%s': %s",
                    method.getName(), Library.getLastError()));
        }
        Function function = new Function(address, ffiReturnType, ffiParameterTypes);
        switch (invokerType) {
            case FastInt:
                return new FastIntMethodInvoker(library, function, returnType, parameterTypes);
            case FastLong:
                return new FastLongMethodInvoker(library, function, returnType, parameterTypes);
            case FastNumeric:
                return new FastNumericMethodInvoker(library, function, returnType, parameterTypes);
            case PointerArray:
                return new PointerArrayMethodInvoker(library, function, returnType, parameterTypes);
            case Default:
                return new DefaultMethodInvoker(library, function, returnType, parameterTypes);
            default:
                throw new RuntimeException("Unsupported InvokerType: " + invokerType);
        }
    }
    private static Type convertClassToFFI(Class type) {
        if (type == void.class || type == Void.class) {
            return Type.VOID;
        } else if (type == byte.class || type == Byte.class) {
            return Type.SINT8;
        } else if (type == short.class || type == Short.class) {
            return Type.SINT16;
        } else if (type == int.class || type == Integer.class) {
            return Type.SINT32;
        } else if (type == long.class || type == Long.class) {
            return Type.SINT64;
        } else if (type == float.class || type == Float.class) {
            return Type.FLOAT;
        } else if (type == double.class || type == Double.class) {
            return Type.DOUBLE;
        } else if (BigDecimal.class.isAssignableFrom(type)) {
            return Type.LONGDOUBLE;
        } else if (Address.class.isAssignableFrom(type)) {
            return Type.POINTER;
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
    private static interface MethodInvoker {
        public Object invoke(Object[] args);
    }
    private static final class DefaultMethodInvoker implements MethodInvoker {
        private final Library library;
        private final Function function;
        private final Class returnType;
        private final Class[] parameterTypes;
        public DefaultMethodInvoker(Library library, Function function, Class returnType, Class[] parameterTypes) {
            this.library = library;
            this.function = function;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }

        public Object invoke(Object[] args) {
            HeapInvocationBuffer buffer = new HeapInvocationBuffer(function);
            for (int i = 0; i < args.length; ++i) {
                if (parameterTypes[i] == byte.class || parameterTypes[i] == Byte.class) {
                    buffer.putByte(((Number) args[i]).intValue());
                } else if (parameterTypes[i] == short.class || parameterTypes[i] == Short.class) {
                    buffer.putShort(((Number) args[i]).intValue());
                } else if (parameterTypes[i] == int.class || parameterTypes[i] == Integer.class) {
                    buffer.putInt(((Number) args[i]).intValue());
                } else if (parameterTypes[i] == long.class || parameterTypes[i] == Long.class) {
                    buffer.putLong(((Number) args[i]).longValue());
                } else if (parameterTypes[i] == float.class || parameterTypes[i] == Float.class) {
                    buffer.putFloat(((Number) args[i]).floatValue());
                } else if (parameterTypes[i] == double.class || parameterTypes[i] == Double.class) {
                    buffer.putDouble(((Number) args[i]).doubleValue());
                } else if (BigDecimal.class.isAssignableFrom(parameterTypes[i])) {
                    buffer.putLongDouble(BigDecimal.class.cast(args[i]));
                } else if (Address.class.isAssignableFrom(parameterTypes[i])) {
                    buffer.putAddress(((Address) args[i]).address);
                } else {
                    throw new RuntimeException("Unknown parameter type: " + parameterTypes[i]);
                }
            }
            Invoker invoker = Invoker.getInstance();
            if (returnType == void.class || returnType == Void.class) {
                invoker.invokeInt(function, buffer);
                return null;
            } else if (returnType == byte.class || returnType == Byte.class) {
                return Byte.valueOf((byte) invoker.invokeInt(function, buffer));
            } else if (returnType == short.class || returnType == Short.class) {
                return Short.valueOf((short) invoker.invokeInt(function, buffer));
            } else if (returnType == int.class || returnType == Integer.class) {
                return Integer.valueOf(invoker.invokeInt(function, buffer));
            } else if (returnType == long.class || returnType == Long.class) {
                return Long.valueOf(invoker.invokeLong(function, buffer));
            } else if (returnType == float.class || returnType == Float.class) {
                return Float.valueOf(invoker.invokeFloat(function, buffer));
            } else if (returnType == double.class || returnType == Double.class) {
                return Double.valueOf(invoker.invokeDouble(function, buffer));

            } else if (BigDecimal.class.isAssignableFrom(returnType)) {
                return invoker.invokeBigDecimal(function, buffer);

            } else if (Address.class.isAssignableFrom(returnType)) {
                return new Address(invoker.invokeAddress(function, buffer));
            }
            throw new RuntimeException("Unknown return type: " + returnType);
        }
    }
    private static final Number convertResult(Class returnType, Number result) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        } else if (returnType == byte.class || returnType == Byte.class) {
            return result.byteValue();
        } else if (returnType == short.class || returnType == Short.class) {
            return result.shortValue();
        } else if (returnType == int.class || returnType == Integer.class) {
            return result.intValue();
        } else if (returnType == long.class || returnType == Long.class) {
            return result.longValue();
        } else if (returnType == float.class || returnType == Float.class) {
            return Float.intBitsToFloat(result.intValue());
        } else if (returnType == double.class || returnType == Double.class) {
            return Double.longBitsToDouble(result.longValue());
        } else if (Address.class.isAssignableFrom(returnType)) {
            return new Address(result.longValue());
        }
        throw new RuntimeException("Unknown return type: " + returnType);
    }
    private static final class FastIntMethodInvoker implements MethodInvoker {
        private final Library library;
        private final Function function;
        private final Class returnType;
        private final Class[] parameterTypes;

        public FastIntMethodInvoker(Library library, Function function, Class returnType, Class[] parameterTypes) {
            this.library = library;
            this.function = function;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }

        private static final boolean isFloat(Class c) {
            return Float.class.isAssignableFrom(c) || float.class == c;
        }

        private static final int i(Object value) {
            return value instanceof Float
                ? Float.floatToRawIntBits(((Float) value).floatValue())
                : ((Number) value).intValue();
        }

        public Object invoke(Object[] args) {
            final int result;
            switch (args.length) {
                case 0:
                    result = Invoker.getInstance().invokeI0(function.getCallContext(), function.getFunctionAddress());
                    break;
                case 1:
                    result = Invoker.getInstance().invokeI1(function.getCallContext(), function.getFunctionAddress(), i(args[0]));
                    break;
                case 2:
                    result = Invoker.getInstance().invokeI2(function.getCallContext(), function.getFunctionAddress(),
                            ((Number) args[0]).intValue(), ((Number) args[1]).intValue());
                    break;
                case 3:
                    result = Invoker.getInstance().invokeI3(function.getCallContext(), function.getFunctionAddress(),
                            ((Number) args[0]).intValue(), ((Number) args[1]).intValue(), ((Number) args[2]).intValue());
                    break;
                default:
                    throw new IndexOutOfBoundsException("fast-int invoker limited to 3 parameters");
            }
            return convertResult(returnType, result);
        }
    }
    private static final class FastLongMethodInvoker implements MethodInvoker {
        private final Library library;
        private final Function function;
        private final Class returnType;
        private final Class[] parameterTypes;
        public FastLongMethodInvoker(Library library, Function function, Class returnType, Class[] parameterTypes) {
            this.library = library;
            this.function = function;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }
        public Object invoke(Object[] args) {
            final long result;
            switch (args.length) {
                case 0:
                    result = Invoker.getInstance().invokeL0(function.getCallContext(), function.getFunctionAddress());
                    break;
                case 1:
                    result = Invoker.getInstance().invokeL1(function.getCallContext(), function.getFunctionAddress(), ((Number) args[0]).longValue());
                    break;
                case 2:
                    result = Invoker.getInstance().invokeL2(function.getCallContext(), function.getFunctionAddress(),
                            ((Number) args[0]).longValue(), ((Number) args[1]).longValue());
                    break;
                case 3:
                    result = Invoker.getInstance().invokeL3(function.getCallContext(), function.getFunctionAddress(),
                            ((Number) args[0]).longValue(), ((Number) args[1]).longValue(), ((Number) args[2]).longValue());
                    break;
                default:
                    throw new IndexOutOfBoundsException("fast-long invoker limited to 3 parameters");
            }
            return convertResult(returnType, result);
        }
    }

    private static final class FastNumericMethodInvoker implements MethodInvoker {
        private final Library library;
        private final Function function;
        private final Class returnType;
        private final Class[] parameterTypes;

        public FastNumericMethodInvoker(Library library, Function function, Class returnType, Class[] parameterTypes) {
            this.library = library;
            this.function = function;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }


        public Object invoke(Object[] args) {
            final long result;
            switch (args.length) {
                case 0:
                    result = Invoker.getInstance().invokeN0(function.getCallContext(), function.getFunctionAddress());
                    break;
                case 1:
                    result = Invoker.getInstance().invokeN1(function.getCallContext(), function.getFunctionAddress(), l(args[0]));
                    break;
                case 2:
                    result = Invoker.getInstance().invokeN2(function.getCallContext(), function.getFunctionAddress(), l(args[0]), l(args[1]));
                    break;
                case 3:
                    result = Invoker.getInstance().invokeN3(function.getCallContext(), function.getFunctionAddress(), l(args[0]), l(args[1]), l(args[1]));
                    break;
                default:
                    throw new IndexOutOfBoundsException("fast-numeric invoker limited to 3 parameters");
            }
            return convertResult(returnType, result);
        }

        private static final long l(Object arg) {
            if (arg instanceof Float) {
                return Float.floatToRawIntBits(((Float) arg).floatValue());
            } else if (arg instanceof Double) {
                return Double.doubleToRawLongBits(((Double) arg).doubleValue());
            } else {
                return ((Number) arg).longValue();
            }
        }
    }


    private static final class PointerArrayMethodInvoker implements MethodInvoker {
        private static final MemoryIO Memory = MemoryIO.getInstance();
        private final Library library;
        private final Function function;
        private final Class returnType;
        private final Class[] parameterTypes;
        public PointerArrayMethodInvoker(Library library, Function function, Class returnType, Class[] parameterTypes) {
            this.library = library;
            this.function = function;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }
        private static final class MemoryHolder {
            private final long address;

            public MemoryHolder(long address) {
                this.address = address;
            }

            @Override
            protected void finalize() throws Throwable {
                MemoryIO.getInstance().freeMemory(address);
            }

        }

        private long getNativeLongReturnValue(long address) {
            return Platform.getPlatform().longSize() == 32
                    ? Memory.getInt(address) : Memory.getLong(address);
        }

        public Object invoke(Object[] args) {
            MemoryHolder[] memoryHolders = new MemoryHolder[function.getParameterCount()];
            long[] parameterAddresses = new long[function.getParameterCount()];
            for (int i = 0; i < parameterAddresses.length; ++i) {
                // Allocate 8 bytes; enough to store long long and double
                parameterAddresses[i] = Memory.allocateMemory(8, true);
                memoryHolders[i] = new MemoryHolder(parameterAddresses[i]);
            }

            for (int i = 0; i < args.length; ++i) {
                if (parameterTypes[i] == byte.class || parameterTypes[i] == Byte.class) {
                    Memory.putByte(parameterAddresses[i], ((Number) args[i]).byteValue());
                } else if (parameterTypes[i] == short.class || parameterTypes[i] == Short.class) {
                    Memory.putShort(parameterAddresses[i], ((Number) args[i]).shortValue());
                } else if (parameterTypes[i] == int.class || parameterTypes[i] == Integer.class) {
                    Memory.putInt(parameterAddresses[i], ((Number) args[i]).intValue());
                } else if (parameterTypes[i] == long.class || parameterTypes[i] == Long.class) {
                    Memory.putLong(parameterAddresses[i], ((Number) args[i]).longValue());
                } else if (parameterTypes[i] == float.class || parameterTypes[i] == Float.class) {
                    Memory.putFloat(parameterAddresses[i], ((Number) args[i]).floatValue());
                } else if (parameterTypes[i] == double.class || parameterTypes[i] == Double.class) {
                    Memory.putDouble(parameterAddresses[i], ((Number) args[i]).doubleValue());
                } else if (Address.class.isAssignableFrom(parameterTypes[i])) {
                    Memory.putAddress(parameterAddresses[i], ((Address) args[i]).address);
                } else {
                    throw new RuntimeException("Unknown parameter type: " + parameterTypes[i]);
                }
            }
            long returnBuffer = Memory.allocateMemory(8, true);
            try {
                Invoker.getInstance().invoke(function, returnBuffer, parameterAddresses);

                if (returnType == void.class || returnType == Void.class) {
                    return null;
                } else if (returnType == byte.class || returnType == Byte.class) {
                    return (byte) getNativeLongReturnValue(returnBuffer);
                } else if (returnType == short.class || returnType == Short.class) {
                    return (short) getNativeLongReturnValue(returnBuffer);
                } else if (returnType == int.class || returnType == Integer.class) {
                    return (int) getNativeLongReturnValue(returnBuffer);
                } else if (returnType == long.class || returnType == Long.class) {
                    return Memory.getLong(returnBuffer);
                } else if (returnType == float.class || returnType == Float.class) {
                    return Memory.getFloat(returnBuffer);
                } else if (returnType == double.class || returnType == Double.class) {
                    return Memory.getDouble(returnBuffer);
                } else if (Address.class.isAssignableFrom(returnType)) {
                    return new Address(Memory.getAddress(returnBuffer));
                }
                throw new RuntimeException("Unknown return type: " + returnType);
            } finally {
                Memory.freeMemory(returnBuffer);
            }
        }
    }
}
