
package com.kenai.jffi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MemoryTest {

    public MemoryTest() {
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
    @Test public void allocateUnaligned() {
        long memory = MemoryIO.getInstance().allocateMemory(1024, false);
        assertNotSame("Could not allocate memory", 0L, memory);
        MemoryIO.getInstance().freeMemory(memory);
    }
    @Test public void zeroTerminatedByteArray() {
        byte[] MAGIC = { 't', 'e', 's', 't' };
        long memory = MemoryIO.getInstance().allocateMemory(MAGIC.length + 1, true);
        MemoryIO.getInstance().putByteArray(memory, MAGIC, 0, MAGIC.length);
        byte[] string = MemoryIO.getInstance().getZeroTerminatedByteArray(memory);
        assertArrayEquals(MAGIC, string);
    }
    @Test public void zeroTerminatedByteArrayWithLength() {
        byte[] MAGIC = { 't', 'e', 's', 't' };
        long memory = MemoryIO.getInstance().allocateMemory(MAGIC.length + 1, true);
        MemoryIO.getInstance().putByteArray(memory, MAGIC, 0, MAGIC.length);
        MemoryIO.getInstance().putByte(memory + 4, (byte) 0xff);
        byte[] string = MemoryIO.getInstance().getZeroTerminatedByteArray(memory, 4);
        assertArrayEquals(MAGIC, string);
    }

    @Test public void putZeroTerminatedByteArray() {
        final byte[] DIRTY = { 'd', 'i', 'r', 't', 'y' };
        final byte[] MAGIC = { 't', 'e', 's', 't' };
        long memory = MemoryIO.getInstance().allocateMemory(MAGIC.length + 1, true);
        MemoryIO.getInstance().putByteArray(memory, DIRTY, 0, DIRTY.length);

        MemoryIO.getInstance().putZeroTerminatedByteArray(memory, MAGIC, 0, MAGIC.length);
        assertArrayEquals("String not written to native memory", MAGIC,
                MemoryIO.getInstance().getZeroTerminatedByteArray(memory, 4));
        assertEquals("String not NUL terminated", (byte)0, MemoryIO.getInstance().getByte(memory + 4));
    }


    private void testGetByteArray(MemoryIO io) {
        final int size = 65536;
        long memory = io.allocateMemory(size, true);
        byte[] expected = new byte[size];
        for (int i = 0; i < size; i++) {
            MemoryIO.getInstance().putByte(memory + i, expected[i] = (byte) i);
        }
        byte[] actual = new byte[size];
        io.getByteArray(memory, actual, 0, actual.length);
        assertArrayEquals(expected, actual);
    }

    @Test public void testGetByteArray() {
        testGetByteArray(MemoryIO.getCheckedInstance());
    }

    @Test public void testGetByteArrayChecked() {
        testGetByteArray(MemoryIO.getCheckedInstance());
    }

    private void testPutByteArray(MemoryIO io) {
        final int size = 65536;
        long memory = io.allocateMemory(size, true);
        byte[] array = new byte[size];
        for (int i = 0; i < size; i++) array[i] = (byte) i;
        io.putByteArray(memory, array, 0, array.length);
        for (int i = 0; i < size; i++) {
            assertEquals(array[i], MemoryIO.getInstance().getByte(memory + i));
        }
    }

    @Test public void testPutByteArray() {
        testPutByteArray(MemoryIO.getCheckedInstance());
    }

    @Test public void testPutByteArrayChecked() {
        testPutByteArray(MemoryIO.getCheckedInstance());
    }


    private void testGetShortArray(MemoryIO io) {
        final int size = 65536;
        long memory = io.allocateMemory(size * 2, true);
        short[] expected = new short[size];
        for (int i = 0; i < size; i++) {
            MemoryIO.getInstance().putShort(memory + (i * 2), expected[i] = (short) i);
        }
        short[] actual = new short[size];
        io.getShortArray(memory, actual, 0, actual.length);
        assertArrayEquals(expected, actual);
    }

    @Test public void testGetshortArray() {
        testGetShortArray(MemoryIO.getCheckedInstance());
    }

    @Test public void testGetshortArrayChecked() {
        testGetShortArray(MemoryIO.getCheckedInstance());
    }

    private void testPutShortArray(MemoryIO io) {
        final int size = 65536;
        long memory = io.allocateMemory(size * 2, true);
        short[] array = new short[size];
        for (int i = 0; i < size; i++) array[i] = (short) i;
        io.putShortArray(memory, array, 0, array.length);
        for (int i = 0; i < size; i++) {
            assertEquals(array[i], MemoryIO.getInstance().getShort(memory + (i * 2)));
        }
    }

    @Test public void testPutShortArray() {
        testPutShortArray(MemoryIO.getCheckedInstance());
    }

    @Test public void testPutShortArrayChecked() {
        testPutShortArray(MemoryIO.getCheckedInstance());
    }

    private void testGetIntArray(MemoryIO io) {
        final int size = 65536;
        long memory = io.allocateMemory(size * 4, true);
        int[] expected = new int[size];
        for (int i = 0; i < size; i++) {
            MemoryIO.getInstance().putInt(memory + (i * 4), expected[i] = i);
        }
        int[] actual = new int[size];
        io.getIntArray(memory, actual, 0, actual.length);
        assertArrayEquals(expected, actual);
    }

    @Test public void testGetintArray() {
        testGetIntArray(MemoryIO.getCheckedInstance());
    }

    @Test public void testGetintArrayChecked() {
        testGetIntArray(MemoryIO.getCheckedInstance());
    }
    
    private void testPutIntArray(MemoryIO io) {
        final int size = 65536;
        long memory = io.allocateMemory(size * 4, true);
        int[] array = new int[size];
        for (int i = 0; i < size; i++) array[i] = i;
        io.putIntArray(memory, array, 0, array.length);
        for (int i = 0; i < size; i++) {
            assertEquals(array[i], MemoryIO.getInstance().getInt(memory + (i * 4)));
        }
    }

    @Test public void testPutIntArray() {
        testPutIntArray(MemoryIO.getCheckedInstance());
    }

    @Test public void testPutIntArrayChecked() {
        testPutIntArray(MemoryIO.getCheckedInstance());
    }

    private void testGetLongArray(MemoryIO io) {
        final int size = 65536;
        long memory = io.allocateMemory(size * 8, true);
        long[] expected = new long[size];
        for (int i = 0; i < size; i++) {
            MemoryIO.getInstance().putLong(memory + (i * 8), expected[i] = (long) i);
        }
        long[] actual = new long[size];
        io.getLongArray(memory, actual, 0, actual.length);
        assertArrayEquals(expected, actual);
    }

    @Test public void testGetlongArray() {
        testGetLongArray(MemoryIO.getCheckedInstance());
    }

    @Test public void testGetlongArrayChecked() {
        testGetLongArray(MemoryIO.getCheckedInstance());
    }

    private void testPutLongArray(MemoryIO io) {
        final int size = 65536;
        long memory = io.allocateMemory(size * 8, true);
        long[] array = new long[size];
        for (int i = 0; i < size; i++) array[i] = i | ((long) i << 32);
        io.putLongArray(memory, array, 0, array.length);
        for (int i = 0; i < size; i++) {
            assertEquals(array[i], MemoryIO.getInstance().getLong(memory + (i * 8)));
        }
    }

    @Test public void testPutLongArray() {
        testPutLongArray(MemoryIO.getCheckedInstance());
    }

    @Test public void testPutLongArrayChecked() {
        testPutLongArray(MemoryIO.getCheckedInstance());
    }

//    @Test public void testCheckedPut() {
//        MemoryIO.getCheckedInstance().putLong(0xdeadbeefL, 0xcafebabeL);
//    }
}
