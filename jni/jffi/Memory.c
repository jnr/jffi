/* 
 * Copyright (C) 2008, 2009 Wayne Meissner
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

#include <stdlib.h>
#include <unistd.h>
#ifndef _WIN32
#  include <sys/mman.h>
#else
#  include <windows.h>
#  include <winnt.h>
#  include <winbase.h>
#endif
#include <jni.h>
#include "Exception.h"
#include "LastError.h"
#include "com_kenai_jffi_Foreign.h"
#include "jffi.h"

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    pageSize
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_pageSize(JNIEnv *env, jobject self)
{
#ifndef _WIN32
    return sysconf(_SC_PAGESIZE);
#else
    SYSTEM_INFO si;
    GetSystemInfo(&si);

    return si.dwPageSize;
#endif
}

#ifndef _WIN32
static int PROT(int p);
static int FLAGS(int f);

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    mmap
 * Signature: (JJIIIJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_mmap(JNIEnv *env, jobject self, jlong addr, jlong len,
        jint prot, jint flags, jint fd, jlong off)
{
    caddr_t result;
    
    result = mmap(j2p(addr), len, PROT(prot), FLAGS(flags), fd, off);
    if (unlikely(result == (caddr_t) -1)) {
        jffi_save_errno();
        return -1;
    }

    return p2j(result);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    munmap
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_munmap(JNIEnv *env, jobject self, jlong addr, jlong len)
{
    int result = munmap(j2p(addr), len);
    if (unlikely(result != 0)) {
        jffi_save_errno();
        return -1;
    }

    return 0;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    mprotect
 * Signature: (JJI)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_mprotect(JNIEnv *env, jobject self, jlong addr, jlong len, jint prot)
{
    int result = mprotect(j2p(addr), len, PROT(prot));
    if (unlikely(result != 0)) {
        jffi_save_errno();
        return -1;
    }

    return 0;
}

static int
PROT(int p)
{
    int n = 0;

    n |= ((p & com_kenai_jffi_Foreign_PROT_NONE) != 0) ? PROT_NONE : 0;
    n |= ((p & com_kenai_jffi_Foreign_PROT_READ) != 0) ? PROT_READ : 0;
    n |= ((p & com_kenai_jffi_Foreign_PROT_WRITE) != 0) ? PROT_WRITE : 0;
    n |= ((p & com_kenai_jffi_Foreign_PROT_EXEC) != 0) ? PROT_EXEC : 0;

    return n;
}

static int
FLAGS(int j)
{
    int m = 0;
#define M(x) m |= ((j & com_kenai_jffi_Foreign_MAP_##x) != 0) ? MAP_##x : 0
    M(FIXED);
    M(SHARED);
    M(PRIVATE);
#ifdef MAP_NORESERVE
    M(NORESERVE);
#endif
    M(ANON);
#ifdef MAP_ALIGN
    M(ALIGN);
#endif
#ifdef MAP_TEXT
    M(TEXT);
#endif

    return m;
}

#else /* _WIN32 */
/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    VirtualAlloc
 * Signature: (JIII)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_VirtualAlloc(JNIEnv *env, jobject self, jlong addr, jint size, jint flags, jint prot)
{
    void* ptr = VirtualAlloc(j2p(addr), size, flags, prot);
    if (unlikely(ptr == NULL)) {
        jffi_save_errno();
        return 0;
    }

    return p2j(ptr);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    VirtualFree
 * Signature: (JI)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_kenai_jffi_Foreign_VirtualFree(JNIEnv *env, jobject self, jlong addr, jint size, jint flags)
{
    if (!VirtualFree(j2p(addr), size, flags)) {
        jffi_save_errno();
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    VirtualProtect
 * Signature: (JII)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_kenai_jffi_Foreign_VirtualProtect(JNIEnv *env, jobject self, jlong addr, jint size, jint prot)
{
    DWORD oldprot;
    if (!VirtualProtect(j2p(addr), size, prot, &oldprot)) {
        jffi_save_errno();
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

#endif /* !_WIN32 */
