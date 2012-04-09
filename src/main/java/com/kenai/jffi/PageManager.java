/*
 * Copyright (C) 2009 Wayne Meissner
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
 * Manages allocation, disposal and protection of native memory pages
 */
abstract public class PageManager {
    /** The memory should be executable */
    public static final int PROT_EXEC = Foreign.PROT_EXEC;

    /** The memory should be readable */
    public static final int PROT_READ = Foreign.PROT_READ;

    /** The memory should be writable */
    public static final int PROT_WRITE = Foreign.PROT_WRITE;


    @SuppressWarnings("UnusedDeclaration") // loads the native lib
    private final Foreign foreign = Foreign.getInstance();
    
    private int pageSize;

    private static final class SingletonHolder {
        public static final PageManager INSTANCE = Platform.getPlatform().getOS() == Platform.OS.WINDOWS
                ? new Windows() : new Unix();
    }

    /**
     * Gets the page manager for the current platform.
     *
     * @return An instance of <tt>PageManager</tt>
     */
    public static PageManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Gets the system page size.
     *
     * @return The size of a page on the current system, in bytes.
     */
    public final long pageSize() {
        return pageSize != 0 ? pageSize : calculatePageSize();
    }

    private long calculatePageSize() {
        long pgSize = Foreign.pageSize();
        return pgSize < Integer.MAX_VALUE ? this.pageSize = (int) pgSize : pgSize;
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
            long memory = Foreign.mmap(0, sz, protection,
                    Foreign.MAP_ANON | Foreign.MAP_PRIVATE, -1, 0);

            return memory != -1L ? memory : 0L;
        }

        @Override
        public void freePages(long address, int npages) {
            Foreign.munmap(address, npages * pageSize());
        }

        @Override
        public void protectPages(long address, int npages, int protection) {
            Foreign.mprotect(address, npages * pageSize(), protection);
        }
    }

    static final class Windows extends PageManager {

        public Windows() {
        }

        @Override
        public long allocatePages(int pageCount, int protection) {
            return Foreign.VirtualAlloc(0, (int) pageSize() * pageCount, Foreign.MEM_COMMIT | Foreign.MEM_RESERVE, w32prot(protection));
        }

        @Override
        public void freePages(long address, int pageCount) {
            Foreign.VirtualFree(address, (int) pageSize() * pageCount, Foreign.MEM_RELEASE);
        }

        @Override
        public void protectPages(long address, int pageCount, int protection) {
            Foreign.VirtualProtect(address, (int) pageSize() * pageCount, w32prot(protection));
        }

        private static int w32prot(int p) {
            int w32 = Foreign.PAGE_NOACCESS;
        
            if ((p & (PROT_READ | PROT_WRITE)) == (PROT_READ | PROT_WRITE)) {
                w32 = Foreign.PAGE_READWRITE;

            } else if ((p & PROT_READ) == PROT_READ) {
                w32 = Foreign.PAGE_READONLY;
            }

            if ((p & PROT_EXEC) == PROT_EXEC) {
                w32 <<= 4;
            }
            
            return w32;
        }
    }
}
