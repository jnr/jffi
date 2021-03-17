package com.kenai.jffi.internal;

import com.kenai.jffi.internal.StubLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class StubLoaderTest {
    @Test
    public void testExtractName() throws Throwable {
        String barName = "bar";
        String barFile = "bar." + StubLoader.dlExtension();

        File path = StubLoader.calculateExtractPath(new File("foo"), barName);

        Assert.assertEquals("foo", path.getParent());
        Assert.assertEquals(barFile, path.getName());

        path = StubLoader.calculateExtractPath(new File("foo"), barFile);

        Assert.assertEquals("foo", path.getParent());
        Assert.assertEquals(barFile, path.getName());
    }

    @Test
    public void testDefaultExtractName() throws Throwable {
        String defaultFile = "jffi-" + StubLoader.VERSION_MAJOR + "." + StubLoader.VERSION_MINOR + "." + StubLoader.dlExtension();

        File path = StubLoader.calculateExtractPath(new File("foo"), "");

        Assert.assertEquals("foo", path.getParent());
        Assert.assertEquals(
                defaultFile,
                path.getName());
    }
}
