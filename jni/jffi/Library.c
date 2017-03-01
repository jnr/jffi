/* 
 * Copyright (C) 2008-2010 Wayne Meissner
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

#include <sys/types.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <limits.h>

#if defined(_WIN32) || defined(__WIN32__)
# include <windows.h>
# include <malloc.h>
#else
# include <dlfcn.h>
#endif
#if defined (__sun) || defined(_AIX)
#  include <alloca.h>
#endif
#include <ffi.h>
#include <jni.h>
#include "jffi.h"

#include "Exception.h"
#include "com_kenai_jffi_Foreign.h"

#if defined(_WIN32) || defined(__WIN32__)
static void* dl_open(const char* name, int flags);
static void dl_error(char* buf, int size);
#define dl_sym(handle, name) GetProcAddress(handle, name)
#define dl_close(handle) FreeLibrary(handle)
enum { RTLD_LAZY=1, RTLD_NOW, RTLD_GLOBAL, RTLD_LOCAL };
#else
# define dl_open(name, flags) dlopen(name, flags != 0 ? flags : (RTLD_LAZY | RTLD_LOCAL))
# define dl_error(buf, size) do { \
    const char *e = dlerror(); snprintf(buf, size, "%s", e ? e : "unknown"); \
} while(0)
# define dl_sym(handle, name) dlsym(handle, name)
# define dl_close(handle) dlclose(handle)
#ifndef RTLD_LOCAL
# define RTLD_LOCAL 8
#endif
#endif

static int getMultibyteString(JNIEnv* env, char* dst, jstring jstr, int n);
static int getWideString(JNIEnv* env, wchar_t* dst, jstring jstr, int n);

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    dlopen
 * Signature: (Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_dlopen(JNIEnv* env, jobject self, jstring jPath, jint jFlags)
{
#ifdef _WIN32
    if (jPath == NULL) {
        return p2j(GetModuleHandle(NULL));
    } else {
        wchar_t path[PATH_MAX];
        DWORD dwFlags;
        getWideString(env, path, jPath, sizeof(path) / sizeof(path[0]));
        dwFlags = PathIsRelativeW(path) ? 0 : LOAD_WITH_ALTERED_SEARCH_PATH;
        return p2j(LoadLibraryExW(path, NULL, dwFlags));
    }
#else
    char path_[PATH_MAX];
    const char* path = NULL; // Handle dlopen(NULL, flags);
    void* handle = NULL;
    int flags = 0;
#define F(x) (jFlags & com_kenai_jffi_Foreign_RTLD_##x) != 0 ? RTLD_##x : 0;
    flags |= F(LAZY);
    flags |= F(GLOBAL);
    flags |= F(LOCAL);
    flags |= F(NOW);
#undef F

#ifdef _AIX
    flags |= RTLD_MEMBER; //  Needed for AIX
#endif
    
    if (jPath != NULL) {
        path = path_;
        getMultibyteString(env, path_, jPath, sizeof(path_));
    }

    handle = dl_open(path, flags);
    if (handle == NULL) {
        char errbuf[1024] = { 0 };
        dl_error(errbuf, sizeof(errbuf) - 1);
        throwException(env, UnsatisfiedLink, "%s", errbuf);
    }
    
    return p2j(handle);
#endif
}

/*
 * Class:     com_googlecode_jffi_NativeLibrary
 * Method:    dlclose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_dlclose(JNIEnv* env, jclass cls, jlong handle)
{
    dl_close(j2p(handle));
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_dlsym(JNIEnv* env, jclass cls, jlong handle, jstring jstr)
{
    char sym[1024];
    void* addr;

    getMultibyteString(env, sym, jstr, sizeof(sym));
#ifndef _WIN32
    dlerror(); // clear any errors
#endif
    addr = dl_sym(j2p(handle), sym);
    if (addr == NULL) {
        char errbuf[1024] = { 0 };
        dl_error(errbuf, sizeof(errbuf) - 1);
        throwException(env, UnsatisfiedLink, "%s", errbuf);
    }

    return p2j(addr);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    dlerror
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_com_kenai_jffi_Foreign_dlerror(JNIEnv* env, jobject self)
{
    char errbuf[1024] = { 0 };
    dl_error(errbuf, sizeof(errbuf) - 1);
    return (*env)->NewStringUTF(env, errbuf);
}


#if defined(_WIN32) || defined(__WIN32__)
static void*
dl_open(const char* name, int flags)
{
    if (name == NULL) {
        return GetModuleHandle(NULL);
    } else {
        DWORD dwFlags = PathIsRelative(name) ? 0 : LOAD_WITH_ALTERED_SEARCH_PATH;
        return LoadLibraryEx(name, NULL, dwFlags);
    }
}

static void
dl_error(char* buf, int size)
{
    FormatMessageA(FORMAT_MESSAGE_FROM_SYSTEM, NULL, GetLastError(),
            0, buf, size, NULL);
}
#endif

static int
getWideString(JNIEnv* env, wchar_t* dst, jstring src, int n)
{
    const jchar* jstr = NULL;
    int len, i;

    if (src != NULL) {
        jstr = (*env)->GetStringChars(env, src, NULL);
    }
    len = (*env)->GetStringLength(env, src);
    if (len > (n - 1)) len = n - 1;
    for (i = 0; i < len; ++i) {
        dst[i] = (wchar_t) jstr[i];
    }
    dst[len] = (wchar_t) 0;
    if (jstr != NULL) {
        (*env)->ReleaseStringChars(env, src, jstr);
    }
    return len;
}

static int
getMultibyteString(JNIEnv* env, char* dst, jstring src, int n)
{
    wchar_t* wstr = NULL;
    const jchar* jstr = NULL;
    int len, i;

    if (src != NULL) {
        jstr = (*env)->GetStringChars(env, src, NULL);
    }
    len = (*env)->GetStringLength(env, src);
    wstr = alloca(sizeof(wchar_t) * (len + 1));
    for (i = 0; i < len; ++i) {
        wstr[i] = (wchar_t) jstr[i];
    }
    wstr[len] = (wchar_t) 0;
    if (jstr != NULL) {
        (*env)->ReleaseStringChars(env, src, jstr);
    }
    return wcstombs(dst, wstr, n);
}

