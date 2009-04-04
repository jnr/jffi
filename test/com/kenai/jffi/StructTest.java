
package com.kenai.jffi;

import com.kenai.jffi.UnitHelper.Address;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests C struct parameters and return values using pass/return by value.
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
        long sint8 = foreign.lookupBuiltinType(Type.SINT8.type());
        long struct = foreign.newStruct(new long[] { sint8 }, false);
        assertEquals("Incorrect size", 1, foreign.getTypeSize(struct));
        assertEquals("Incorrect alignment", 1, foreign.getTypeAlign(struct));
    }

    //
    // struct { char c; int i; };
    //
    @Test public void structS8S32() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        long sint8 = foreign.lookupBuiltinType(Type.SINT8.type());
        long sint32 = foreign.lookupBuiltinType(Type.SINT32.type());
        long struct = foreign.newStruct(new long[] { sint8, sint32 }, false);
        assertEquals("Incorrect size", 8, foreign.getTypeSize(struct));
        assertEquals("Incorrect alignment", 4, foreign.getTypeAlign(struct));
    }

    @Test public void returnS8S32() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        long sint8 = foreign.lookupBuiltinType(Type.SINT8.type());
        long sint32 = foreign.lookupBuiltinType(Type.SINT32.type());
        long struct = foreign.newStruct(new long[] { sint8, sint32 }, false);
        
        
        Address sym = UnitHelper.findSymbol("struct_return_s8s32");
        assertNotSame("Could not lookup struct_return_s8s32", 0L, sym);

        long function = foreign.newFunction(sym.address, struct, new long[0], 0);
        assertNotSame("Could not create function for struct_return_s8s32", 0L, function);
     
        byte[] paramBuffer = new byte[0];
        byte[] returnBuffer = new byte[8];
        foreign.invokeArrayReturnStruct(function, paramBuffer, returnBuffer, 0);
        ByteBuffer buf = ByteBuffer.wrap(returnBuffer).order(ByteOrder.nativeOrder());
        assertEquals("Wrong s8 value", (byte) 0x7f, buf.get(0));
        assertEquals("Wrong s32 value", 0x12345678, buf.getInt(4));
    }

    @Test public void returnStructS8S32() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        Struct struct = new Struct(new Type[] { Type.SINT8, Type.SINT32 });

        Address sym = UnitHelper.findSymbol("struct_return_s8s32");
        assertNotNull("Could not lookup struct_return_s8s32", sym);

        Function function = new Function(sym.address, struct, new Type[0]);

        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        byte[] returnBuffer = new byte[8];
        foreign.invokeArrayReturnStruct(function.getContextAddress(), paramBuffer.array(), returnBuffer, 0);
        ByteBuffer buf = ByteBuffer.wrap(returnBuffer).order(ByteOrder.nativeOrder());
        assertEquals("Wrong s8 value", (byte) 0x7f, buf.get(0));
        assertEquals("Wrong s32 value", 0x12345678, buf.getInt(4));
    }

    @Test public void structS8S32FromArray() throws Throwable {
        
        Address sym_s8 = UnitHelper.findSymbol("struct_s8s32_get_s8");
        assertNotNull("Could not lookup struct_s8s32_get_s8", sym_s8);

        Address sym_s32 = UnitHelper.findSymbol("struct_s8s32_get_s32");
        assertNotNull("Could not lookup struct_s8s32_get_s32", sym_s32);

        Struct s8s32 = new Struct(new Type[] { Type.SINT8, Type.SINT32 });
        Function get_s8 = new Function(sym_s8.address, Type.SINT32, new Type[] { s8s32 });
        Function get_s32 = new Function(sym_s32.address, Type.SINT32, new Type[] { s8s32 });
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(get_s8);
        ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder());
        buf.put(0, (byte) 0x12);
        buf.putInt(4, 0x87654321);
        
        paramBuffer.putStruct(buf.array(), buf.arrayOffset());

        int retval = Invoker.getInstance().invokeInt(get_s8, paramBuffer);
        assertEquals("Wrong s8 value", 0x12, retval);
        retval = Invoker.getInstance().invokeInt(get_s32, paramBuffer);
        assertEquals("Wrong s32 value", 0x87654321, retval);
    }

    @Test public void structS8S32FromPointer() throws Throwable {

        Address sym_s8 = UnitHelper.findSymbol("struct_s8s32_get_s8");
        assertNotNull("Could not lookup struct_s8s32_get_s8", sym_s8);

        Address sym_s32 = UnitHelper.findSymbol("struct_s8s32_get_s32");
        assertNotNull("Could not lookup struct_s8s32_get_s32", sym_s32);

        Struct s8s32 = new Struct(new Type[] { Type.SINT8, Type.SINT32 });
        Function get_s8 = new Function(sym_s8.address, Type.SINT32, new Type[] { s8s32 });
        Function get_s32 = new Function(sym_s32.address, Type.SINT32, new Type[] { s8s32 });
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(get_s8);
        long struct = MemoryIO.getInstance().allocateMemory(s8s32.size(), true);
        MemoryIO.getInstance().putByte(struct, (byte) 0x12);
        MemoryIO.getInstance().putInt(struct + 4, 0x87654321);
        paramBuffer.putStruct(struct);

        int retval = Invoker.getInstance().invokeInt(get_s8, paramBuffer);
        assertEquals("Wrong s8 value", 0x12, retval);
        retval = Invoker.getInstance().invokeInt(get_s32, paramBuffer);
        assertEquals("Wrong s32 value", 0x87654321, retval);
    }

    @Test public void structS8S32FromArrayAndS32() throws Throwable {
        
        Address sym = UnitHelper.findSymbol("struct_s8s32_s32_ret_s32");
        assertNotNull("Could not lookup struct_s8s32_s32_ret_s32", sym);

        Struct s8s32 = new Struct(new Type[] { Type.SINT8, Type.SINT32 });
        Function function = new Function(sym.address, Type.SINT32, new Type[] { s8s32, Type.SINT32 });
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        ByteBuffer buf = ByteBuffer.allocate(s8s32.size()).order(ByteOrder.nativeOrder());
        buf.put(0, (byte) 0x12);
        buf.putInt(4, 0x87654321);
        
        paramBuffer.putStruct(buf.array(), buf.arrayOffset());
        paramBuffer.putInt(0xdeadbeef);

        int retval = Invoker.getInstance().invokeInt(function, paramBuffer);
        assertEquals("Wrong s32 param value", (int) 0xdeadbeef, retval);
    }

    @Test public void structS8S32FromPointerAndS32() throws Throwable {

        Address sym = UnitHelper.findSymbol("struct_s8s32_s32_ret_s32");
        assertNotNull("Could not lookup struct_s8s32_s32_ret_s32", sym);

        Struct s8s32 = new Struct(new Type[] { Type.SINT8, Type.SINT32 });
        Function function = new Function(sym.address, Type.SINT32, new Type[] { s8s32, Type.SINT32 });
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        long struct = MemoryIO.getInstance().allocateMemory(s8s32.size(), true);
        MemoryIO.getInstance().putByte(struct, (byte) 0x12);
        MemoryIO.getInstance().putInt(struct + 4, 0x87654321);
        paramBuffer.putStruct(struct);

        // Add a following int32 param and ensure it is passed
        paramBuffer.putInt(0xdeadbeef);

        int retval = Invoker.getInstance().invokeInt(function, paramBuffer);
        assertEquals("Wrong s32 param value", (int) 0xdeadbeef, retval);
    }

    @Test public void structS8S32FromArrayAndS64() throws Throwable {

        Address sym = UnitHelper.findSymbol("struct_s8s32_s64_ret_s64");
        assertNotNull("Could not lookup struct_s8s32_s64_ret_s64", sym);

        Struct s8s32 = new Struct(new Type[] { Type.SINT8, Type.SINT32 });
        Function function = new Function(sym.address, Type.SINT64, new Type[] { s8s32, Type.SINT64 });
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        ByteBuffer buf = ByteBuffer.allocate(s8s32.size()).order(ByteOrder.nativeOrder());
        buf.put(0, (byte) 0x12);
        buf.putInt(4, 0x87654321);

        paramBuffer.putStruct(buf.array(), buf.arrayOffset());
        paramBuffer.putLong(0xdeadbeef);

        long retval = Invoker.getInstance().invokeLong(function, paramBuffer);
        assertEquals("Wrong s64 param value", 0xdeadbeef, retval);
    }

    @Test public void structS8S32FromPointerAndS64() throws Throwable {

        Address sym = UnitHelper.findSymbol("struct_s8s32_s64_ret_s64");
        assertNotNull("Could not lookup struct_s8s32_s64_ret_s64", sym);

        Struct s8s32 = new Struct(new Type[] { Type.SINT8, Type.SINT32 });
        Function function = new Function(sym.address, Type.SINT64, new Type[] { s8s32, Type.SINT64 });
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        long struct = MemoryIO.getInstance().allocateMemory(s8s32.size(), true);
        MemoryIO.getInstance().putByte(struct, (byte) 0x12);
        MemoryIO.getInstance().putInt(struct + 4, 0x87654321);
        paramBuffer.putStruct(struct);

        // Add a following int64 param and ensure it is passed
        paramBuffer.putLong(0xdeadbeef);

        long retval = Invoker.getInstance().invokeLong(function, paramBuffer);
        assertEquals("Wrong s64 param value", 0xdeadbeef, retval);
    }

    @Test public void s8s32_set() throws Throwable {
        Foreign foreign = Foreign.getInstance();

        Address sym = UnitHelper.findSymbol("struct_s8s32_set");
        assertNotNull("Could not lookup struct_s8s32_set", sym);

        Struct s8s32 = new Struct(new Type[] { Type.SINT8, Type.SINT32 });
        Function function = new Function(sym.address, s8s32, new Type[] { Type.SINT8, Type.SINT32 });
        
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        paramBuffer.putByte((byte) 0x12);
        paramBuffer.putInt(0x87654321);

        byte[] returnBuffer = new byte[s8s32.size()];
        foreign.invokeArrayReturnStruct(function.getContextAddress(), paramBuffer.array(), returnBuffer, 0);
        ByteBuffer buf = ByteBuffer.wrap(returnBuffer).order(ByteOrder.nativeOrder());
        assertEquals("Wrong s8 value", (byte) 0x12, buf.get(0));
        assertEquals("Wrong s32 value", 0x87654321, buf.getInt(4));
    }
}

