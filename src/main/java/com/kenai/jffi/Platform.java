/*
 * Copyright (C) 2007, 2008, 2009 Wayne Meissner
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
 *
 * 
 * Alternatively, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kenai.jffi;

/**
 * Convenience class to interrogate the system about various platform-specific details.
 */
public abstract class Platform {
    private static final java.util.Locale LOCALE = java.util.Locale.ENGLISH;
    private final OS os;
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
        I386(32),
        /** AMD 64 bit (aka EM64T/X64) */
        X86_64(64),
        /** Power PC 32 bit */
        PPC(32),
        /** Power PC 64 bit */
        PPC64(64),
        /** Sun sparc 32 bit */
        SPARC(32),
        /** Sun sparc 64 bit */
        SPARCV9(64),
        /** IBM zSeries S/390 64 bit */
        S390X(64),
        /** ARM */
        ARM(32),
        /** Unknown CPU */
        UNKNOWN(64);
        
        CPU(int dataModel) {
            this.dataModel = dataModel;
            this.addressMask = dataModel == 32 ? 0xffffffffL : 0xffffffffffffffffL;
        }

        public final int dataModel;
        public final long addressMask;
        @Override
        public String toString() { return name().toLowerCase(LOCALE); }
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
        String osName = System.getProperty("os.name").split(" ")[0];
        if (startsWithIgnoreCase(osName, "mac") || startsWithIgnoreCase(osName, "darwin")) {
            return OS.DARWIN;
        
        } else if (startsWithIgnoreCase(osName, "linux")) {
            return OS.LINUX;
        
        } else if (startsWithIgnoreCase(osName, "sunos") || startsWithIgnoreCase(osName, "solaris")) {
            return OS.SOLARIS;
        
        } else if (startsWithIgnoreCase(osName, "aix")) {
            return OS.AIX; 
        
        } else if (startsWithIgnoreCase(osName, "openbsd")) {
            return OS.OPENBSD;
        
        } else if (startsWithIgnoreCase(osName, "freebsd")) {
            return OS.FREEBSD;
        
        } else if (startsWithIgnoreCase(osName, "windows")) {
            return OS.WINDOWS;
        
        } else {
            return OS.UNKNOWN;
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
                return newDarwinPlatform();
            
            case WINDOWS:
                return newWindowsPlatform();
            
            default:
                return newDefaultPlatform(os);
        }
    }
    
    private static Platform newDarwinPlatform() {
        return new Darwin();
    }
    
    private static Platform newWindowsPlatform() {
        return new Windows();
    }
    
    private static Platform newDefaultPlatform(OS os) {
        return new Default(os);
    }
    
    
    private static final class ArchHolder {
        public static final CPU cpu = determineCPU();
        
        /**
         * Determines the CPU architecture the JVM is running on.
         *
         * This normalizes all the variations that are equivalent (e.g. i386, x86, i86pc)
         * into a common cpu type.
         *
         * @return A member of the <tt>CPU</tt> enum.
         */
        private static CPU determineCPU() {
            String archString = null;
            try {
                archString = Foreign.getInstance().getArch();
            } catch (UnsatisfiedLinkError ex) {}
            
            if (archString == null || "unknown".equals(archString)) {
                archString = System.getProperty("os.arch", "unknown");
            }

            if ("x86".equalsIgnoreCase(archString) || "i386".equalsIgnoreCase(archString) || "i86pc".equalsIgnoreCase(archString)) {
                return CPU.I386;

            } else if ("x86_64".equalsIgnoreCase(archString) || "amd64".equalsIgnoreCase(archString)) {
                return CPU.X86_64;

            } else if ("ppc".equalsIgnoreCase(archString) || "powerpc".equalsIgnoreCase(archString)) {
                return CPU.PPC;

            } else if ("ppc64".equalsIgnoreCase(archString) || "powerpc64".equalsIgnoreCase(archString)) {
                return CPU.PPC64;
            
            } else if ("s390".equalsIgnoreCase(archString) || "s390x".equalsIgnoreCase(archString)) {
                return CPU.S390X;
                
            } else if ("arm".equalsIgnoreCase(archString)) {
                return CPU.ARM;
                
            }

            // Try to find by lookup up in the CPU list
            for (CPU cpu : CPU.values()) {
                if (cpu.name().equalsIgnoreCase(archString)) {
                    return cpu;
                }
            }

            return CPU.UNKNOWN;
        }
    }

    /**
     * Constructs a new <tt>Platform</tt> instance.
     *
     * @param os The current operating system.
     */
    private Platform(OS os) {
        this.os = os;

        int version = 5;
        try {
            String versionString = System.getProperty("java.version");
            if (versionString != null) {
                String[] v = versionString.split("\\.");
                version = Integer.valueOf(v[1]);
            }
        } catch (Exception ex) {
            // Assume version 5 or above.
            version = 5;
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
        return ArchHolder.cpu;
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
    public abstract int longSize();

    /**
     * Gets the size of a C address/pointer on the native platform.
     *
     * @return the size of a pointer in bits
     */
    public final int addressSize() {
        return getCPU().dataModel;
    }

    /**
     * Gets the 32/64bit mask of a C address/pointer on the native platform.
     *
     * @return the size of a pointer in bits
     */
    public final long addressMask() {
        return getCPU().addressMask;
    }

    /**
     * Gets the name of this <tt>Platform</tt>.
     *
     * @return The name of this platform.
     */
    public String getName() {
        String osName = System.getProperty("os.name").split(" ")[0];
        return getCPU().name().toLowerCase(LOCALE) + "-" + osName;
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
        
        public final int longSize() {
            return getCPU().dataModel;
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
        
        public final int longSize() {
            return getCPU().dataModel;
        }

    }

    /**
     * A {@link Platform} subclass representing the Windows system.
     */
    private static final class Windows extends Platform {

        public Windows() {
            super(OS.WINDOWS);
        }

        @Override
        public String getLibraryNamePattern() {
            return ".*\\.dll$";
        }
        
        public final int longSize() {
            return 32;
        }
    }

    private static boolean startsWithIgnoreCase(String s1, String s2) {
        return s1.startsWith(s2)
            || s1.toUpperCase(LOCALE).startsWith(s2.toUpperCase(LOCALE))
            || s1.toLowerCase(LOCALE).startsWith(s2.toLowerCase(LOCALE));
    }
}

