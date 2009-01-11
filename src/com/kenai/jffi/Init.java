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

final class Init {
    static final void init() {}
    static {
        load();
    }
    private static final void load() {
        final String libName = Platform.getPlatform().getStubLibraryName();
        String bootPath = System.getProperty("jffi.boot.library.path");
        if (bootPath != null) {
            String[] dirs = bootPath.split(File.pathSeparator);
            for (int i = 0; i < dirs.length; ++i) {
                String path = new File(new File(dirs[i]), System.mapLibraryName(libName)).getAbsolutePath();
                try {
                    System.load(path);
                    return;
                } catch (UnsatisfiedLinkError ex) {}
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
                        return;
                    } catch (UnsatisfiedLinkError ex) {}
                }
            }
        }
        try {
            System.loadLibrary(libName);
            return;
        } catch (UnsatisfiedLinkError ex) {}
        InputStream is = Platform.getPlatform().getStubLibraryStream();
        if (is == null) {
            throw new UnsatisfiedLinkError("Could not locate " + libName + " in jar file");
        }

        File dstFile = null;
        try {
            dstFile = File.createTempFile(libName, null);
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
}
