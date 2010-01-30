/*
 * Copyright (C) 2009 Wayne Meissner
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
 * Manages allocation, disposal and protection of native memory pages
 */
abstract public class PageManager {
    /** The memory should be executable */
    public static final int PROT_EXEC = Foreign.PROT_EXEC;

    /** The memory should be readable */
    public static final int PROT_READ = Foreign.PROT_READ;

    /** The memory should be writable */
    public static final int PROT_WRITE = Foreign.PROT_WRITE;

    private static final class SingletonHolder {
        public static final PageManager INSTANCE = Platform.getPlatform().getOS() == Platform.OS.WINDOWS
                ? new Windows() : new Unix();
    }

    /**
     * Gets the page manager for the current platform.
     *
     * @return An instance of <tt>PageManager</tt>
     */
    public static final PageManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Gets the system page size.
     *
     * @return The size of a page on the current system, in bytes.
     */
    public final long pageSize() {
        return Foreign.getInstance().pageSize();
    }

    /**
     * Allocates native memory pages.
     *
     * The memory allocated is aligned on a page boundary, and the size of the
     * allocated memory is <tt>npages</tt> * {@link #pageSize}.
     *
     * @param npages The number of pages to allocate.
     * @param protection The initial protection for the page.  This must be a
     *   bitmask of {@link #PROT_READ}, {@link #PROT_WRITE} and {@link #PROT_EXEC}.
     *
     * @return The native address of the allocated memory.
     */
    public abstract long allocatePages(int npages, int protection);

    /**
     * Free pages allocated via {@link #allocatePages }
     *
     * @param address The memory address as returned from {@link #allocatePages}
     * @param npages The number of pages to free.
     */
    public abstract void freePages(long address, int npages);

    /**
     * Sets the protection mask on a memory region.
     *
     * @param address The start of the memory region.
     * @param npages The number of pages to protect.
     * @param protection The protection mask.
     */
    public abstract void protectPages(long address, int npages, int protection);

    static final class Unix extends PageManager {

        @Override
        public long allocatePages(int npages, int protection) {
            long sz = npages * pageSize();
            return Foreign.getInstance().mmap(0, sz, protection,
                    Foreign.MAP_ANON | Foreign.MAP_PRIVATE, -1, 0);

        }

        @Override
        public void freePages(long address, int npages) {
            Foreign.getInstance().munmap(address, npages * pageSize());
        }

        @Override
        public void protectPages(long address, int npages, int protection) {
            Foreign.getInstance().mprotect(address, npages * pageSize(), protection);
        }
    }

    static final class Windows extends PageManager {

        public Windows() {
        }

        @Override
        public long allocatePages(int npages, int protection) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void freePages(long address, int npages) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void protectPages(long address, int npages, int protection) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
