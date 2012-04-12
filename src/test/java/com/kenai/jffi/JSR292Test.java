/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jffi;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class JSR292Test {

    public JSR292Test() {
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

    @Test public void testInvokeDynamicAvailable() throws Throwable {
        CallContext context = CallContext.getCallContext(Type.VOID, new Type[0], CallingConvention.DEFAULT, true);
        InvokeDynamicSupport.Invoker invoker = InvokeDynamicSupport.getFastNumericInvoker(context, 0xdeadbeef);
        if (Platform.getPlatform().getJavaMajorVersion() >= 7) {
            assertNotNull("invoke dynamic support should be available on JDK 7+", invoker);
            assertNotNull(invoker.getMethod());
            assertNotNull(invoker.getMethodHandle());
        } else {
            assertNull("invoke dynamic support should not be available on < JDK 7", invoker);
        }
    }
}
