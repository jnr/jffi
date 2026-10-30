package com.kenai.jffi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VariadicTest {
    @Test
    public void invokesIdenticalTypesWithDifferentFixedParameterCounts() {
        Type[] parameterTypes = { Type.SINT, Type.SINT, Type.SINT };
        Function oneFixed = function("sum_three_one_fixed", 1, parameterTypes);
        Function twoFixed = function("sum_three_two_fixed", 2, parameterTypes);
        Function allFixed = function("sum_three_fixed", 3, parameterTypes);

        assertEquals(81, invoke(oneFixed));
        assertEquals(81, invoke(twoFixed));
        assertEquals(81, invoke(allFixed));
    }

    private Function function(String name, int fixedCount, Type[] parameterTypes) {
        return new Function(UnitHelper.findSymbol(name).address, Type.SINT, fixedCount,
                parameterTypes, CallingConvention.DEFAULT, true);
    }

    private int invoke(Function function) {
        HeapInvocationBuffer buffer = new HeapInvocationBuffer(function);
        buffer.putInt(11);
        buffer.putInt(23);
        buffer.putInt(47);
        return Invoker.getInstance().invokeInt(function, buffer);
    }
}
