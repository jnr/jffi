
package com.kenai.jffi;

/**
 * Native function call context
 *
 * This class holds all the information that JFFI needs to correctly call a
 * native function, or to implement a callback from native code to java.
 */
public final class CallContext implements CallInfo {

    /** The return type of this function */
    private final Type returnType;

    /** The parameter types of this function */
    private final Type[] parameterTypes;

    final int flags;

    final long nativeReturnType;

    final long[] nativeParameterTypes;
    
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

        this.flags = (!saveErrno ? Foreign.F_NOERRNO : 0)
                | (convention == CallingConvention.STDCALL ? Foreign.F_STDCALL : Foreign.F_DEFAULT);

        //
        // Keep references to the return and parameter types so they do not get
        // garbage collected
        //
        this.returnType = returnType;
        this.parameterTypes = (Type[]) paramTypes.clone();

        this.nativeReturnType = returnType.handle();
        this.nativeParameterTypes = Type.nativeHandles(paramTypes);
    }

    /**
     * Gets the number of parameters the native function accepts.
     *
     * @return The number of parameters the native function accepts.
     */
    public final int getParameterCount() {
        return parameterTypes.length;
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

    public synchronized final void dispose() {
    }
}
