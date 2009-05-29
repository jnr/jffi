
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
    @Test public void s8UsingForeign() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        long sint8 = foreign.lookupBuiltinType(Foreign.TYPE_SINT8);
        long struct = foreign.newStruct(new long[] { sint8 }, false);
        assertEquals("Incorrect size", 1, foreign.getTypeSize(struct));
        assertEquals("Incorrect alignment", 1, foreign.getTypeAlign(struct));
    }

    @Test public void s8UsingStruct() throws Throwable {
        Struct s8 = new Struct(Type.SINT8);
        assertEquals("Incorrect size", 1, s8.size());
        assertEquals("Incorrect alignment", 1, s8.alignment());
    }

    //
    // struct { char c; int i; };
    //
    @Test public void s8s32UsingForeign() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        long sint8 = foreign.lookupBuiltinType(Foreign.TYPE_SINT8);
        long sint32 = foreign.lookupBuiltinType(Foreign.TYPE_SINT32);
        long struct = foreign.newStruct(new long[] { sint8, sint32 }, false);
        assertEquals("Incorrect size", 8, foreign.getTypeSize(struct));
        assertEquals("Incorrect alignment", 4, foreign.getTypeAlign(struct));
    }

    @Test public void s8s32UsingStruct() throws Throwable {
        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);

        assertEquals("Incorrect size", 8, s8s32.size());
        assertEquals("Incorrect alignment", 4, s8s32.alignment());
    }

    @Test public void s8s32ReturnWithDefaultBuffer() throws Throwable {

        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);
        
        Address sym = UnitHelper.findSymbol("struct_return_s8s32");

        Function f = new Function(sym.address, s8s32/*, new Type[0]*/);

        byte[] returnBuffer = Invoker.getInstance().invokeStruct(f, new HeapInvocationBuffer(f));
        ByteBuffer buf = ByteBuffer.wrap(returnBuffer).order(ByteOrder.nativeOrder());
        assertEquals("Wrong s8 value", (byte) 0x7f, buf.get(0));
        assertEquals("Wrong s32 value", 0x12345678, buf.getInt(4));
    }

    @Test public void s8s32ReturnWithProvidedBuffer() throws Throwable {

        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);

        Address sym = UnitHelper.findSymbol("struct_return_s8s32");

        Function f = new Function(sym.address, s8s32/*, new Type[0]*/);

        byte[] returnBuffer = new byte[s8s32.size()];
        Invoker.getInstance().invokeStruct(f, new HeapInvocationBuffer(f), returnBuffer, 0);
        
        ByteBuffer buf = ByteBuffer.wrap(returnBuffer).order(ByteOrder.nativeOrder());
        assertEquals("Wrong s8 value", (byte) 0x7f, buf.get(0));
        assertEquals("Wrong s32 value", 0x12345678, buf.getInt(4));
    }

    @Test public void s8s32ReturnWithProvidedBufferAndOffset() throws Throwable {

        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);

        Address sym = UnitHelper.findSymbol("struct_return_s8s32");

        Function f = new Function(sym.address, s8s32, new Type[0]);

        int adj = 8;
        byte[] returnBuffer = new byte[adj + s8s32.size()];
        Invoker.getInstance().invokeStruct(f, new HeapInvocationBuffer(f), returnBuffer, adj);

        ByteBuffer buf = ByteBuffer.wrap(returnBuffer, adj, s8s32.size()).slice().order(ByteOrder.nativeOrder());

        assertEquals("Wrong s8 value", (byte) 0x7f, buf.get(0));
        assertEquals("Wrong s32 value", 0x12345678, buf.getInt(4));
    }

    @Test public void structS8S32ParameterFromArray() throws Throwable {
        
        Address sym_s8 = UnitHelper.findSymbol("struct_s8s32_get_s8");
        
        Address sym_s32 = UnitHelper.findSymbol("struct_s8s32_get_s32");
        
        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);
        Function get_s8 = new Function(sym_s8.address, Type.SINT32, s8s32 );
        Function get_s32 = new Function(sym_s32.address, Type.SINT32, s8s32);

        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(get_s8);
        ByteBuffer buf = ByteBuffer.allocate(s8s32.size()).order(ByteOrder.nativeOrder());
        buf.put(0, (byte) 0x12);
        buf.putInt(4, 0x87654321);
        
        paramBuffer.putStruct(buf.array(), buf.arrayOffset());

        assertEquals("Wrong s8 value", 0x12, Invoker.getInstance().invokeInt(get_s8, paramBuffer));
        assertEquals("Wrong s32 value", 0x87654321, Invoker.getInstance().invokeInt(get_s32, paramBuffer));
    }

    @Test public void structS8S32ParameterFromPointer() throws Throwable {

        Address sym_s8 = UnitHelper.findSymbol("struct_s8s32_get_s8");
        
        Address sym_s32 = UnitHelper.findSymbol("struct_s8s32_get_s32");
        
        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);
        Function get_s8 = new Function(sym_s8.address, Type.SINT32, s8s32);
        Function get_s32 = new Function(sym_s32.address, Type.SINT32, s8s32);
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(get_s8);

        long struct = MemoryIO.getInstance().allocateMemory(s8s32.size(), true);
        MemoryIO.getInstance().putByte(struct, (byte) 0x12);
        MemoryIO.getInstance().putInt(struct + 4, 0x87654321);

        paramBuffer.putStruct(struct);

        assertEquals("Wrong s8 value", 0x12, Invoker.getInstance().invokeInt(get_s8, paramBuffer));
        assertEquals("Wrong s32 value", 0x87654321, Invoker.getInstance().invokeInt(get_s32, paramBuffer));
    }

    @Test public void structS8S32ParameterFromArrayAndS32() throws Throwable {
        
        Address sym = UnitHelper.findSymbol("struct_s8s32_s32_ret_s32");
        
        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);
        Function function = new Function(sym.address, Type.SINT32, s8s32, Type.SINT32);
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        ByteBuffer buf = ByteBuffer.allocate(s8s32.size()).order(ByteOrder.nativeOrder());
        buf.put(0, (byte) 0x12);
        buf.putInt(4, 0x87654321);
        
        paramBuffer.putStruct(buf.array(), buf.arrayOffset());
        paramBuffer.putInt(0xdeadbeef);

        int retval = Invoker.getInstance().invokeInt(function, paramBuffer);
        assertEquals("Wrong s32 param value", (int) 0xdeadbeef, retval);
    }

    @Test public void structS8S32ParameterFromPointerAndS32() throws Throwable {

        Address sym = UnitHelper.findSymbol("struct_s8s32_s32_ret_s32");
        
        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);
        Function function = new Function(sym.address, Type.SINT32, s8s32, Type.SINT32);
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

    @Test public void structS8S32ParameterFromArrayAndS64() throws Throwable {

        Address sym = UnitHelper.findSymbol("struct_s8s32_s64_ret_s64");
        
        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);
        Function function = new Function(sym.address, Type.SINT64, s8s32, Type.SINT64);

        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        ByteBuffer buf = ByteBuffer.allocate(s8s32.size()).order(ByteOrder.nativeOrder());
        buf.put(0, (byte) 0x12);
        buf.putInt(4, 0x87654321);

        paramBuffer.putStruct(buf.array(), buf.arrayOffset());
        paramBuffer.putLong(0xdeadbeef);

        long retval = Invoker.getInstance().invokeLong(function, paramBuffer);
        assertEquals("Wrong s64 param value", 0xdeadbeef, retval);
    }

    @Test public void structS8S32ParameterFromPointerAndS64() throws Throwable {

        Address sym = UnitHelper.findSymbol("struct_s8s32_s64_ret_s64");
        
        Struct s8s32 = new Struct(new Type[] { Type.SINT8, Type.SINT32 });
        Function function = new Function(sym.address, Type.SINT64, s8s32, Type.SINT64);

        
        long struct = MemoryIO.getInstance().allocateMemory(s8s32.size(), true);
        MemoryIO.getInstance().putByte(struct, (byte) 0x12);
        MemoryIO.getInstance().putInt(struct + 4, 0x87654321);

        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        paramBuffer.putStruct(struct);

        // Add a following int64 param and ensure it is passed
        paramBuffer.putLong(0xdeadbeef);

        long retval = Invoker.getInstance().invokeLong(function, paramBuffer);
        assertEquals("Wrong s64 param value", 0xdeadbeef, retval);
    }

    @Test public void s8s32_set() throws Throwable {
        
        Address sym = UnitHelper.findSymbol("struct_s8s32_set");
        
        Struct s8s32 = new Struct(Type.SINT8, Type.SINT32);
        Function function = new Function(sym.address, s8s32, Type.SINT8, Type.SINT32);
        
        HeapInvocationBuffer paramBuffer = new HeapInvocationBuffer(function);
        paramBuffer.putByte((byte) 0x12);
        paramBuffer.putInt(0x87654321);

        byte[] returnBuffer = Invoker.getInstance().invokeStruct(function, paramBuffer);

        ByteBuffer buf = ByteBuffer.wrap(returnBuffer).order(ByteOrder.nativeOrder());
        assertEquals("Wrong s8 value", (byte) 0x12, buf.get(0));
        assertEquals("Wrong s32 value", 0x87654321, buf.getInt(4));
    }
}

