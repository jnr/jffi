package com.kenai.jffi.internal;

import com.kenai.jffi.internal.StubLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class StubLoaderTest {
    @Test
    public void testExtractName() throws Throwable {
        File path = StubLoader.calculateExtractPath(new File("foo"), "bar");

        Assert.assertEquals("foo", path.getParent());
        Assert.assertEquals("bar", path.getName());
    }
}
