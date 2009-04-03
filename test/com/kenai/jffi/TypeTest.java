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
public class TypeTest {

    public TypeTest() {
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
    @Test public void lookupBuiltinType() throws Throwable {
        Foreign foreign = Foreign.getInstance();
        long handle = foreign.lookupBuiltinType(Foreign.TYPE_SINT8);
        assertEquals("Incorrect type", Type.SINT8.type(), foreign.getTypeType(handle));
        assertEquals("Incorrect size", 1, foreign.getTypeSize(handle));
        assertEquals("Incorrect alignment", 1, foreign.getTypeAlign(handle));
    }
}