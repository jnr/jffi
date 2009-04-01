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
}