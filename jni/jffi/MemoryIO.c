/*
 * Copyright (C) 2007 Wayne Meissner
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

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <stdlib.h>
#include <jni.h>
#include "jffi.h"
#include "FaultProtect.h"
#include "com_kenai_jffi_Foreign.h"

#if FAULT_PROTECT_ENABLED
# define PROT(stmt, rval) do { \
    JNIEnv* volatile env_ = env; \
    FaultData fd; \
    int val; \
    if (unlikely((val = jffi_setjmp(&fd)) != 0)) { \
        jffi_faultException(env_, &fd, val); \
        return rval; \
    } else { \
        ThreadData* td = thread_data_get(); \
        td->fault_data = &fd; \
        stmt; \
        td->fault_data = NULL; \
    } \
} while (0)

#else
# define PROT(stmt, rval) do { stmt; } while(0)
#endif

#ifndef MIN
# define MIN(a, b) ((a) < (b) ? (a) : (b))
#endif

static void putArrayChecked(JNIEnv* env, jlong address, jobject obj, jint offset, jint length, int typeSize,
    void (JNICALL *get)(JNIEnv *env, jobject array, jsize start, jsize l, void *buf));
static void getArrayChecked(JNIEnv* env, jlong address, jobject obj, jint offset, jint length, int typeSize,
    void (JNICALL *put)(JNIEnv *env, jobject array, jsize start, jsize l, const void *buf));

#define GET(JTYPE, NTYPE) JNIEXPORT NTYPE JNICALL \
Java_com_kenai_jffi_Foreign_get##JTYPE(JNIEnv* env, jobject self, jlong address) \
{ NTYPE tmp; memcpy(&tmp, j2p(address), sizeof(tmp)); return tmp; } \
JNIEXPORT NTYPE JNICALL \
Java_com_kenai_jffi_Foreign_get##JTYPE##Checked(JNIEnv* env, jobject self, jlong address) \
{ NTYPE tmp; PROT(memcpy(&tmp, j2p(address), sizeof(tmp)), 0); return tmp; }

#define PUT(JTYPE, NTYPE) \
JNIEXPORT void JNICALL \
Java_com_kenai_jffi_Foreign_put##JTYPE(JNIEnv *env, jobject self, jlong address, NTYPE value) \
{ memcpy(j2p(address), &value, sizeof(value)); } \
JNIEXPORT void JNICALL \
Java_com_kenai_jffi_Foreign_put##JTYPE##Checked(JNIEnv *env, jobject self, jlong address, NTYPE value) \
{ PROT(memcpy(j2p(address), &value, sizeof(value)),); }

#define COPY(JTYPE, NTYPE) \
JNIEXPORT void JNICALL \
Java_com_kenai_jffi_Foreign_put##JTYPE##Array(JNIEnv* env, jobject unsafe, jlong address, jobject obj, jint offset, jint length) \
{ \
    (*env)->Get##JTYPE##ArrayRegion(env, obj, offset, length, (NTYPE *) j2p(address)); \
} \
JNIEXPORT void JNICALL \
Java_com_kenai_jffi_Foreign_put##JTYPE##ArrayChecked(JNIEnv* env, jobject unsafe, jlong address, jobject obj, jint offset, jint length) \
{ \
    putArrayChecked(env, address, obj, offset, length, sizeof(NTYPE), \
        (void (JNICALL *)(JNIEnv *env, jobject array, jsize start, jsize l, void *buf)) (*env)->Get##JTYPE##ArrayRegion); \
} \
JNIEXPORT void JNICALL \
Java_com_kenai_jffi_Foreign_get##JTYPE##Array(JNIEnv* env, jobject unsafe, jlong address, jobject obj, jint offset, jint length) \
{ \
    (*env)->Set##JTYPE##ArrayRegion(env, obj, offset, length, (NTYPE *) j2p(address)); \
} \
JNIEXPORT void JNICALL \
Java_com_kenai_jffi_Foreign_get##JTYPE##ArrayChecked(JNIEnv* env, jobject unsafe, jlong address, jobject obj, jint offset, jint length) \
{ \
    getArrayChecked(env, address, obj, offset, length, sizeof(NTYPE), \
        (void (JNICALL *)(JNIEnv *env, jobject array, jsize start, jsize l, const void *)) (*env)->Set##JTYPE##ArrayRegion); \
}

static inline void
copy(void* dst, const void* src, int len)
{
    int i;
    for (i = 0; i < len; i++) *((char *) dst + i) = *((const char *) src + i);
}

static void
putArrayChecked(JNIEnv* env, jlong address, jobject obj, jint offset, jint length, int typeSize,
    void (JNICALL *get)(JNIEnv *env, jobject array, jsize start, jsize l, void *buf))
{
    jint copyOff = 0;
    PROT(while (copyOff < length) {
        jbyte tmp[4096];
        int copyLen = MIN((int) sizeof(tmp) / typeSize, length - copyOff);
        (*get)(env, obj, offset + copyOff, copyLen, tmp);
        copy(j2p(address + (copyOff * typeSize)), tmp, copyLen * typeSize);
        copyOff += copyLen;
    },);
}

static void
getArrayChecked(JNIEnv* env, jlong address, jobject obj, jint offset, jint length, int typeSize,
    void (JNICALL *put)(JNIEnv *env, jobject array, jsize start, jsize l, const void *buf))
{
    jint copyOff = 0;
    PROT(while (copyOff < length) {
        jbyte tmp[4096];
        int copyLen = MIN((int) sizeof(tmp) / typeSize, length - copyOff);
        copy(tmp, j2p(address + (copyOff * typeSize)), copyLen * typeSize);
        (*put)(env, obj, offset + copyOff, copyLen, tmp);
        copyOff += copyLen;
    },);
}

#define UNSAFE(J, N) GET(J, N) PUT(J, N) COPY(J, N)

UNSAFE(Byte, jbyte);
UNSAFE(Char, jchar);
UNSAFE(Boolean, jboolean);
UNSAFE(Short, jshort);
UNSAFE(Int, jint);
UNSAFE(Long, jlong);
UNSAFE(Float, jfloat);
UNSAFE(Double, jdouble);

/*
 * Class:     com_googlecode_jffi_JNIUnsafe
 * Method:    getAddress
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_getAddress(JNIEnv* ev, jobject self, jlong address)
{
    void* tmp;
    memcpy(&tmp, j2p(address), sizeof(tmp));
    return p2j(tmp);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_getAddressChecked(JNIEnv* env, jobject self, jlong address)
{
    void* tmp;
    PROT(memcpy(&tmp, j2p(address), sizeof(tmp)), 0);
    return p2j(tmp);
}


/*
 * Class:     com_googlecode_jffi_JNIUnsafe
 * Method:    putAddress
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_putAddress(JNIEnv* env, jobject self, jlong address, jlong value)
{
    void* tmp = j2p(value);
    memcpy(j2p(address), &tmp, sizeof(tmp));
}

JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_putAddressChecked(JNIEnv* env, jobject self, jlong address, jlong value)
{
    void* tmp = j2p(value);
    PROT(memcpy(j2p(address), &tmp, sizeof(tmp)),);
}

/*
 * Class:     com_googlecode_jffi_Unsafe_JNIUnsafe
 * Method:    setMemory
 * Signature: (JJB)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_setMemory(JNIEnv* env, jobject self, jlong address, jlong size, jbyte value)
{
    memset(j2p(address), value, size);
}

JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_setMemoryChecked(JNIEnv* env, jobject self, jlong address, jlong size, jbyte value)
{
    PROT(memset(j2p(address), value, size),);
}

/*
 * Class:     com_googlecode_jffi_lowlevel_Unsafe_JNIUnsafe
 * Method:    copyMemory
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_copyMemory(JNIEnv* env, jobject self, jlong src, jlong dst, jlong size)
{
    memcpy(j2p(dst), j2p(src), size);
}

JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_copyMemoryChecked(JNIEnv* env, jobject self, jlong src, jlong dst, jlong size)
{
    PROT(memcpy(j2p(dst), j2p(src), size),);
}

/*
 * Class:     com_googlecode_jffi_lowlevel_Unsafe
 * Method:    memchr
 * Signature: (JIJ)I
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_memchr(JNIEnv* env, jobject self, jlong address, jint c, jlong maxlen)
{
    return p2j(memchr(j2p(address), c, maxlen));
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_memchrChecked(JNIEnv* env, jobject self, jlong address, jint c, jlong maxlen)
{
    PROT(return p2j(memchr(j2p(address), c, maxlen)), 0);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    memmove
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_memmove(JNIEnv* env, jobject self, jlong dst, jlong src, jlong size)
{
      memmove(j2p(dst), j2p(src), size);
}

JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_memmoveChecked(JNIEnv* env, jobject self, jlong dst, jlong src, jlong size)
{
      PROT(memmove(j2p(dst), j2p(src), size),);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    memcpy
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_memcpy(JNIEnv* env, jobject self, jlong dst, jlong src, jlong size)
{
      memcpy(j2p(dst), j2p(src), size);
}

JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_memcpyChecked(JNIEnv* env, jobject self, jlong dst, jlong src, jlong size)
{
      PROT(memcpy(j2p(dst), j2p(src), size), );
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    strlen
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_strlen(JNIEnv* env, jobject self, jlong address)
{
    return (jlong) strlen(j2p(address));
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_strlenChecked(JNIEnv* env, jobject self, jlong address)
{
    PROT(return (jlong) strlen(j2p(address)), 0);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getUTF8StringAsBytes
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL
Java_com_kenai_jffi_Foreign_getZeroTerminatedByteArray__J(JNIEnv* env, jobject self, jlong address)
{
    const char* str = (const char*) j2p(address);
    int len = strlen(str);

    jbyteArray bytes = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *) str);
    
    return bytes;
}

JNIEXPORT jbyteArray JNICALL
Java_com_kenai_jffi_Foreign_getZeroTerminatedByteArrayChecked__J(JNIEnv* env, jobject self, jlong address)
{
    const char* str = (const char*) j2p(address);
    int len;

    // Just protecting the strlen against segfault should be sufficient
    PROT(len = strlen(str), NULL);

    jbyteArray bytes = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *) str);

    return bytes;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getZeroTerminatedByteArray
 * Signature: (JI)[B
 */
JNIEXPORT jbyteArray JNICALL
Java_com_kenai_jffi_Foreign_getZeroTerminatedByteArray__JI(JNIEnv* env, jobject self, jlong address, jint maxlen)
{
    const char *str = (const char*) j2p(address), *zp;
    jsize len = ((zp = memchr(str, 0, maxlen)) != NULL) ? zp - str : maxlen;
    
    jbyteArray bytes = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *) str);

    return bytes;
}

JNIEXPORT jbyteArray JNICALL
Java_com_kenai_jffi_Foreign_getZeroTerminatedByteArrayChecked__JI(JNIEnv* env, jobject self, jlong address, jint maxlen)
{
    const char *str = (const char*) j2p(address), *zp;
    jsize len;

    PROT(zp = memchr(str, 0, maxlen), NULL);
    len = zp != NULL ? zp - str : maxlen;
    jbyteArray bytes = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *) str);

    return bytes;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    putZeroTerminatedByteArray
 * Signature: (J[BII)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_putZeroTerminatedByteArray(JNIEnv *env, jobject self,
   jlong address, jbyteArray data, jint offset, jint length)
{
    (*env)->GetByteArrayRegion(env, data, offset, length, (jbyte *)j2p(address));
    *((char *) (uintptr_t) address + length) = '\0';
}

JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_putZeroTerminatedByteArrayChecked(JNIEnv *env, jobject self,
   jlong address, jbyteArray data, jint offset, jint length)
{
    char* cp = (char *) (uintptr_t) address;
    PROT({ *cp = 0; *(cp + length) ='\0';},);
    (*env)->GetByteArrayRegion(env, data, offset, length, (jbyte *)j2p(address));
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    allocateMemory
 * Signature: (JZ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_allocateMemory(JNIEnv* env, jobject self, jlong size, jboolean clear)
{
    void* memory = malloc(size);
    if (memory != NULL && clear != JNI_FALSE) {
        memset(memory, 0, size);
    }
    return p2j(memory);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeMemory
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_freeMemory(JNIEnv* env, jobject self, jlong address)
{
    free(j2p(address));
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    newDirectByteBuffer
 * Signature: (I)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL
Java_com_kenai_jffi_Foreign_newDirectByteBuffer(JNIEnv* env, jobject self, jlong address, jint capacity)
{
    return (*env)->NewDirectByteBuffer(env, j2p(address), capacity);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getDirectBufferAddress
 * Signature: (Lcom/kenai/jffi/Closure/Buffer;)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_getDirectBufferAddress(JNIEnv* env, jobject self, jobject buffer)
{
    return p2j((*env)->GetDirectBufferAddress(env, buffer));
}
