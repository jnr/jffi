
package com.kenai.jffi;

/**
 * Native function call context
 *
 * This class holds all the information that JFFI needs to correctly call a
 * native function, or to implement a callback from native code to java.
 */
public final class CallContext implements CallInfo {
    /** The native address of the context */
    private final long contextAddress;

    /** Whether the native context has been freed yet */
    private volatile boolean released = false;

    /** The number of parameters this function takes */
    private final int parameterCount;

    /** The size of buffer required when packing parameters */
    private final int rawParameterSize;

    /** The return type of this function */
    private final Type returnType;

    /** The parameter types of this function */
    private final Type[] parameterTypes;

    /**
     * Creates a new instance of <tt>Function</tt> with default calling convention.
     *
     * @param address The native address of the function to invoke.
     * @param returnType The return type of the native function.
     * @param parameterTypes The parameter types the function accepts.
     */
    public CallContext(Type returnType, Type... paramTypes) {
        this(returnType, paramTypes, CallingConvention.DEFAULT, true);
    }

    /**
     * Creates a new instance of <tt>Function</tt>.
     *
     * <tt>Function</tt> instances created with this constructor will save the
     * C errno contents after each call.
     *
     * @param address The native address of the function to invoke.
     * @param returnType The return type of the native function.
     * @param parameterTypes The parameter types the function accepts.
     * @param convention The calling convention of the function.
     */
    public CallContext(Type returnType, Type[] paramTypes, CallingConvention convention) {
        this(returnType, paramTypes, convention, true);
    }

    /**
     * Creates a new instance of <tt>Function</tt>.
     *
     * @param address The native address of the function to invoke.
     * @param returnType The return type of the native function.
     * @param parameterTypes The parameter types the function accepts.
     * @param convention The calling convention of the function.
     * @param saveErrno Whether the errno should be saved or not
     */
    public CallContext(Type returnType, Type[] paramTypes, CallingConvention convention, boolean saveErrno) {

        final int flags = (!saveErrno ? Foreign.F_NOERRNO : 0)
                | (convention == CallingConvention.STDCALL ? Foreign.F_STDCALL : Foreign.F_DEFAULT);

        final long h = Foreign.getInstance().newCallContext(returnType.handle(),
                Type.nativeHandles(paramTypes), flags);
        if (h == 0) {
            throw new RuntimeException("Failed to create native function");
        }
        this.contextAddress = h;

        //
        // Keep references to the return and parameter types so they do not get
        // garbage collected
        //
        this.returnType = returnType;
        this.parameterTypes = (Type[]) paramTypes.clone();

        this.parameterCount = paramTypes.length;
        this.rawParameterSize = Foreign.getInstance().getFunctionRawParameterSize(h);
    }

    /**
     * Gets the number of parameters the native function accepts.
     *
     * @return The number of parameters the native function accepts.
     */
    public final int getParameterCount() {
        return parameterCount;
    }

    /**
     * Gets the number of bytes required to pack all the parameters this function
     * accepts, into a region of memory.
     *
     * @return The number of bytes required to store all paraameters of this function.
     */
    public final int getRawParameterSize() {
        return rawParameterSize;
    }

    /**
     * Gets the address of the function context.
     *
     * @return The address of the native function context struct.
     */
    final long getAddress() {
        return contextAddress;
    }

    /**
     * Gets the native return type of this function.
     *
     * @return The native return type of this function.
     */
    public final Type getReturnType() {
        return returnType;
    }

    /**
     * Gets the type of a parameter.
     *
     * @param index The index of the parameter in the function signature
     * @return The <tt>Type</tt> of the parameter.
     */
    public final Type getParameterType(int index) {
        return parameterTypes[index];
    }

    public synchronized final void free() {
        if (released) {
            throw new RuntimeException("context already freed");
        }
        Foreign.getInstance().freeCallContext(contextAddress);
        released = true;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (contextAddress != 0 && !released) {
                Foreign.getInstance().freeCallContext(contextAddress);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            super.finalize();
        }
    }
}
