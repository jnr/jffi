package com.kenai.jffi;

import org.junit.Test;

import com.kenai.jffi.InvokerTest.HeapInvoker;
import com.kenai.jffi.InvokerTest.NativeInvoker;
import com.kenai.jffi.UnitHelper.Address;

import static org.junit.Assert.assertEquals;

public class VersionTest {

    public VersionTest() {
    }

    @Test public void old_answer() {
        Invoker invoker = new NativeInvoker();

        Address sym = UnitHelper.findSymbolWithVersion("answer", "VERS_1.0");
        Function function = new Function(sym.address, Type.SINT);
        CallContext ctx = new CallContext(Type.SINT);

        long res = invoker.invokeN0(ctx, function.getFunctionAddress());

        assertEquals(41, res);
    }

    @Test public void new_answer() {
        Invoker invoker = new HeapInvoker();

        Address sym = UnitHelper.findSymbolWithVersion("answer", "VERS_1.1");
        Function function = new Function(sym.address, Type.SINT);
        CallContext ctx = new CallContext(Type.SINT);

        long res = invoker.invokeN0(ctx, function.getFunctionAddress());

        assertEquals(42, res);
    }

    @Test(expected = UnsatisfiedLinkError.class)
    public void future_answer() {
        UnitHelper.findSymbolWithVersion("answer", "VERS_1.2");
    }
}
