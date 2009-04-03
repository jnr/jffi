
package com.kenai.jffi;

/**
 * Native function invocation context
 *
 * This class holds all the information that JFFI needs to correctly call a
 * native function.
 */
public final class Function {
    /** The native address of the context */
    private final int address32;

    /** The native address of the context */
    private final long address;

    /** The number of parameters this function takes */
    private final int parameterCount;

    /** The size of buffer required when packing parameters */
    private final int rawParameterSize;

    /** The return type of this function */
    final Type returnType;

    /** The parameter types of this function */
    final Type[] paramTypes;

    /**
     * Creates a new instance of <tt>Function</tt>.
     *
     * @param address The native address of the function to invoke.
     * @param returnType The return type of the native function.
     * @param paramTypes The parameter types the function accepts.
     * @param convention The calling convention of the function.
     */
    public Function(long address, Type returnType, Type[] paramTypes, CallingConvention convention) {

        final long h = Foreign.getInstance().newFunction(address,
                returnType.handle(), Type.nativeHandles(paramTypes),
                convention == CallingConvention.STDCALL ? 1 : 0);
        if (h == 0) {
            throw new RuntimeException("Failed to create native function");
        }

        //
        // Keep references to the return and parameter types so they do not get
        // garbage collected
        //
        this.returnType = returnType;
        this.paramTypes = (Type[]) paramTypes.clone();

        this.parameterCount = paramTypes.length;
        this.rawParameterSize = Foreign.getInstance().getFunctionRawParameterSize(h);
        this.address = h;
        this.address32 = (int) h;
    }

    /**
     * Creates a new instance of <tt>Function</tt> with default calling convention.
     *
     * @param address The native address of the function to invoke.
     * @param returnType The return type of the native function.
     * @param paramTypes The parameter types the function accepts.
     */
    public Function(long address, Type returnType, Type[] paramTypes) {
        this(address, returnType, paramTypes, CallingConvention.DEFAULT);
    }

    /**
     * Gets the number of parameters the native function accepts.
     *
     * @return The number of parameters the native function accepts.
     */
    final int getParameterCount() {
        return parameterCount;
    }

    /**
     * Gets the number of bytes required to pack all the parameters this function
     * accepts, into a region of memory.
     *
     * @return The number of bytes required to store all paraameters of this function.
     */
    final int getRawParameterSize() {
        return rawParameterSize;
    }

    /**
     * Gets the address of the function context.
     *
     * @return The address of the native function context struct.
     */
    final long getContextAddress() {
        return address;
    }

    /**
     * Gets the native return type of this function.
     *
     * @return The native return type of this function.
     */
    final Type getReturnType() {
        return returnType;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            if (address != 0) {
                Foreign.getInstance().freeFunction(address);
            }
        } finally {
            super.finalize();
        }
    }
}
