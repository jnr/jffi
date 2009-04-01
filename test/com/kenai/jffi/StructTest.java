/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jffi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public class StructTest {

    public StructTest() {
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

    //
    // struct { char c; };
    //
    @Test public void structS8() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        long sint8 = foreign.lookupBuiltinType(Type.SINT8.value());
        long struct = foreign.newStruct(new long[] { sint8 });
        assertEquals("Incorrect size", 1, foreign.getTypeSize(struct));
        assertEquals("Incorrect alignment", 1, foreign.getTypeAlign(struct));
    }

    //
    // struct { char c; int i; };
    //
    @Test public void structS8S32() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        long sint8 = foreign.lookupBuiltinType(Type.SINT8.value());
        long sint32 = foreign.lookupBuiltinType(Type.SINT32.value());
        long struct = foreign.newStruct(new long[] { sint8, sint32 });
        assertEquals("Incorrect size", 8, foreign.getTypeSize(struct));
        assertEquals("Incorrect alignment", 4, foreign.getTypeAlign(struct));
    }

    @Test public void returnS8S32() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        long sint8 = foreign.lookupBuiltinType(Type.SINT8.value());
        long sint32 = foreign.lookupBuiltinType(Type.SINT32.value());
        long struct = foreign.newStruct(new long[] { sint8, sint32 });
        
        Library lib = Library.getCachedInstance("build/libtest.so", Library.LAZY | Library.GLOBAL);
        assertNotNull("Could not open libtest", lib);
        
        long sym = lib.getSymbolAddress("struct_return_s8s32");
        assertNotSame("Could not lookup struct_return_s8s32", 0L, sym);

        long function = foreign.newFunction(sym, struct, new long[0], 0);
        assertNotSame("Could not create function for struct_return_s8s32", 0L, function);
     
        byte[] paramBuffer = new byte[foreign.getFunctionRawParameterSize(function)];
        byte[] returnBuffer = new byte[8];
        foreign.invokeArrayWithReturnBuffer(function, paramBuffer, returnBuffer);
        ByteBuffer buf = ByteBuffer.wrap(returnBuffer).order(ByteOrder.nativeOrder());
        assertEquals("Wrong s8 value", (byte) 0x7f, buf.get(0));
        assertEquals("Wrong s32 value", 0x12345678, buf.getInt(4));
    }
}