
package com.kenai.jffi;

import com.kenai.jffi.UnitHelper.InvokerType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class NumberTest {
    private static interface LibNumberTest {
        byte ret_s8(byte v);
        byte ret_u8(byte v);
        short ret_s16(short v);
        short ret_u16(short v);
        int ret_s32(int v);
        int ret_u32(int v);
        long ret_s64(long v);
        long ret_u64(long v);
        float ret_float(float v);
        double ret_double(double v);
        BigDecimal ret_f128(BigDecimal v);
    }

    private static interface LibM {
        float powf(float x, float y);
        float cosf(float x);
    }
    
    public static final String FLT64_MAX =  "1.79769313486231570814527423731704357e+308";
    public static final String DBL_MAX =    "1.79769313486231570814527423731704357e+308";
    public static final String FLT128_MAX = "1.18973149535723176508575932662800702e+4932";
    public static final String LDBL_MAX =   "1.18973149535723176502126385303097021e+4932";

    public static final String FLT64_MIN =  "2.22507385850720138309023271733240406e-308";
    public static final String DBL_MIN =    "2.22507385850720138309023271733240406e-308";
    public static final String FLT128_MIN = "3.36210314311209350626267781732175260e-4932";
    public static final String LDBL_MIN =   "3.36210314311209350626267781732175260e-4932";

    public static final String FLT64_EPSILON =  "2.22044604925031308084726333618164062e-16";
    public static final String DBL_EPSILON =    "2.22044604925031308084726333618164062e-16";
    public static final String FLT128_EPSILON = "1.92592994438723585305597794258492732e-34";
    public static final String LDBL_EPSILON =   "1.08420217248550443400745280086994171e-19";

    public static final String FLT64_DENORM_MIN =  "4.94065645841246544176568792868221372e-324";
    public static final String DBL_DENORM_MIN =    "4.94065645841246544176568792868221372e-324";
    public static final String FLT128_DENORM_MIN = "6.47517511943802511092443895822764655e-4966";
    public static final String LDBL_DENORM_MIN =   "3.64519953188247460252840593361941982e-4951";

    public static final String FLT64_ONE_PLUS_EPSILON =  "1.000000000000000222044604925031308084726333618164062";
    public static final String DBL_ONE_PLUS_EPSILON =    "1.000000000000000222044604925031308084726333618164062";
    public static final String FLT128_ONE_PLUS_EPSILON = "1.000000000000000000000000000000000192592994438723585305597794258492732";
    public static final String LDBL_ONE_PLUS_EPSILON =   "1.000000000000000000108420217248550443400745280086994171";

    public static final String FLT64_ONE_MINUS_EPSILON =  "0.999999999999999777955395074968691915273666381835938";
    public static final String DBL_ONE_MINUS_EPSILON =    "0.999999999999999777955395074968691915273666381835938";
    public static final String FLT128_ONE_MINUS_EPSILON = "0.999999999999999999999999999999999807407005561276414694402205741507268";
    public static final String LDBL_ONE_MINUS_EPSILON =   "0.999999999999999999891579782751449556599254719913005829";

    public NumberTest() {
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

    private static final byte[] s8_values = { 0, Byte.MAX_VALUE, Byte.MIN_VALUE, -1 };
    private static final byte[] u8_values = { 0, Byte.MAX_VALUE, (byte) 0x80, (byte) 0xff };
    private static final short[] s16_values = { 0, Short.MAX_VALUE, Short.MIN_VALUE, -1 };
    private static final short[] u16_values = { 0, Short.MAX_VALUE, (short) 0x8000, (short) 0xffff };
    private static final int[] s32_values = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1 };
    private static final int[] u32_values = { 0, Integer.MAX_VALUE, 0x80000000, 0xffffffff };
    private static final long[] s64_values = { 0, Long.MAX_VALUE, Long.MIN_VALUE, -1 };
    private static final long[] u64_values = { 0, Long.MAX_VALUE, 0x8000000000000000L, 0xffffffffffffffffL };

    @Test public void returnS8() {
        returnS8(InvokerType.Default);
    }
    @Test public void returnU8() {
        returnU8(InvokerType.Default);
    }
    @Test public void returnFastIntS8() {
        returnS8(InvokerType.FastInt);
    }
    @Test public void returnFastIntU8() {
        returnU8(InvokerType.FastInt);
    }
    @Test public void returnFastLongS8() {
        returnS8(InvokerType.FastLong);
    }
    @Test public void returnFastLongU8() {
        returnU8(InvokerType.FastLong);
    }
    @Test public void returnFastNumericS8() {
        returnS8(InvokerType.FastNumeric);
    }
    @Test public void returnFastNumericU8() {
        returnU8(InvokerType.FastNumeric);
    }
    @Test public void returnPointerArrayS8() {
        returnS8(InvokerType.PointerArray);
    }
    @Test public void returnPointerArrayU8() {
        returnU8(InvokerType.PointerArray);
    }
    private void returnS8(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        for (int i = 0; i < s8_values.length; ++i) {
            assertEquals("Value not returned correctly", s8_values[i], lib.ret_s8(s8_values[i]));
        }
    }
    
    private void returnU8(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        for (int i = 0; i < u8_values.length; ++i) {
            assertEquals("Value not returned correctly", u8_values[i], lib.ret_u8(u8_values[i]));
        }
    }
    @Test public void returnS16() {
        returnS16(InvokerType.Default);
    }
    @Test public void returnFastIntS16() {
        returnS16(InvokerType.FastInt);
    }
    @Test public void returnFastLongS16() {
        returnS16(InvokerType.FastLong);
    }
    @Test public void returnFastNumericS16() {
        returnS16(InvokerType.FastNumeric);
    }
    @Test public void returnPointerArrayS16() {
        returnS16(InvokerType.PointerArray);
    }
    private void returnS16(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        for (int i = 0; i < s16_values.length; ++i) {
            assertEquals("Value not returned correctly", s16_values[i], lib.ret_s16(s16_values[i]));
        }
    }
    @Test public void returnU16() {
        returnU16(InvokerType.Default);
    }
    @Test public void returnFastIntU16() {
        returnU16(InvokerType.FastInt);
    }
    @Test public void returnFastLongU16() {
        returnU16(InvokerType.FastLong);
    }
    @Test public void returnFastNumericU16() {
        returnU16(InvokerType.FastNumeric);
    }
    @Test public void returnPointerArrayU16() {
        returnU16(InvokerType.PointerArray);
    }
    private void returnU16(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        for (int i = 0; i < u16_values.length; ++i) {
            assertEquals("Value not returned correctly", u16_values[i], lib.ret_u16(u16_values[i]));
        }
    }
    @Test public void returnS32() {
        returnS32(InvokerType.Default);
    }
    @Test public void returnFastintS32() {
        returnS32(InvokerType.FastInt);
    }
    @Test public void returnFastLongS32() {
        returnS32(InvokerType.FastLong);
    }
    @Test public void returnFastNumericS32() {
        returnS32(InvokerType.FastNumeric);
    }
    @Test public void returnPointerArrayS32() {
        returnS32(InvokerType.PointerArray);
    }
    private void returnS32(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        for (int i = 0; i < s32_values.length; ++i) {
            assertEquals("Value not returned correctly", s32_values[i], lib.ret_s32(s32_values[i]));
        }
    }
    @Test public void returnU32() {
        returnU32(InvokerType.Default);
    }
    @Test public void returnFastintU32() {
        returnU32(InvokerType.FastInt);
    }
    @Test public void returnFastLongU32() {
        returnU32(InvokerType.FastLong);
    }
    @Test public void returnFastNumericU32() {
        returnU32(InvokerType.FastNumeric);
    }
    @Test public void returnPointerArrayU32() {
        returnU32(InvokerType.PointerArray);
    }
    private void returnU32(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        for (int i = 0; i < u32_values.length; ++i) {
            assertEquals("Value not returned correctly", u32_values[i], lib.ret_u32(u32_values[i]));
        }
    }
    @Test public void returnS64() {
        returnS64(InvokerType.Default);
    }
    @Test public void returnFastLongS64() {
        returnS64(InvokerType.FastLong);
    }
    @Test public void returnFastNumericS64() {
        returnS64(InvokerType.FastNumeric);
    }
    @Test public void returnPointerArrayS64() {
        returnS64(InvokerType.PointerArray);
    }
    private void returnS64(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        for (int i = 0; i < s64_values.length; ++i) {
            assertEquals("Value not returned correctly", s64_values[i], lib.ret_s64(s64_values[i]));
        }
    }
    @Test public void returnU64() {
        returnU64(InvokerType.Default);
    }
    @Test public void returnFastLongU64() {
        returnU64(InvokerType.FastLong);
    }
    @Test public void returnFastNumericU64() {
        returnU64(InvokerType.FastNumeric);
    }
    @Test public void returnPointerArrayU64() {
        returnU64(InvokerType.PointerArray);
    }
    private void returnU64(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        for (int i = 0; i < u64_values.length; ++i) {
            assertEquals("Value not returned correctly", u64_values[i], lib.ret_u64(u64_values[i]));
        }
    }

    @Test public void returnDefaultF32() {
        returnF32(InvokerType.Default);
    }
    @Test public void returnPointerArrayF32() {
        returnF32(InvokerType.PointerArray);
    }

    private void returnF32(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        float[] values = { 0f, 1.0f, -2.0f };
        for (int i = 0; i < values.length; ++i) {
            assertEquals("Value not returned correctly", values[i], lib.ret_float(values[i]), 0.1f);
        }
    }

    @Test public void returnDefaultF64() {
        returnF64(InvokerType.Default);
    }

    private void returnF64(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        double[] values = { 0d, 1.0d, -2.0d };
        for (int i = 0; i < values.length; ++i) {
            assertEquals("Value not returned correctly", values[i], lib.ret_double(values[i]), 0.1f);
        }
    }

    @Test public void returnDefaultF128() {
        returnF128(InvokerType.Default);
    }
private static double calculateMachineEpsilonDouble() {
        double machEps = 1.0f;

        do
           machEps /= 2.0f;
        while ((double) (1.0 + (machEps / 2.0)) != 1.0);

        return machEps;
    }

    @Test public void returnDefaultF128HighPrecision() {
        assertEquals(1.0, 1.0 + new Double(FLT64_EPSILON), new Double(FLT64_EPSILON));
        
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, InvokerType.Default);
        BigDecimal delta;
        switch (Type.LONGDOUBLE.size()) {
            case 8:
                delta = new BigDecimal(DBL_EPSILON);

                returnF128HighPrecision(lib, DBL_MAX, delta.multiply(new BigDecimal(DBL_MAX)));
                returnF128HighPrecision(lib, DBL_MIN, delta);
                returnF128HighPrecision(lib, DBL_EPSILON, delta);
                returnF128HighPrecision(lib, DBL_DENORM_MIN, delta);
                returnF128HighPrecision(lib, DBL_ONE_PLUS_EPSILON, delta);
                returnF128HighPrecision(lib, DBL_ONE_MINUS_EPSILON, delta);
            break;
            case 12:
            case 16:
                //gcc uses 80bits
                delta = new BigDecimal(LDBL_EPSILON);
                
                returnF128HighPrecision(lib, LDBL_MAX, delta.multiply(new BigDecimal(LDBL_MAX)));
                returnF128HighPrecision(lib, LDBL_MIN, delta);
                returnF128HighPrecision(lib, LDBL_EPSILON, delta);
                returnF128HighPrecision(lib, LDBL_DENORM_MIN, delta);
                returnF128HighPrecision(lib, LDBL_ONE_PLUS_EPSILON, delta);
                returnF128HighPrecision(lib, LDBL_ONE_MINUS_EPSILON, delta);
                break;
            default : throw new RuntimeException("Unknown  size of long double: " + Type.LONGDOUBLE.size());
        }
    }
//        BigDecimal param = new BigDecimal("1.234567890123456789");

    private void returnF128HighPrecision(LibNumberTest lib, String valueStr, BigDecimal maxDelta) {
        BigDecimal value = new BigDecimal(valueStr);
        BigDecimal result = lib.ret_f128(value);
        BigDecimal delta = value.subtract(result).abs();

        int comp = maxDelta.compareTo(delta);
        assertEquals("Not equals, \n\texpected: " + value.toString() + "\n\tbut was: " + result.toString() +"\n\t delta: " + delta.toEngineeringString(), 1, comp);
    }


    private void returnF128(InvokerType type) {
        LibNumberTest lib = UnitHelper.loadTestLibrary(LibNumberTest.class, type);
        double[] values = { 0d, 1.0d, -2.0d };
        for (double v : values) {
            BigDecimal param = BigDecimal.valueOf(v);
            BigDecimal result = lib.ret_f128(param);
            BigDecimal delta = param.subtract(result).abs();
            assertTrue(delta.compareTo(new BigDecimal("0.1")) < 0);
        }
    }

}
