/*
 * Copyright (C) 2007 Wayne Meissner
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

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <stdlib.h>
#include <jni.h>
#include "jffi.h"
#include "com_kenai_jffi_Foreign.h"

#define GET(JTYPE, NTYPE) JNIEXPORT NTYPE JNICALL \
Java_com_kenai_jffi_Foreign_get##JTYPE(JNIEnv* env, jobject self, jlong address) \
{ NTYPE tmp; memcpy(&tmp, j2p(address), sizeof(tmp)); return tmp; }

#define PUT(JTYPE, NTYPE) \
JNIEXPORT void JNICALL \
Java_com_kenai_jffi_Foreign_put##JTYPE(JNIEnv *env, jobject self, jlong address, NTYPE value) \
{ memcpy(j2p(address), &value, sizeof(value)); }

#define COPY(JTYPE, NTYPE) \
JNIEXPORT void JNICALL \
Java_com_kenai_jffi_Foreign_put##JTYPE##Array(JNIEnv* env, jobject unsafe, jlong address, jobject obj, jint offset, jint length) \
{ \
    (*env)->Get##JTYPE##ArrayRegion(env, obj, offset, length, (NTYPE *) j2p(address)); \
} \
JNIEXPORT void JNICALL \
Java_com_kenai_jffi_Foreign_get##JTYPE##Array(JNIEnv* env, jobject unsafe, jlong address, jobject obj, jint offset, jint length) \
{ \
    (*env)->Set##JTYPE##ArrayRegion(env, obj, offset, length, (NTYPE *) j2p(address)); \
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

/*
 * Class:     com_googlecode_jffi_lowlevel_Unsafe
 * Method:    memchr
 * Signature: (JIJ)I
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_memchr(JNIEnv* env, jobject self, jlong address, jint c, jlong maxlen)
{
    void* ptr = memchr(j2p(address), c, maxlen);
    if (ptr == NULL) {
        return -1;
    }
    return (int) (p2j(ptr) - address);
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
 * Signature: (J)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL
Java_com_kenai_jffi_Foreign_newDirectByteBuffer(JNIEnv* env, jobject self, jlong address, jlong capacity)
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
