
package com.kenai.jffi;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 * Test internals of the Foreign class
 */
public class ForeignTest {

    public ForeignTest() {
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
    @Test public void version() {
        final int VERSION = Foreign.VERSION_MAJOR << 16 | Foreign.VERSION_MINOR << 8 | Foreign.VERSION_MICRO;
        int version = Foreign.getInstance().getVersion();
        assertEquals("Bad version", VERSION, version);
    }

    @Test public void pageSize() {
        long pageSize = Foreign.getInstance().pageSize();
        assertNotSame("Invalid page size", 0, pageSize);
    }
    @Test public void mmap() {
        if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
            final int SIZE = 123;
            long addr = Foreign.getInstance().mmap(0, SIZE, Foreign.PROT_READ | Foreign.PROT_WRITE,
                Foreign.MAP_PRIVATE | Foreign.MAP_ANON, -1, 0);
            assertNotSame("Failed to allocate memory", -1L, addr);
        }
    }

    @Test public void munmap() {
        if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
            final int SIZE = 123;
            long addr = Foreign.getInstance().mmap(0, SIZE, Foreign.PROT_READ | Foreign.PROT_WRITE,
                    Foreign.MAP_PRIVATE | Foreign.MAP_ANON, -1, 0);
            assertNotSame("Failed to allocate memory", -1, addr);
            assertTrue("Failed to free memory", Foreign.getInstance().munmap(addr, SIZE) == 0);
        }
    }

    @Test public void writeToAllocatedMemory() {
        if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
            final int SIZE = 257;
            final byte[] MAGIC = {'t', 'e', 's', 't'};
            long addr = Foreign.getInstance().mmap(0, SIZE, Foreign.PROT_READ | Foreign.PROT_WRITE,
                    Foreign.MAP_PRIVATE | Foreign.MAP_ANON, -1, 0);
            assertNotSame("Failed to allocate memory", -1, addr);
            MemoryIO.getInstance().putByteArray(addr, MAGIC, 0, MAGIC.length);
            byte[] tmp = new byte[MAGIC.length];
            MemoryIO.getInstance().getByteArray(addr, tmp, 0, MAGIC.length);
            assertArrayEquals("Incorrect data read back", MAGIC, tmp);
            assertTrue("Failed to free memory", Foreign.getInstance().munmap(addr, SIZE) == 0);
        }
    }
    
    @Test(expected = RuntimeException.class)
    public void longDoubleFromStringWrongArraySize() {
         byte[] expectedLd = new byte[] {(byte)0x8d,(byte)0xdb,(byte)0xcf,(byte)0x62,(byte)0x14,(byte)0x52,(byte)0x06,(byte)0x9e,(byte)0xff,(byte)0x3f,(byte)0x00,(byte)0x10,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
        String strRetValue = Foreign.getInstance().longDoubleToString(expectedLd, 0, 2);
    }
    
    @Test public void longDoubleFromString() {
        DecimalFormat nf = new DecimalFormat();
        String strValue = "1.234567890123456789";
        BigDecimal value = new BigDecimal(strValue);
        nf.setMaximumFractionDigits(35);
        byte[] ld = new byte[Type.LONGDOUBLE.size()];
        Foreign.getInstance().longDoubleFromString(nf.format(value), ld, 0, Type.LONGDOUBLE.size());
        nf.setParseBigDecimal(true);
        String strRetValue = Foreign.getInstance().longDoubleToString(ld, 0, Type.LONGDOUBLE.size());
        BigDecimal retValue;
        try {
            retValue = (BigDecimal)nf.parse(strRetValue);
        } catch (ParseException ex) {
            Logger.getLogger(ForeignTest.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        BigDecimal delta = value.subtract(retValue).abs();
        assertTrue("Not equals, expected: " + value.toEngineeringString() + " but was: " + retValue.toEngineeringString(), delta.compareTo(new BigDecimal("0.0000000000000000001")) < 0);
    }
    static class ClosureProxy {
        void invoke(Closure.Buffer buf) {}
    }
//    @Test public void freeClosure() throws Throwable {
//        Method m = ClosureProxy.class.getDeclaredMethod("invoke", new Class[] { Closure.Buffer.class});
//        long cl = Foreign.getInstance().newClosure(new ClosureProxy(), m, Type.VOID.handle, new long[0], 0);
//        Foreign.getInstance().freeClosure(cl);
//    }

//    @Test public void newNativeMethod() throws Throwable {
//        Foreign.getInstance().newNativeMethod("test", "()V", 0);
//    }
}