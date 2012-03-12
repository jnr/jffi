
package com.kenai.jffi;

/**
 *
 */
final class NativeObjectParameterInvoker extends ObjectParameterInvoker {
    private final Foreign foreign;

    public final boolean isNative() {
        return true;
    }

    NativeObjectParameterInvoker(Foreign foreign) {
        this.foreign = foreign;
    }
    
    public final long invokeN1O1rN(Function function,
                                   long n1,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        return foreign.invokeN1O1(function.getContextAddress(), function.getFunctionAddress(),
                n1, 
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }


    public final long invokeN1O1(CallContext ctx, long fn,
                                   long n1,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        return foreign.invokeN1O1(ctx.getAddress(), fn,
                n1,
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }


    /**
     * Invokes a function with two numeric arguments, and returns a numeric value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param n1 first numeric argument.
     * @param n2 The second numeric argument.
     * @param o1 array or buffer, to be passed as a pointer for the first numeric parameter.
     * @param o1off offset from the start of the array or buffer.
     * @param o1len length of the array to use.
     * @param o1flags object flags (type, direction, parameter index).
     */
    public final long invokeN2O1rN(Function function,
                                   long n1, long n2,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {

        return foreign.invokeN2O1(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2,
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }

    /**
     * Invokes a function with two numeric arguments, and returns a numeric value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param n1 first numeric argument.
     * @param n2 The second numeric argument.
     * @param o1 array or buffer, to be passed as a pointer for the first numeric parameter.
     * @param o1off offset from the start of the array or buffer.
     * @param o1len length of the array to use.
     * @param o1flags object flags (type, direction, parameter index).
     */
    public final long invokeN2O1(CallContext ctx, long function,
                                 long n1, long n2,
                                 Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {

        return foreign.invokeN2O1(ctx.getAddress(), function,
                n1, n2,
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }
    
    /**
     * Invokes a function with two numeric arguments, and returns a numeric value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param n1 The first numeric argument.
     * @param n2 The second numeric argument.
     * @param o1 An Object (array or buffer), to be passed as a pointer.
     * @param o1off The offset from the start of the array or buffer.
     * @param o1len The length of the array to use.
     * @param o1flags Object flags (direction, type, parameter index).
     * @param o2 An Object (array or buffer), to be passed as a pointer.
     * @param o2off The offset from the start of the array or buffer.
     * @param o2len The length of the array to use.
     * @param o2flags Object flags (direction, type, idx).

     */
    public final long invokeN2O2rN(Function function,
                                   long n1, long n2,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                   Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {

        return foreign.invokeN2O2(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2,
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }
    
    /**
     * Invokes a function with two numeric arguments, and returns a numeric value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param n1 The first numeric argument.
     * @param n2 The second numeric argument.
     * @param n3 The third numeric argument.
     * @param o1 An Object (array or buffer), to be passed as a pointer.
     * @param o1off The offset from the start of the array or buffer.
     * @param o1len The length of the array to use.
     * @param o1flags Object flags (direction, type, parameter index).
     */
    public final long invokeN3O1rN(Function function,
                                   long n1, long n2, long n3,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {

        return foreign.invokeN3O1(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, 
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }

    /**
     * Invokes a function with two numeric arguments, and returns a numeric value.
     *
     * @param function The <tt>Function</tt> to invoke.
     * @param n1 The first numeric argument.
     * @param n2 The second numeric argument.
     * @param n3 The third numeric argument.
     * @param o1 An Object (array or buffer), to be passed as a pointer.
     * @param o1off The offset from the start of the array or buffer.
     * @param o1len The length of the array to use.
     * @param o1flags Object flags (direction, type, parameter index).
     * @param o2 An Object (array or buffer), to be passed as a pointer.
     * @param o2off The offset from the start of the array or buffer.
     * @param o2len The length of the array to use.
     * @param o2flags Object flags (direction, type, idx).
     */
    public final long invokeN3O2rN(Function function,
                                   long n1, long n2, long n3,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                   Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {

        return foreign.invokeN3O2(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3,
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }
    
    public final long invokeN3O3rN(Function function,
                                   long n1, long n2, long n3,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                   Object o2, int o2off, int o2len, ObjectParameterInfo o2flags,
                                   Object o3, int o3off, int o3len, ObjectParameterInfo o3flags) {

        return foreign.invokeN3O3(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3,
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len,
                o3, o3flags.asObjectInfo(), o3off, o3len);
    }

    public final long invokeN4O1rN(Function function,
                                   long n1, long n2, long n3, long n4,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {

        return foreign.invokeN4O1(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, n4,
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }

    public final long invokeN4O2rN(Function function,
                                   long n1, long n2, long n3, long n4,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                   Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {

        return foreign.invokeN4O2(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, n4,
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }
    
    public final long invokeN4O3rN(Function function,
                                   long n1, long n2, long n3, long n4,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                   Object o2, int o2off, int o2len, ObjectParameterInfo o2flags,
                                   Object o3, int o3off, int o3len, ObjectParameterInfo o3flags) {

        return foreign.invokeN4O3(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, n4,
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len,
                o3, o3flags.asObjectInfo(), o3off, o3len);
    }

    @Override
    public long invokeN5O1rN(Function function,
                             long n1, long n2, long n3, long n4, long n5,
                             Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        
        return foreign.invokeN5O1(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, n4, n5,
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }

    @Override
    public long invokeN5O2rN(Function function,
                             long n1, long n2, long n3, long n4, long n5,
                             Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                             Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        
        return foreign.invokeN5O2(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, n4, n5,
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }
    
    public final long invokeN5O3rN(Function function,
                                   long n1, long n2, long n3, long n4, long n5,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                   Object o2, int o2off, int o2len, ObjectParameterInfo o2flags,
                                   Object o3, int o3off, int o3len, ObjectParameterInfo o3flags) {

        return foreign.invokeN5O3(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, n4, n5,
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len,
                o3, o3flags.asObjectInfo(), o3off, o3len);
    }

    @Override
    public long invokeN6O1rN(Function function,
                             long n1, long n2, long n3, long n4, long n5, long n6,
                             Object o1, int o1off, int o1len, ObjectParameterInfo o1flags) {
        
        return foreign.invokeN6O1(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, n4, n5, n6,
                o1, o1flags.asObjectInfo(), o1off, o1len);
    }

    @Override
    public long invokeN6O2rN(Function function,
                             long n1, long n2, long n3, long n4, long n5, long n6,
                             Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                             Object o2, int o2off, int o2len, ObjectParameterInfo o2flags) {
        
        return foreign.invokeN6O2(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, n4, n5, n6,
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len);
    }
    
    public final long invokeN6O3rN(Function function,
                                   long n1, long n2, long n3, long n4, long n5, long n6,
                                   Object o1, int o1off, int o1len, ObjectParameterInfo o1flags,
                                   Object o2, int o2off, int o2len, ObjectParameterInfo o2flags,
                                   Object o3, int o3off, int o3len, ObjectParameterInfo o3flags) {

        return foreign.invokeN6O3(function.getContextAddress(), function.getFunctionAddress(),
                n1, n2, n3, n4, n5, n6,
                o1, o1flags.asObjectInfo(), o1off, o1len,
                o2, o2flags.asObjectInfo(), o2off, o2len,
                o3, o3flags.asObjectInfo(), o3off, o3len);
    }
}
