
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
}