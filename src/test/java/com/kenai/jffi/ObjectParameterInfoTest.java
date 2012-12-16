package com.kenai.jffi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 *
 */
public class ObjectParameterInfoTest {
    static int[] possibleFlags = {
            ObjectParameterInfo.IN,
            ObjectParameterInfo.OUT,
            ObjectParameterInfo.NULTERMINATE,
            ObjectParameterInfo.CLEAR,
    };

    @Test public void testCache() {
        for (int i = 0; i < 256; i++) {
            for (ObjectParameterInfo.ObjectType objectType : EnumSet.allOf(ObjectParameterInfo.ObjectType.class)) {
                for (ObjectParameterInfo.ComponentType componentType : EnumSet.allOf(ObjectParameterInfo.ComponentType.class)) {
                    int ioflags = 0;
                    for (int f : possibleFlags) {
                        ioflags |= f;
                        ObjectParameterInfo info1 = ObjectParameterInfo.create(i, objectType, componentType, ioflags);
                        ObjectParameterInfo info2 = ObjectParameterInfo.create(i, objectType, componentType, ioflags);
                        assertEquals(i, info1.getParameterIndex());
                        assertEquals(i, info2.getParameterIndex());
                        assertEquals(ioflags, info1.ioflags());
                        assertEquals(ioflags, info2.ioflags());
                        assertSame(info1, info2);
                    }
                }
            }
        }
    }
}
