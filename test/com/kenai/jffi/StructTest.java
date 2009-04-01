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
     
        byte[] paramBuffer = new byte[0];
        byte[] returnBuffer = new byte[8];
        foreign.invokeArrayWithReturnBuffer(function, paramBuffer, returnBuffer);
        ByteBuffer buf = ByteBuffer.wrap(returnBuffer).order(ByteOrder.nativeOrder());
        assertEquals("Wrong s8 value", (byte) 0x7f, buf.get(0));
        assertEquals("Wrong s32 value", 0x12345678, buf.getInt(4));
    }

    @Test public void returnStructS8S32() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        Struct struct = new Struct(new Type[] { Type.SINT8, Type.SINT32 });

        Library lib = Library.getCachedInstance("build/libtest.so", Library.LAZY | Library.GLOBAL);
        assertNotNull("Could not open libtest", lib);

        long sym = lib.getSymbolAddress("struct_return_s8s32");
        assertNotSame("Could not lookup struct_return_s8s32", 0L, sym);

        Function function = new Function(sym, struct, new Type[0]);

        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        byte[] returnBuffer = new byte[8];
        foreign.invokeArrayWithReturnBuffer(function.getAddress64(), paramBuffer.array(), returnBuffer);
        ByteBuffer buf = ByteBuffer.wrap(returnBuffer).order(ByteOrder.nativeOrder());
        assertEquals("Wrong s8 value", (byte) 0x7f, buf.get(0));
        assertEquals("Wrong s32 value", 0x12345678, buf.getInt(4));
    }

    @Test public void paramS8S32() throws Throwable {
        Foreign foreign = Foreign.getInstance();

        Library lib = Library.getCachedInstance("build/libtest.so", Library.LAZY | Library.GLOBAL);
        assertNotNull("Could not open libtest", lib);

        long sym_s8 = lib.getSymbolAddress("struct_s8s32_get_s8");
        assertNotSame("Could not lookup struct_s8s32_get_s8", 0L, sym_s8);

        long sym_s32 = lib.getSymbolAddress("struct_s8s32_get_s32");
        assertNotSame("Could not lookup struct_s8s32_get_s32", 0L, sym_s32);

        Struct s8s32 = new Struct(new Type[] { Type.SINT8, Type.SINT32 });
        Function get_s8 = new Function(sym_s8, Type.SINT32, new Type[] { s8s32 });
        Function get_s32 = new Function(sym_s32, Type.SINT32, new Type[] { s8s32 });
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(get_s8);
        ByteBuffer buf = ByteBuffer.wrap(paramBuffer.array()).order(ByteOrder.nativeOrder());
        buf.put(0, (byte) 0x12);
        buf.putInt(4, 0x87654321);

        int retval = 0;
        retval = foreign.invokeArrayInt32(get_s8.getAddress64(), paramBuffer.array());
        assertEquals("Wrong s8 value", 0x12, retval);
        retval = foreign.invokeArrayInt32(get_s32.getAddress64(), paramBuffer.array());
        assertEquals("Wrong s32 value", 0x87654321, retval);
    }
}