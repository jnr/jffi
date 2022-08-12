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

import com.kenai.jffi.Platform;
import com.kenai.jffi.Util;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.kenai.jffi.Util.equalsIgnoreCase;

/**
 * Loads the native stub library.  This is intended to only ever be called
 * reflectively, so it cannot access other jffi classes.
 */
public class StubLoader {
    public final static int VERSION_MAJOR = getVersionField("MAJOR");
    public final static int VERSION_MINOR = getVersionField("MINOR");
    private static final String versionClassName = "com.kenai.jffi.Version";
    private static final java.util.Locale LOCALE = java.util.Locale.ENGLISH;
    
    private static final String bootPropertyFilename = "boot.properties";
    private static final String bootLibraryPropertyName = "jffi.boot.library.path";
    private static final String stubLibraryName
            = String.format("jffi-%d.%d", VERSION_MAJOR, VERSION_MINOR);

    private static final String TMPDIR_ENV =
            Platform.getPlatform().getOS() == Platform.OS.WINDOWS ?
                    "TEMP" : "TMPDIR";
    private static final String TMPDIR = System.getProperty("java.io.tmpdir");
    private static final String TMPDIR_RECOMMENDATION =
            "Set `" + TMPDIR_ENV + "` or Java property `java.io.tmpdir` to a read/write path that is not mounted \"noexec\".";
    public static final String TMPDIR_WRITE_ERROR = "Unable to write jffi binary stub to `" + TMPDIR + "`.";
    public static final String TMPDIR_EXEC_ERROR = "Unable to execute or load jffi binary stub from `" + TMPDIR + "`.";

    private static volatile OS os = null;
    private static volatile CPU cpu = null;

    private static volatile Throwable failureCause = null;
    private static volatile boolean loaded = false;
    private static final File jffiExtractDir;
    private static final String jffiExtractName;

    private static final String JFFI_EXTRACT_DIR = "jffi.extract.dir";
    private static final String JFFI_EXTRACT_NAME = "jffi.extract.name";

    static {
        String extractDir = System.getProperty(JFFI_EXTRACT_DIR);
        if (extractDir != null) {
            jffiExtractDir = new File(extractDir);
        } else {
            jffiExtractDir = null;
        }

        String extractName = System.getProperty(JFFI_EXTRACT_NAME);
        if (extractName != null) {
            jffiExtractName = extractName;
        } else {
            jffiExtractName = null;
        }
    }

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
	/** DragonFly */
	DRAGONFLY,
        /** Linux */
        LINUX,
        /** Solaris (and OpenSolaris) */
        SOLARIS,
        /** The evil borg operating system */
        WINDOWS,
        /** IBM AIX */
        AIX,
        /** IBM i */
        IBMI,
        /** IBM zOS **/
        ZLINUX,

        /** No idea what the operating system is */
        UNKNOWN;

        @Override
        public String toString() { return name().toLowerCase(LOCALE); }
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
        /** Power PC 64 bit little endian*/
        PPC64LE,
        /** Sun sparc 32 bit */
        SPARC,
        /** Sun sparc 64 bit */
        SPARCV9,
        /** IBM zSeries S/390 64 bit */
        S390X,
        /** ARM */
        ARM,
        /** AArch64 */
        AARCH64,
        /** LOONGARCH64 */
        LOONGARCH64,
        /** MIPS 64-bit little endian */
        MIPS64EL,
        /** Unknown CPU */
        UNKNOWN;

        @Override
        public String toString() { return name().toLowerCase(LOCALE); }
    }

    /**
     * Determines the operating system jffi is running on
     *
     * @return An member of the <code>OS</code> enum.
     */
    private static OS determineOS() {
        String osName = System.getProperty("os.name").split(" ")[0];
        if (Util.startsWithIgnoreCase(osName, "mac", LOCALE) || Util.startsWithIgnoreCase(osName, "darwin", LOCALE)) {
            return OS.DARWIN;
        } else if (Util.startsWithIgnoreCase(osName, "linux", LOCALE)) {
            return OS.LINUX;
        } else if (Util.startsWithIgnoreCase(osName, "sunos", LOCALE) || Util.startsWithIgnoreCase(osName, "solaris", LOCALE)) {
            return OS.SOLARIS;
        } else if (Util.startsWithIgnoreCase(osName, "aix", LOCALE)) {
            return OS.AIX; 
        } else if (Util.startsWithIgnoreCase(osName, "os400", LOCALE) || Util.startsWithIgnoreCase(osName, "os/400", LOCALE)) {
            return OS.IBMI;
        } else if (Util.startsWithIgnoreCase(osName, "openbsd", LOCALE)) {
            return OS.OPENBSD;
        } else if (Util.startsWithIgnoreCase(osName, "freebsd", LOCALE)) {
            return OS.FREEBSD;
	} else if (Util.startsWithIgnoreCase(osName, "dragonfly", LOCALE)) {
            return OS.DRAGONFLY;
        } else if (Util.startsWithIgnoreCase(osName, "windows", LOCALE)) {
            return OS.WINDOWS;
        } else {
            throw new RuntimeException("cannot determine operating system");
        }
    }
    
    /**
     * Determines the CPU architecture the JVM is running on.
     *
     * This normalizes all the variations that are equivalent (e.g. i386, x86, i86pc)
     * into a common cpu type.
     *
     * @return A member of the <code>CPU</code> enum.
     */
    private static CPU determineCPU() {
        String archString = System.getProperty("os.arch", "unknown");
        if (Util.equalsIgnoreCase("x86", archString, LOCALE) || Util.equalsIgnoreCase("i386", archString, LOCALE) || Util.equalsIgnoreCase("i86pc", archString, LOCALE)) {
            return CPU.I386;
        } else if (Util.equalsIgnoreCase("x86_64", archString, LOCALE) || Util.equalsIgnoreCase("amd64", archString, LOCALE)) {
            return CPU.X86_64;
        } else if (Util.equalsIgnoreCase("ppc", archString, LOCALE) || Util.equalsIgnoreCase("powerpc", archString, LOCALE)) {
            return CPU.PPC;
        } else if (Util.equalsIgnoreCase("ppc64", archString, LOCALE) || Util.equalsIgnoreCase("powerpc64", archString, LOCALE)) {
            if ("little".equals(System.getProperty("sun.cpu.endian"))) {
                return CPU.PPC64LE;
            }
            return CPU.PPC64;
        } else if (equalsIgnoreCase("ppc64le", archString, LOCALE) || equalsIgnoreCase("powerpc64le", archString, LOCALE)) {
            return CPU.PPC64LE;
        } else if (equalsIgnoreCase("s390", archString, LOCALE) || equalsIgnoreCase("s390x", archString, LOCALE)) {
            return CPU.S390X;
        } else if (Util.equalsIgnoreCase("arm", archString, LOCALE) || Util.equalsIgnoreCase("armv7l", archString, LOCALE)) {
            return CPU.ARM;
        } else if (Util.equalsIgnoreCase("aarch64", archString, LOCALE)) {
            return CPU.AARCH64;
        } else if (Util.equalsIgnoreCase("loongarch64", archString, LOCALE)) {
            return CPU.LOONGARCH64;
        } else if (Util.equalsIgnoreCase("mips64", archString, LOCALE) || Util.equalsIgnoreCase("mips64el", archString, LOCALE)) {
            return CPU.MIPS64EL;

        }

        // Try to find by lookup up in the CPU list
        for (CPU cpu : CPU.values()) {
            if (Util.equalsIgnoreCase(cpu.name(), archString, LOCALE)) {
                return cpu;
            }
        }

        throw new RuntimeException("cannot determine CPU");
    }
    
    public static CPU getCPU() {
        return cpu != null ? cpu : (cpu = determineCPU());
    }
    
    public static OS getOS() {
        return os != null ? os : (os = determineOS());
    }

    /**
     * Gets the name of the stub library.
     *
     * @return The name of the stub library as a <code>String</code>
     */
    private static String getStubLibraryName() {
        return stubLibraryName;
    }

    /**
     * Gets the name of this <code>Platform</code>.
     *
     * @return The name of this platform.
     */
    public static String getPlatformName() {
        if (getOS().equals(OS.DARWIN)) {
            return "Darwin";
        }

       
        String osName = System.getProperty("os.name").split(" ")[0];
        return getCPU().name().toLowerCase(LOCALE) + "-" + osName;
    }
    /**
     * Gets the path within the jar file of the stub native library.
     *
     * @return The path of the jar file.
     */
    private static String getStubLibraryPath() {
        String mappedLibraryName = OS.IBMI.equals(getOS()) ? ("lib" + stubLibraryName + ".so"): System.mapLibraryName(stubLibraryName);
        return "jni/" + getPlatformName() + "/"+ mappedLibraryName;
    }
    
    public StubLoader() {}
    
    /**
     * Loads the stub library
     */
    static void load() {
        final String libName = getStubLibraryName();
        List<Throwable> errors = new ArrayList<Throwable>();
        String bootPath = getBootPath();
        if (bootPath != null && loadFromBootPath(libName, bootPath, errors)) {
            return;
        }

        String libraryPath = System.getProperty("java.library.path");
        if (libraryPath != null && loadFromBootPath(libName, libraryPath, errors)) {
            return;
        }

        // try user-specified location only if present
        if (jffiExtractDir != null) {
            try {
                loadFromJar(jffiExtractDir);
                return;
            } catch (SecurityException se) {
                throw se;
            } catch (Throwable t1) {
                UnsatisfiedLinkError ule = new UnsatisfiedLinkError("could not load jffi library from " + jffiExtractDir);
                ule.initCause(t1);
                throw ule;
            }
        }

        // try default tmp location with failover to current directory
        try {
            loadFromJar(null);
            return;
        } catch (SecurityException se) {
            throw se;
        } catch (Throwable t) {
            try {
                loadFromJar(new File(System.getProperty("user.dir")));
            } catch (SecurityException se) {
                throw se;
            } catch (Throwable t1){
                errors.add(t1);
            }
        }

        // aggregate error output and rethrow
        if (!errors.isEmpty()) {
            Collections.reverse(errors);
            CharArrayWriter caw = new CharArrayWriter();
            PrintWriter pw = new PrintWriter(caw);
            for (Throwable t : errors) {
                t.printStackTrace(pw);
            }
            throw new UnsatisfiedLinkError(new String(caw.toCharArray()));
        }
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
                return null;
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {}
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

    private static boolean loadFromBootPath(String libName, String bootPath, Collection<Throwable> errors) {
        String[] dirs = bootPath.split(File.pathSeparator);
        for (int i = 0; i < dirs.length; ++i) {
            String soname = System.mapLibraryName(libName);
            
            // First try to load <dir>/${cpu}-${os}/libjffi-x.y.so, then fallback to <dir>/libjffi-x.y.so 
            File stub = new File(new File(dirs[i], getPlatformName()), soname);
            if (!stub.isFile()) {
                stub = new File(new File(dirs[i]), soname);
            }

            String path = stub.getAbsolutePath();
            if (stub.isFile()) {
                try {
                    System.load(path);
                    return true;
                } catch (UnsatisfiedLinkError ex) {
                    errors.add(ex);
                }
            }

            if (getOS() == OS.DARWIN) {
                path = getAlternateLibraryPath(path);
                if (new File(path).isFile()) {
                    try {
                        System.load(path);
                        return true;
                    } catch (UnsatisfiedLinkError ex) {
                        errors.add(ex);
                    }
                }
            }
        }
        return false;
    }
    
    static String dlExtension() {
        switch (getOS()) {
            case WINDOWS:
                return "dll";
            case DARWIN:
                return "dylib";
            default:
                return "so";
        }
    } 

    private static void loadFromJar(File tmpDirFile) throws IOException, LinkageError {
        File dstFile;

        // Install the stub library
        String jffiExtractName = StubLoader.jffiExtractName;

        try (InputStream sourceIS = getStubLibraryStream()) {
            dstFile = calculateExtractPath(tmpDirFile, jffiExtractName);
            if (jffiExtractName != null && dstFile.exists()) {
                // unpacking to a specific name and that file exists, verify it
                verifyExistingLibrary(dstFile, sourceIS);
            } else {
                unpackLibrary(dstFile, sourceIS);
            }
        } catch (IOException ioe) {
            // If we get here it means we are unable to write the stub library to the system default temp location.
            throw tempReadonlyError(ioe);
        }

        try {
            System.load(dstFile.getAbsolutePath());
            if (null == jffiExtractName) dstFile.delete();
        } catch (UnsatisfiedLinkError ule) {
            // If we get here it means the file wrote to temp ok but can't be loaded from there.
            throw tempLoadError(ule);
        }
    }

    private static void unpackLibrary(File dstFile, InputStream sourceIS) throws IOException {
        // Write the library to the tempfile
        try (FileOutputStream os = new FileOutputStream(dstFile)) {
            ReadableByteChannel srcChannel = Channels.newChannel(sourceIS);

            for (long pos = 0; sourceIS.available() > 0; ) {
                pos += os.getChannel().transferFrom(srcChannel, pos, Math.max(4096, sourceIS.available()));
            }
        }
    }

    private static void verifyExistingLibrary(File dstFile, InputStream sourceIS) throws IOException {
        int sourceSize = sourceIS.available();

        try (FileInputStream targetIS = new FileInputStream(dstFile)) {
            // perform minimal verification of the found file
            int targetSize = targetIS.available();
            if (targetSize != sourceSize) throw sizeMismatchError(dstFile, sourceSize, targetSize);

            // compare sha-256 for existing file
            MessageDigest sourceMD = MessageDigest.getInstance("SHA-256");
            MessageDigest targetMD = MessageDigest.getInstance("SHA-256");
            DigestInputStream sourceDIS = new DigestInputStream(sourceIS, sourceMD);
            DigestInputStream targetDIS = new DigestInputStream(targetIS, targetMD);
            byte[] buf = new byte[8192];
            while (sourceIS.available() > 0) {
                sourceDIS.read(buf);
                targetDIS.read(buf);
            }
            byte[] sourceDigest = sourceMD.digest();
            byte[] targetDigest = targetMD.digest();

            if (!Arrays.equals(sourceDigest, targetDigest)) throw digestMismatchError(dstFile);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IOException(nsae);
        }
    }

    private static SecurityException sizeMismatchError(File dstFile, int sourceSize, int targetSize) {
        return new SecurityException("file size mismatch: " + dstFile + " (" + targetSize + ") does not match packaged library (" + sourceSize + ")");
    }

    private static SecurityException digestMismatchError(File dstFile) {
        return new SecurityException("digest mismatch: " + dstFile + " does not match packaged library");
    }

    static File calculateExtractPath(File tmpDirFile, String jffiExtractName) throws IOException {
        if (jffiExtractName == null) return calculateExtractPath(tmpDirFile);

        File dstFile;

        // allow empty name to mean "jffi-#.#"
        if (null == jffiExtractName || jffiExtractName.isEmpty()) {
            jffiExtractName = "jffi-" + VERSION_MAJOR + "." + VERSION_MINOR;
        }

        // add extension if necessary
        if (!jffiExtractName.endsWith(dlExtension())) {
            jffiExtractName = jffiExtractName + "." + dlExtension();
        }

        // use tmpdir if no dir was specified
        if (null == tmpDirFile) {
            dstFile = new File(TMPDIR, jffiExtractName);
        } else {
            dstFile = new File(tmpDirFile, jffiExtractName);
        }

        return dstFile;
    }

    static File calculateExtractPath(File tmpDirFile) throws IOException {
        File dstFile;

        // create tempfile
        if (null == tmpDirFile) {
            dstFile = File.createTempFile("jffi", "." + dlExtension());
        } else {
            dstFile = File.createTempFile("jffi", "." + dlExtension(), tmpDirFile);
        }

        dstFile.deleteOnExit();

        return dstFile;
    }

    private static IOException tempReadonlyError(IOException ioe) {
        return new IOException(
                TMPDIR_WRITE_ERROR + " " +
                    TMPDIR_RECOMMENDATION,
                ioe);
    }

    private static UnsatisfiedLinkError tempLoadError(UnsatisfiedLinkError ule) {
        return new UnsatisfiedLinkError(
                TMPDIR_EXEC_ERROR + " " +
                        TMPDIR_RECOMMENDATION + "\n" +
                        ule.getLocalizedMessage());
    }
    
    /**
     * Gets an <code>InputStream</code> representing the stub library image stored in
     * the jar file.
     *
     * @return A new <code>InputStream</code>
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
