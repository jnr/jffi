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

#include <sys/param.h>
#include <sys/types.h>
#ifndef _WIN32
#  include <sys/mman.h>
#endif
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <stdbool.h>
#ifndef _WIN32
#  include <unistd.h>
#else
#  include <windows.h>
#endif
#include <errno.h>

#include "MemoryUtil.h"

int
jffi_getPageSize(void)
{
#ifdef _WIN32
    SYSTEM_INFO si;
    GetSystemInfo(&si);
    return si.dwPageSize;
#else
    return sysconf(_SC_PAGESIZE);
#endif
}

void*
jffi_allocatePages(int npages)
{
#ifdef _WIN32
    return VirtualAlloc(NULL, npages * jffi_getPageSize(), MEM_COMMIT | MEM_RESERVE, PAGE_READWRITE);
#else
    caddr_t memory = mmap(NULL, npages * jffi_getPageSize(), PROT_READ | PROT_WRITE, MAP_ANON | MAP_PRIVATE, -1, 0);
    return (memory != (caddr_t) -1) ? memory : NULL;
#endif
}

bool
jffi_freePages(void *addr, int npages)
{
#ifdef _WIN32
    return VirtualFree(addr, 0, MEM_RELEASE);
#else
    return munmap(addr, npages * jffi_getPageSize()) == 0;
#endif
}

bool
jffi_makePagesExecutable(void* memory, int npages)
{
#ifdef _WIN32
    DWORD oldProtect;
    return VirtualProtect(memory, npages * jffi_getPageSize(), PAGE_EXECUTE_READ, &oldProtect);
#else
    return mprotect(memory, npages * jffi_getPageSize(), PROT_READ | PROT_EXEC) == 0;
#endif
}

