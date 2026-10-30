package com.kenai.jffi;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class CallContextCacheTest {
    @Test
    public void distinguishesFixedParameterCounts() {
        // syscall has one fixed argument, fcntl has two, and ordinary functions
        // have all arguments fixed. Their native parameter types can be identical.
        for (int parameterCount = 2; parameterCount <= 4; parameterCount++) {
            assertFixedParameterCounts(Type.SINT, parameterCount, false);
            assertFixedParameterCounts(Type.SLONG_LONG, parameterCount, true);
        }
    }

    private void assertFixedParameterCounts(Type returnType, int parameterCount, boolean reverse) {
        Type[] parameterTypes = new Type[parameterCount];
        Arrays.fill(parameterTypes, Type.SINT);
        CallContext[] contexts = new CallContext[parameterCount];
        CallContextCache cache = CallContextCache.getInstance();

        for (int i = 0; i < parameterCount; i++) {
            int fixedCount = reverse ? parameterCount - i : i + 1;
            CallContext context = cache.getCallContext(returnType, fixedCount, parameterTypes,
                    CallingConvention.DEFAULT, true);
            assertEquals("requested fixed parameter count", fixedCount, context.fixedParamCount);
            assertSame("identical signatures should reuse the context", context,
                    cache.getCallContext(returnType, fixedCount, parameterTypes.clone(),
                            CallingConvention.DEFAULT, true));
            for (int j = 0; j < i; j++) {
                assertNotSame("different fixed counts require different native ABIs", contexts[j], context);
            }
            contexts[i] = context;
        }

        assertSame("the ordinary overload has all parameters fixed", contexts[reverse ? 0 : parameterCount - 1],
                cache.getCallContext(returnType, parameterTypes, CallingConvention.DEFAULT, true));
    }

    @Test
    public void functionConstructorsPreserveFixedParameterCounts() {
        Type[] parameterTypes = { Type.SINT, Type.SINT };
        Function syscall = new Function(0L, Type.SINT, 1, parameterTypes, CallingConvention.DEFAULT, false);
        Function fcntl = new Function(0L, Type.SINT, 2, parameterTypes, CallingConvention.DEFAULT, false);
        Function ordinary = new Function(0L, Type.SINT, parameterTypes, CallingConvention.DEFAULT, false);

        assertEquals(1, syscall.getCallContext().fixedParamCount);
        assertEquals(2, fcntl.getCallContext().fixedParamCount);
        assertNotSame(syscall.getCallContext(), fcntl.getCallContext());
        assertSame(fcntl.getCallContext(), ordinary.getCallContext());
    }
}
