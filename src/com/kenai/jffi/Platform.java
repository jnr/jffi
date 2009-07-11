/*
 * Copyright (C) 2007, 2008, 2009 Wayne Meissner
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

/**
 * Convenience class to interrogate the system about various platform-specific details.
 */
public abstract class Platform {
    private final OS os;
    private final CPU cpu;
    private final int addressSize;
    private final long addressMask;
    private final int javaVersionMajor;

    /**
     * The common names of operating systems.
     *
     * <b>Note</b> The names of the enum values are used in other parts of the
     * code to determine where to find the native stub library.  Do not rename.
     */
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
        /** Unknown CPU */
        UNKNOWN;

        @Override
        public String toString() { return name().toLowerCase(); }
    }

    /**
     * Holds a single, lazily loaded instance of <tt>Platform</tt>
     */
    private static final class SingletonHolder {
        static final Platform PLATFORM = determinePlatform(determineOS());
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
            throw new ExceptionInInitializerError("Unsupported operating system");
        }
    }

    /**
     * Determines the <tt>Platform</tt> that best describes the <tt>OS</tt>
     *
     * @param os The operating system.
     * @return An instance of <tt>Platform</tt>
     */
    private static final Platform determinePlatform(OS os) {
        switch (os) {
            case DARWIN:
                return new Darwin();
            case WINDOWS:
                return new Windows();
            case UNKNOWN:
                throw new ExceptionInInitializerError("Unsupported operating system");
            default:
                return new Default(os);
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
        } else if ("ppc64".equals(archString)) {
            return CPU.PPC64;
        } else if ("sparc".equals(archString)) {
            return CPU.SPARC;
        } else if ("sparcv9".equals(archString)) {
            return CPU.SPARCV9;
        } else {
            throw new ExceptionInInitializerError("Unsupported CPU architecture: " + archString);
        }
    }

    /**
     * Constructs a new <tt>Platform</tt> instance.
     *
     * @param os The current operating system.
     */
    private Platform(OS os) {
        this.os = os;
        this.cpu = determineCPU();

        
        int dataModel = Integer.getInteger("sun.arch.data.model", 0);

        //
        // If we're running on a broken JVM that doesn't support the sun.arch.data.model property
        // try to deduce the data model using the CPU type.
        //
        if (dataModel != 32 && dataModel != 64) {
            switch (cpu) {
                case I386:
                case PPC:
                case SPARC:
                    dataModel = 32;
                    break;
                case X86_64:
                case PPC64:
                case SPARCV9:
                    dataModel = 64;
                    break;
                default:
                    throw new ExceptionInInitializerError("Cannot determine cpu address size");
            }
        }

        addressSize = dataModel;
        addressMask = addressSize == 32 ? 0xffffffffL : 0xffffffffffffffffL;
        int version = 5;
        try {
            String versionString = System.getProperty("java.version");
            if (versionString != null) {
                String[] v = versionString.split("\\.");
                version = Integer.valueOf(v[1]);
            }
        } catch (Exception ex) {
            throw new ExceptionInInitializerError("Could not determine java version");
        }
        javaVersionMajor = version;
    }
    
    /**
     * Gets the current <tt>Platform</tt>
     *
     * @return The current platform.
     */
    public static final Platform getPlatform() {
        return SingletonHolder.PLATFORM;
    }

    /**
     * Gets the current Operating System.
     *
     * @return A <tt>OS</tt> value representing the current Operating System.
     */
    public final OS getOS() {
        return os;
    }

    /**
     * Gets the current processor architecture the JVM is running on.
     *
     * @return A <tt>CPU</tt> value representing the current processor architecture.
     */
    public final CPU getCPU() {
        return cpu;
    }
    
    /**
     * Gets the version of the Java Virtual Machine (JVM) jffi is running on.
     *
     * @return A number representing the java version.  e.g. 5 for java 1.5, 6 for java 1.6
     */
    public final int getJavaMajorVersion() {
        return javaVersionMajor;
    }

    /**
     * Gets the size of a C 'long' on the native platform.
     *
     * @return the size of a long in bits
     */
    public final int longSize() {
        return addressSize;
    }

    /**
     * Gets the size of a C address/pointer on the native platform.
     *
     * @return the size of a pointer in bits
     */
    public final int addressSize() {
        return addressSize;
    }

    /**
     * Gets the 32/64bit mask of a C address/pointer on the native platform.
     *
     * @return the size of a pointer in bits
     */
    public final long addressMask() {
        return addressMask;
    }

    /**
     * Gets the name of this <tt>Platform</tt>.
     *
     * @return The name of this platform.
     */
    public String getName() {
        String osName = System.getProperty("os.name").split(" ")[0];
        return getCPU().name().toLowerCase() + "-" + osName;
    }

    /**
     * Maps from a generic library name (e.g. "c") to the platform specific library name.
     *
     * @param libName The library name to map
     * @return The mapped library name.
     */
    public String mapLibraryName(String libName) {
        //
        // A specific version was requested - use as is for search
        //
        if (libName.matches(getLibraryNamePattern())) {
            return libName;
        }
        return System.mapLibraryName(libName);
    }

    /**
     * Gets the regex string used to match platform-specific libraries
     * @return
     */
    public String getLibraryNamePattern() {
        return "lib.*\\.so.*$";
    }

    /**
     * Checks if the current platform is supported by JFFI.
     *
     * @return <tt>true</tt> if the platform is supported, else false.
     */
    public boolean isSupported() {
        //
        // Call a function in the stub library - this will throw an
        // exception if there is no stub lib for this platform.
        //
        int version = Foreign.getInstance().getVersion();
        if ((version & 0xffff00) == (Foreign.VERSION_MAJOR << 16 | Foreign.VERSION_MINOR << 8)) {
            return true;
        }

        throw new UnsatisfiedLinkError("Incorrect native library version");
    }

    private static final class Default extends Platform {

        public Default(OS os) {
            super(os);
        }
        
    }
    /**
     * A {@link Platform} subclass representing the MacOS system.
     */
    private static final class Darwin extends Platform {

        public Darwin() {
            super(OS.DARWIN);
        }

        @Override
        public String mapLibraryName(String libName) {
            //
            // A specific version was requested - use as is for search
            //
            if (libName.matches(getLibraryNamePattern())) {
                return libName;
            }
            return "lib" + libName + ".dylib";
        }
        @Override
        public String getLibraryNamePattern() {
            return "lib.*\\.(dylib|jnilib)$";
        }
        
        @Override
        public String getName() {
            return "Darwin";
        }

    }

    /**
     * A {@link Platform} subclass representing the Windows system.
     */
    private static class Windows extends Platform {

        public Windows() {
            super(OS.WINDOWS);
        }

        @Override
        public String getLibraryNamePattern() {
            return ".*\\.dll$";
        }
    }
}

