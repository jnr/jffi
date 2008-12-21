/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jffi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wayne
 */
public class CallContextTest {

    public CallContextTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test public void VrV() {
        long handle = Foreign.getForeign().newCallContext(Type.VOID.value(),
                new int[] { Type.VOID.value() }, 0);
        assertNotSame("Foreign#newCallContext failed", 0L, handle);
        CallContext ctx = new CallContext(Type.VOID, new Type[] { Type.VOID });
    }
    @Test public void VrI() {
        long handle = Foreign.getForeign().newCallContext(Type.INT.value(),
                new int[] { Type.VOID.value() }, 0);
        assertNotSame("Foreign#newCallContext failed", 0L, handle);
    }
    @Test public void IrI() {
        long handle = Foreign.getForeign().newCallContext(Type.INT.value(),
                new int[] { Type.INT.value() }, 0);
        assertNotSame("Foreign#newCallContext failed", 0L, handle);
    }
    @Test public void getpid() {

        long libc = Foreign.getForeign().dlopen("libc.so.6", Library.LAZY | Library.LOCAL);
        long getpid = Foreign.getForeign().dlsym(libc, "getpid");
        Function f = new Function(new Address(getpid), Type.SINT32,
                new Type[] { });
        int pid = Invoker.getInstance().invokeVrI(f);
        System.out.println("pid=" + pid);
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(1);
        pid = Invoker.getInstance().invokeInt(f, paramBuffer);
    }
}