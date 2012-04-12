/*
 * Copyright (C) 2011 Wayne Meissner
 *
 * This file is part of jffi.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenai.jffi.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Properties;

/**
 * Loads the native stub library.  This is intended to only ever be called
 * reflectively, so it cannot access other jffi classes.
 */
public class StubLoader {
    public final static int VERSION_MAJOR = getVersionField("MAJOR");
    public final static int VERSION_MINOR = getVersionField("MINOR");
    private static final String versionClassName = "com.kenai.jffi.Version";
    
    private static final String bootPropertyFilename = "boot.properties";
    private static final String bootLibraryPropertyName = "jffi.boot.library.path";
    private static final String stubLibraryName
            = String.format("jffi-%d.%d", VERSION_MAJOR, VERSION_MINOR);

    private static final OS OS_ = determineOS();
    private static final CPU CPU_ = determineCPU();
    private static volatile Throwable failureCause = null;
    private static volatile boolean loaded = false;
    
    
    public static final boolean isLoaded() {
        return loaded;
    }
    
    public static final Throwable getFailureCause() {
        return failureCause;
    }
    

    public enum OS {
        /** MacOSX */
        DARWIN,
        /** FreeBSD */
        FREEBSD,
        /** NetBSD */
        NETBSD,
        /** OpenBSD */
        OPENBSD,
        /** Linux */
        LINUX,
        /** Solaris (and OpenSolaris) */
        SOLARIS,
        /** The evil borg operating system */
        WINDOWS,
        /** IBM AIX */
        AIX,
        /** IBM zOS **/
        ZLINUX,

        /** No idea what the operating system is */
        UNKNOWN;

        @Override
        public String toString() { return name().toLowerCase(); }
    }

    /**
     * The common names of cpu architectures.
     *
     * <b>Note</b> The names of the enum values are used in other parts of the
     * code to determine where to find the native stub library.  Do not rename.
     */
    public enum CPU {
        /** Intel ia32 */
        I386,
        /** AMD 64 bit (aka EM64T/X64) */
        X86_64,
        /** Power PC 32 bit */
        PPC,
        /** Power PC 64 bit */
        PPC64,
        /** Sun sparc 32 bit */
        SPARC,
        /** Sun sparc 64 bit */
        SPARCV9,
        /** IBM zSeries S/390 64 bit */
        S390X,
        /** ARM */
        ARM,
        /** Unknown CPU */
        UNKNOWN;

        @Override
        public String toString() { return name().toLowerCase(); }
    }

    /**
     * Determines the operating system jffi is running on
     *
     * @return An member of the <tt>OS</tt> enum.
     */
    private static final OS determineOS() {
        String osName = System.getProperty("os.name").split(" ")[0].toLowerCase();
        if (osName.startsWith("mac") || osName.startsWith("darwin")) {
            return OS.DARWIN;
        } else if (osName.startsWith("linux")) {
            return OS.LINUX;
        } else if (osName.startsWith("sunos") || osName.startsWith("solaris")) {
            return OS.SOLARIS;
        } else if (osName.startsWith("aix")) {
            return OS.AIX; 
        } else if (osName.startsWith("openbsd")) {
            return OS.OPENBSD;
        } else if (osName.startsWith("freebsd")) {
            return OS.FREEBSD;
        } else if (osName.startsWith("windows")) {
            return OS.WINDOWS;
        } else {
            return OS.UNKNOWN;
        }
    }
    
    /**
     * Determines the CPU architecture the JVM is running on.
     *
     * This normalizes all the variations that are equivalent (e.g. i386, x86, i86pc)
     * into a common cpu type.
     *
     * @return A member of the <tt>CPU</tt> enum.
     */
    private static final CPU determineCPU() {
        String archString = System.getProperty("os.arch", "unknown").toLowerCase();
        if ("x86".equals(archString) || "i386".equals(archString) || "i86pc".equals(archString)) {
            return CPU.I386;
        } else if ("x86_64".equals(archString) || "amd64".equals(archString)) {
            return CPU.X86_64;
        } else if ("ppc".equals(archString) || "powerpc".equals(archString)) {
            return CPU.PPC;
        } else if ("powerpc64".equals(archString)) {
            return CPU.PPC64;
        }
        
        // Try to find by lookup up in the CPU list
        try {
            return CPU.valueOf(archString.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return CPU.UNKNOWN;
        }
    }
    
    public static CPU getCPU() {
        return CPU_;
    }
    
    public static OS getOS() {
        return OS_;
    }

    /**
     * Gets the name of the stub library.
     *
     * @return The name of the stub library as a <tt>String</tt>
     */
    private static String getStubLibraryName() {
        return stubLibraryName;
    }

    /**
     * Gets the name of this <tt>Platform</tt>.
     *
     * @return The name of this platform.
     */
    public static String getPlatformName() {
        if (getOS().equals(OS.DARWIN)) {
            return "Darwin";
        }

       
        String osName = System.getProperty("os.name").split(" ")[0];
        return getCPU().name().toLowerCase() + "-" + osName;
    }
    /**
     * Gets the path within the jar file of the stub native library.
     *
     * @return The path of the jar file.
     */
    private static String getStubLibraryPath() {
        return "jni/" + getPlatformName() + "/"+ System.mapLibraryName(stubLibraryName);
    }
    
    public StubLoader() {}
    
    /**
     * Loads the stub library
     */
    static void load() {
        final String libName = getStubLibraryName();
        String bootPath = getBootPath();
        if (bootPath != null && loadFromBootPath(libName, bootPath)) {
            return;
        }

        String libraryPath = System.getProperty("java.library.path");
        if (libraryPath != null && loadFromBootPath(libName, libraryPath)) {
            return;
        }

        loadFromJar();
    }

    private static String getBootPath() {
        
        String bootPath = System.getProperty(bootLibraryPropertyName);
        if (bootPath != null) {
            return bootPath;
        }

        InputStream is = getResourceAsStream(bootPropertyFilename);
        if (is != null) {
            Properties p = new Properties();
            try {
                p.load(is);
                return p.getProperty(bootLibraryPropertyName);
            } catch (IOException ex) { 
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return null;
    }

    private static String getAlternateLibraryPath(String path) {
        if (path.endsWith("dylib")) {
            return path.substring(0, path.lastIndexOf("dylib")) + "jnilib";
        } else {
            return path.substring(0, path.lastIndexOf("jnilib")) + "dylib";
        }
    }

    private static boolean loadFromBootPath(String libName, String bootPath) {
        String[] dirs = bootPath.split(File.pathSeparator);
        for (int i = 0; i < dirs.length; ++i) {
            String path = new File(new File(dirs[i]), System.mapLibraryName(libName)).getAbsolutePath();
            try {
                System.load(path);
                return true;
            } catch (UnsatisfiedLinkError ex) {
            }
            if (getOS() == OS.DARWIN) {
                try {
                    System.load(getAlternateLibraryPath(path));
                    return true;
                } catch (UnsatisfiedLinkError ex) {}
            }
        }
        return false;
    }

    private static void loadFromJar() {
        InputStream is = getStubLibraryStream();
        File dstFile = null;
        FileOutputStream os = null;

        try {
            dstFile = File.createTempFile("jffi", null);
            dstFile.deleteOnExit();
            os = new FileOutputStream(dstFile);
            ReadableByteChannel srcChannel = Channels.newChannel(is);

            for (long pos = 0; is.available() > 0; ) {
                pos += os.getChannel().transferFrom(srcChannel, pos, Math.max(4096, is.available()));
            }

        } catch (IOException ex) {
            throw new UnsatisfiedLinkError(ex.getMessage());

        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                is.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        System.load(dstFile.getAbsolutePath());
    }
    
    /**
     * Gets an <tt>InputStream</tt> representing the stub library image stored in
     * the jar file.
     *
     * @return A new <tt>InputStream</tt>
     */
    private static InputStream getStubLibraryStream() {
        String stubPath = getStubLibraryPath();
        String[] paths = { stubPath, "/" + stubPath };

        for (String path : paths) {
            InputStream is = getResourceAsStream(path);

            // On MacOS, the stub might be named .dylib or .jnilib - cater for both
            if (is == null && getOS() == OS.DARWIN) {
                is = getResourceAsStream(getAlternateLibraryPath(path));
            }
            if (is != null) {
                return is;
            }
        }

        throw new UnsatisfiedLinkError("could not locate stub library"
                + " in jar file.  Tried " + Arrays.deepToString(paths));
    }

    private static InputStream getResourceAsStream(String resourceName) {
        // try both our classloader and context classloader
        ClassLoader[] cls = new ClassLoader[] {
            ClassLoader.getSystemClassLoader(),
            StubLoader.class.getClassLoader(),
            Thread.currentThread().getContextClassLoader()
        };
        
        for (ClassLoader cl : cls) {
            // skip null classloader (e.g. boot or null context loader)
            if (cl == null) { 
                continue;
            }

            InputStream is;
            if ((is = cl.getResourceAsStream(resourceName)) != null) {
                return is;
            }
        }
        
        return null;
    }
    
    private static int getVersionField(String name) {
        try {
        Class c = Class.forName(versionClassName);
            return (Integer) c.getField(name).get(c);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static {
        try {
            load();
            loaded = true;
        } catch (Throwable t) {
            failureCause = t;
        }
    }
}
