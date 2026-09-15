package com.kenai.jffi.internal;

import com.kenai.jffi.Platform;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static com.kenai.jffi.Platform.OS.DARWIN;

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

    @Test
    public void testBootPathDarwin() throws Throwable {
        if (Platform.getPlatform().getOS() != DARWIN) return;

        String library = "jffi-1234";
        String bootPath = "/foo:/bar";
        Logger logger = StubLoader.LOGGER;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamHandler handler = new StreamHandler(out, new Formatter() {
            @Override
            public String format(LogRecord record) {
                return record.getMessage() + "\n";
            }
        });
        logger.addHandler(handler);
        logger.setLevel(Level.FINEST);
        handler.setLevel(Level.FINEST);

        StubLoader.loadFromBootPath(library, bootPath, new ArrayList<>());

        handler.flush();

        String output = new String(out.toByteArray());

        Assert.assertEquals(DARWIN_EXPECTED, output);
    }

    private static final String DARWIN_EXPECTED = "Attempting to load library \"jffi-1234\" from boot path \"/foo:/bar\"\n" +
            "/foo/Darwin/libjffi-1234.dylib not found\n" +
            "/foo/Darwin/libjffi-1234.jnilib not found\n" +
            "/foo/libjffi-1234.dylib not found\n" +
            "/foo/libjffi-1234.jnilib not found\n" +
            "/bar/Darwin/libjffi-1234.dylib not found\n" +
            "/bar/Darwin/libjffi-1234.jnilib not found\n" +
            "/bar/libjffi-1234.dylib not found\n" +
            "/bar/libjffi-1234.jnilib not found\n" +
            "no loadable files found\n";
}
