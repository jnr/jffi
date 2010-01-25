/*
 * Copyright (C) 2007, 2008 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.kenai.jffi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

/**
 * Utility class to load the jffi stub library
 */
final class Init {
    private static final String bootPropertyFilename = "boot.properties";
    private static final String bootLibraryPropertyName = "jffi.boot.library.path";
    private static final String stubLibraryName
            = String.format("jffi-%d.%d", Foreign.VERSION_MAJOR, Foreign.VERSION_MINOR);

    private static volatile boolean loaded = false;

    // prevent instantiation
    private Init() {}
    
    /**
     * Loads the stub library
     */
    static final void load() {
        if (loaded) {
            return;
        }

        final String libName = getStubLibraryName();
        String bootPath = getBootPath();
        if (bootPath != null && loadFromBootPath(libName, bootPath)) {
            return;
        }
        
        try {
            System.loadLibrary(libName);
            return;
        } catch (UnsatisfiedLinkError ex) {}

        loadFromJar();
    }

    private static final String getBootPath() {
        
        String bootPath = System.getProperty(bootLibraryPropertyName);
        if (bootPath != null) {
            return bootPath;
        }

        InputStream is = Init.class.getResourceAsStream(bootPropertyFilename);
        if (is != null) {
            Properties p = new Properties();
            try {
                p.load(is);
                return p.getProperty(bootLibraryPropertyName);
            } catch (IOException ex) { }
        }

        return null;
    }

    private static final boolean loadFromBootPath(String libName, String bootPath) {
        String[] dirs = bootPath.split(File.pathSeparator);
        for (int i = 0; i < dirs.length; ++i) {
            String path = new File(new File(dirs[i]), System.mapLibraryName(libName)).getAbsolutePath();
            try {
                System.load(path);
                return true;
            } catch (UnsatisfiedLinkError ex) {
            }
            if (Platform.getPlatform().getOS() == Platform.OS.DARWIN) {
                String orig, ext;
                if (path.endsWith("dylib")) {
                    orig = "dylib";
                    ext = "jnilib";
                } else {
                    orig = "jnilib";
                    ext = "dylib";
                }
                try {
                    System.load(path.substring(0, path.lastIndexOf(orig)) + ext);
                    return true;
                } catch (UnsatisfiedLinkError ex) {
                }
            }
        }
        return false;
    }

    private static final void loadFromJar() {
        InputStream is = getStubLibraryStream();
        File dstFile = null;

        try {
            dstFile = File.createTempFile("jffi", null);
            dstFile.deleteOnExit();
            FileChannel dstChannel = new FileOutputStream(dstFile).getChannel();
            ReadableByteChannel srcChannel = Channels.newChannel(is);
            for (long pos = 0; is.available() > 0; ) {
                pos += dstChannel.transferFrom(srcChannel, pos, Math.max(4096, is.available()));
            }
            dstChannel.close();
            System.load(dstFile.getAbsolutePath());
        } catch (IOException ex) {
            throw new UnsatisfiedLinkError(ex.getMessage());
        } finally {
        }
    }

    /**
     * Gets an <tt>InputStream</tt> representing the stub library image stored in
     * the jar file.
     *
     * @return A new <tt>InputStream</tt>
     */
    private static final InputStream getStubLibraryStream() {
        String path = getStubLibraryPath();

        InputStream is = Init.class.getResourceAsStream(path);

        // On MacOS, the stub might be named .dylib or .jnilib - cater for both
        if (is == null && Platform.getPlatform().getOS() == Platform.OS.DARWIN) {
            is = Init.class.getResourceAsStream(path.replaceAll("dylib", "jnilib"));
        }
        if (is == null) {
            throw new UnsatisfiedLinkError("Could not locate stub library ("
                    + path + ") in jar file");
        }

        return is;
    }

    /**
     * Gets the name of the stub library.
     *
     * @return The name of the stub library as a <tt>String</tt>
     */
    private static final String getStubLibraryName() {
        return stubLibraryName;
    }

    /**
     * Gets the path within the jar file of the stub native library.
     *
     * @return The path of the jar file.
     */
    private static final String getStubLibraryPath() {
        return "/jni/" + Platform.getPlatform().getName() + "/"+ System.mapLibraryName(stubLibraryName);
    }

}
