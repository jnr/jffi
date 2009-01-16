
package com.kenai.jffi;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UnitHelper {
    public enum InvokerType {
        Default,
        FastInt,
        FastLong
    }
    public static final String getCLibraryName() {
        switch (Platform.getPlatform().getOS()) {
            case LINUX:
                return "libc.so.6";
            case DARWIN:
                return "libc.dylib";
            case WINDOWS:
                return "msvcrt.dll";
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
            } if (returnType == short.class || returnType == Short.class) {
                return Short.valueOf((short) invoker.invokeInt(function, buffer));
            } if (returnType == int.class || returnType == Integer.class) {
                return Integer.valueOf(invoker.invokeInt(function, buffer));
            } if (returnType == long.class || returnType == Long.class) {
                return Long.valueOf(invoker.invokeLong(function, buffer));
            } if (returnType == float.class || returnType == Float.class) {
                return Float.valueOf(invoker.invokeFloat(function, buffer));
            } if (returnType == double.class || returnType == double.class) {
                return Double.valueOf(invoker.invokeDouble(function, buffer));
            }
            throw new RuntimeException("Unknown return type: " + returnType);
        }
    }
    private static final Number convertResult(Class returnType, Number result) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        } else if (returnType == byte.class || returnType == Byte.class) {
            return result.byteValue();
        } if (returnType == short.class || returnType == Short.class) {
            return result.shortValue();
        } if (returnType == int.class || returnType == Integer.class) {
            return result.intValue();
        } if (returnType == long.class || returnType == Long.class) {
            return result.longValue();
        } if (returnType == float.class || returnType == Float.class) {
            return result.floatValue();
        } if (returnType == double.class || returnType == double.class) {
            return result.doubleValue();
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
        public Object invoke(Object[] args) {
            final int result;
            switch (args.length) {
                case 0:
                    result = Invoker.getInstance().invokeVrI(function);
                    break;
                case 1:
                    result = Invoker.getInstance().invokeIrI(function, ((Number) args[0]).intValue());
                    break;
                case 2:
                    result = Invoker.getInstance().invokeIIrI(function,
                            ((Number) args[0]).intValue(), ((Number) args[1]).intValue());
                    break;
                case 3:
                    result = Invoker.getInstance().invokeIIIrI(function,
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
                    result = Invoker.getInstance().invokeVrL(function);
                    break;
                case 1:
                    result = Invoker.getInstance().invokeLrL(function, ((Number) args[0]).longValue());
                    break;
                case 2:
                    result = Invoker.getInstance().invokeLLrL(function,
                            ((Number) args[0]).longValue(), ((Number) args[1]).longValue());
                    break;
                case 3:
                    result = Invoker.getInstance().invokeLLLrL(function,
                            ((Number) args[0]).longValue(), ((Number) args[1]).longValue(), ((Number) args[2]).longValue());
                    break;
                default:
                    throw new IndexOutOfBoundsException("fast-long invoker limited to 3 parameters");
            }
            return convertResult(returnType, result);
        }
    }
}
