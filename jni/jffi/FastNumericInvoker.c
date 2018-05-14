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

#include <sys/types.h>
#include <stdlib.h>
#if defined(__sun) || defined(_AIX)
# include <alloca.h>
#endif
#include <errno.h>
#include <ffi.h>
#include <jni.h>
#include "endian.h"
#include "jffi.h"
#include "Exception.h"
#include "CallContext.h"
#include "Array.h"
#include "LastError.h"
#include "FaultProtect.h"
#include "com_kenai_jffi_Foreign.h"
#include "FastNumeric.h"


/* for return values <= sizeof(long), need to use an ffi_sarg sized return value */
#if BYTE_ORDER == BIG_ENDIAN
# define RETVAL(retval, ctx) ((ctx->cif.rtype)->size > sizeof(ffi_sarg) ? (retval).j : (retval).sarg)
#else
# define RETVAL(retval, ctx) ((retval).j)
#endif

#define MAX_STACK_ARRAY (1024)


#define OBJIDX(flags) ((flags & com_kenai_jffi_ObjectBuffer_INDEX_MASK) >> com_kenai_jffi_ObjectBuffer_INDEX_SHIFT)
#define OBJTYPE(flags) ((flags) & com_kenai_jffi_ObjectBuffer_TYPE_MASK)

#define IS_ARRAY(flags) \
        ((OBJTYPE(flags) & ~com_kenai_jffi_ObjectBuffer_PRIM_MASK) == com_kenai_jffi_ObjectBuffer_ARRAY)

#define IS_BUFFER(flags) \
        ((OBJTYPE(flags) & ~com_kenai_jffi_ObjectBuffer_PRIM_MASK) == com_kenai_jffi_ObjectBuffer_BUFFER)


typedef struct Pinned {
    jobject object;
    int offset;
    int length;
    int flags;
} Pinned;

typedef struct ObjectParam {
    jobject object;
    int offset;
    int length;
    int flags;
} ObjectParam;


static jlong call1(JNIEnv* env, CallContext* ctx, void* function,
        jlong n1);
static jlong call2(JNIEnv* env, CallContext* ctx, void* function,
        jlong n1, jlong n2);
static jlong call3(JNIEnv* env, CallContext* ctx, void* function,
        jlong n1, jlong n2, jlong n3);
static jlong call4(JNIEnv* env, CallContext* ctx, void* function,
        jlong n1, jlong n2, jlong n3, jlong n4);
static jlong call5(JNIEnv* env, CallContext* ctx, void* function,
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5);
static jlong call6(JNIEnv* env, CallContext* ctx, void* function,
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5, jlong n6);
static bool pin_arrays(JNIEnv* env, Pinned* pinned, int pinnedCount, 
        Array* arrays, int *arrayCount, jlong* v);
static bool object_to_ptr(JNIEnv* env, jobject obj, int off, int len, int f, jlong* vp, 
        Array* arrays, int* arrayCount, Pinned* pinned, int* pinnedCount);

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrL
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeN0(JNIEnv* env, jobject self, jlong ctxAddress, jlong function)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;
    
    FAULTPROT_CTX(env, ctx, if (0) {
#if defined(LONG_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_LONG) != 0)) {
        invokeL0(ctx, j2p(function), &retval);
#endif    

#if defined(INT_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_INT) != 0)) {
        invokeI0(ctx, j2p(function), &retval.j);
#endif
    } else {
        ffi_call0(ctx, j2p(function), &retval);
    }, return 0);

    return RETVAL(retval, ctx);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLrL
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeN1(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, jlong arg1)
{
    return call1(env, (CallContext *) j2p(ctxAddress), j2p(function), arg1);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLrL
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeN2(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, jlong arg1, jlong arg2)
{
    return call2(env, (CallContext *) j2p(ctxAddress), j2p(function), arg1, arg2);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLLrL
 * Signature: (JJJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeN3(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3)
{
    return call3(env, (CallContext *) j2p(ctxAddress), j2p(function), arg1, arg2, arg3);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeN4(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4)
{
    return call4(env, (CallContext *) j2p(ctxAddress), j2p(function), arg1, arg2, arg3, arg4);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeN5(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5)
{
    return call5(env, (CallContext *) j2p(ctxAddress), j2p(function), arg1, arg2, arg3, arg4, arg5);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeN6(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5, jlong arg6)
{
    return call6(env, (CallContext *) j2p(ctxAddress), j2p(function), arg1, arg2, arg3, arg4, arg5, arg6);
}

static jlong
call1(JNIEnv* env, CallContext* ctx, void* function, jlong n1)
{
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, if (0) {
#if defined(LONG_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_LONG) != 0)) {
        invokeL1(ctx, function, &retval, n1);
#endif    

#if defined(INT_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_INT) != 0)) {
        invokeI1(ctx, function, &retval.j, (jint) n1);
#endif

    } else {
        ffi_call1(ctx, function, &retval, n1);
    }, return 0);

    return RETVAL(retval, ctx);
}

static jlong 
call2(JNIEnv* env, CallContext* ctx, void* function, jlong n1, jlong n2)
{
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, if (0) {
#if defined(LONG_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_LONG) != 0)) {
        invokeL2(ctx, function, &retval, n1, n2);
#endif    

#if defined(INT_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_INT) != 0)) {
        invokeI2(ctx, function, &retval.j, (jint) n1, (jint) n2);
#endif

    } else {
        ffi_call2(ctx, function, &retval, n1, n2);
    }, return 0);

    return RETVAL(retval, ctx);
}

static jlong
call3(JNIEnv* env, CallContext* ctx, void* function, jlong n1, jlong n2, jlong n3)
{
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, if (0) {
#if defined(LONG_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_LONG) != 0)) {
        invokeL3(ctx, function, &retval, n1, n2, n3);
#endif    

#if defined(INT_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_INT) != 0)) {
        invokeI3(ctx, function, &retval.j, (jint) n1, (jint) n2, (jint) n3);
#endif

    } else {
        ffi_call3(ctx, function, &retval, n1, n2, n3);
    }, return 0);

    return RETVAL(retval, ctx);
}



static jlong 
call4(JNIEnv* env, CallContext* ctx, void* function, jlong n1, jlong n2, jlong n3, jlong n4)
{
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, if (0) {
#if defined(LONG_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_LONG) != 0)) {
        invokeL4(ctx, function, &retval, n1, n2, n3, n4);
#endif    

#if defined(INT_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_INT) != 0)) {
        invokeI4(ctx, function, &retval.j, (jint) n1, (jint) n2, (jint) n3, (jint) n4);
#endif

    } else {
        ffi_call4(ctx, function, &retval, n1, n2, n3, n4);
    }, return 0);

    return RETVAL(retval, ctx);
}


static jlong 
call5(JNIEnv* env, CallContext* ctx, void* function,
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5)
{
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, if (0) {
#if defined(LONG_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_LONG) != 0)) {
        invokeL5(ctx, function, &retval, n1, n2, n3, n4, n5);
#endif    

#if defined(INT_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_INT) != 0)) {
        invokeI5(ctx, function, &retval.j, (jint) n1, (jint) n2, (jint) n3, (jint) n4, (jint) n5);
#endif

    } else {
        ffi_call5(ctx, function, &retval, n1, n2, n3, n4, n5);
    }, return 0);

    return RETVAL(retval, ctx);
}

static jlong 
call6(JNIEnv* env, CallContext* ctx, void* function,
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5, jlong n6)
{
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, if (0) {
#if defined(LONG_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_LONG) != 0)) {
        invokeL6(ctx, function, &retval, n1, n2, n3, n4, n5, n6);
#endif    

#if defined(INT_BYPASS_FFI)
    } else if (likely((ctx->flags & CALL_CTX_FAST_INT) != 0)) {
        invokeI6(ctx, function, &retval.j, (jint) n1, (jint) n2, (jint) n3, (jint) n4, (jint) n5, (jint) n6);
#endif

    } else {
        ffi_call6(ctx, function, &retval, n1, n2, n3, n4, n5, n6);
    }, return 0);

    return RETVAL(retval, ctx);
}

static bool
pin_arrays(JNIEnv* env, Pinned* pinned, int pinnedCount, 
        Array* arrays, int *arrayCount, jlong* v)
{
    int aryIdx;
    for (aryIdx = 0; aryIdx < pinnedCount; aryIdx++) {
        Pinned* p = &pinned[aryIdx];
        Array* ary = &arrays[*arrayCount];

        void* addr = jffi_getArrayCritical(env, p->object, p->offset, p->length, p->flags, ary);
        if (unlikely(addr == NULL)) {
            return false;
        }

        v[OBJIDX(p->flags)] = p2j(addr);
        (*arrayCount)++;
    }

    return true;
}

static bool 
object_to_ptr(JNIEnv* env, jobject obj, int off, int len, int f, jlong* vp, 
        Array* arrays, int* arrayCount, Pinned* pinned, int* pinnedCount)
{
    if (unlikely(obj == NULL)) {
        throwException(env, NullPointer, "null object for parameter %d", OBJIDX(f));
        return false;
                
    } else if (unlikely(IS_PINNED_ARRAY(f))) {
        Pinned* p = &pinned[(*pinnedCount)++];
        p->object = obj;
        p->offset = off;
        p->length = len;
        p->flags = f;
	    *vp = 0LL;
    
    } else if (IS_ARRAY(f)) {
        *vp = p2j(jffi_getArrayHeap(env, obj, off, len, f, &arrays[*arrayCount]));
        if (unlikely(*vp == 0L)) {
            return false;
        }
        (*arrayCount)++;
    
    } else if (IS_BUFFER((f))) {
        caddr_t addr = (caddr_t) (*env)->GetDirectBufferAddress(env, obj);
        if (unlikely(addr == NULL)) {
            throwException(env, NullPointer, 
                    "could not get direct buffer address for parameter %d", OBJIDX(f));
            return false;
        }
        *vp = p2j(addr + off);
    
    } else {
        throwException(env, IllegalArgument, "unsupported object type for parameter %d: %#x", OBJIDX(f), f);
        return false;
    }
    
    return true;
}


#define N1 n1
#define N2 N1, n2
#define N3 N2, n3
#define N4 N3, n4
#define N5 N4, n5
#define N6 N5, n6

#define INIT(n) \
    const int MAX_PARAM_INDEX = (n) - 1; \
    CallContext* ctx = (CallContext *) j2p(ctxAddress); \
    Array arrays[(n)]; \
    Pinned pinned[(n)]; \
    int arrayCount = 0, pinnedCount = 0; \
    jlong retval = 0; \
    jlong v[] = { N##n }

#define END \
    error: \
        RELEASE_ARRAYS(env, arrays, arrayCount); \
        return retval

#define ADDOBJ(obj, off, len, flags) do { \
    int idx = OBJIDX(flags); \
    if (unlikely(idx < 0 || idx > MAX_PARAM_INDEX)) { \
        throwException(env, OutOfBounds, "invalid object parameter index %d (expected 0..%d)", \
            idx, MAX_PARAM_INDEX); \
        goto error; \
    } \
    if (likely(IS_UNPINNED_ARRAY(flags) && len < MAX_STACK_ARRAY)) { \
        void* ptr = alloca(jffi_arraySize((len) + 1, (flags))); \
        if (unlikely(jffi_getArrayBuffer(env, obj, off, len, flags, &arrays[arrayCount], ptr) == NULL)) { \
            goto error; \
        } \
        v[idx] = p2j(ptr); \
        arrayCount++; \
    } else if (!object_to_ptr(env, obj, off, len, flags, &v[idx], arrays, &arrayCount, pinned, &pinnedCount)) { \
        goto error; \
    } \
} while (0)

#define PIN_ARRAYS do { \
    if (unlikely(pinnedCount > 0)) { \
        if (!pin_arrays(env, pinned, pinnedCount, arrays, &arrayCount, v)) goto error; \
    } \
} while(0)

#define CALL(n, args...) \
    PIN_ARRAYS; \
    retval = call##n(env, ctx, j2p(function), args); \
    END

#define CALL1 CALL(1, v[0])
#define CALL2 CALL(2, v[0], v[1])
#define CALL3 CALL(3, v[0], v[1], v[2])
#define CALL4 CALL(4, v[0], v[1], v[2], v[3])
#define CALL5 CALL(5, v[0], v[1], v[2], v[3], v[4])
#define CALL6 CALL(6, v[0], v[1], v[2], v[3], v[4], v[5])

#define IMPL(n) \
    INIT(n); \
    int objIdx; \
    for (objIdx = 0; objIdx < nobjects; objIdx++) { \
        ADDOBJ(objects[objIdx].object, objects[objIdx].offset, objects[objIdx].length, \
                objects[objIdx].flags); \
    } \
    CALL##n;


#define DEF_N(x) jlong n##x
#define DEF_N1 DEF_N(1)
#define DEF_N2 DEF_N1, DEF_N(2)
#define DEF_N3 DEF_N2, DEF_N(3)
#define DEF_N4 DEF_N3, DEF_N(4)
#define DEF_N5 DEF_N4, DEF_N(5)
#define DEF_N6 DEF_N5, DEF_N(6)

#define DEFINVOKE(n) \
static jlong invoke##n(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, DEF_N##n, ObjectParam* objects, int nobjects) \
{ \
    IMPL(n); \
}

DEFINVOKE(1)
DEFINVOKE(2)
DEFINVOKE(3)
DEFINVOKE(4)
DEFINVOKE(5)
DEFINVOKE(6)

#define DEF_O(x) jobject o##x, jint o##x##flags, jint o##x##off, jint o##x##len
#define DEF_O1 DEF_O(1)
#define DEF_O2 DEF_O1, DEF_O(2)
#define DEF_O3 DEF_O2, DEF_O(3)
#define DEF_O4 DEF_O3, DEF_O(4)
#define DEF_O5 DEF_O4, DEF_O(5)
#define DEF_O6 DEF_O5, DEF_O(6)

#define DEF_N(x) jlong n##x
#define DEF_N1 DEF_N(1)
#define DEF_N2 DEF_N1, DEF_N(2)
#define DEF_N3 DEF_N2, DEF_N(3)
#define DEF_N4 DEF_N3, DEF_N(4)
#define DEF_N5 DEF_N4, DEF_N(5)
#define DEF_N6 DEF_N5, DEF_N(6)

#define OBJ(x) { o##x, o##x##off, o##x##len, o##x##flags }
#define OBJ1 OBJ(1)
#define OBJ2 OBJ1, OBJ(2)
#define OBJ3 OBJ2, OBJ(3)
#define OBJ4 OBJ3, OBJ(4)
#define OBJ5 OBJ4, OBJ(5)
#define OBJ6 OBJ5, OBJ(6)

#define DEFJNI(n, o) \
JNIEXPORT jlong JNICALL \
Java_com_kenai_jffi_Foreign_invokeN##n##O##o(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, DEF_N##n, DEF_O##o) \
{ \
    ObjectParam objects[] = { OBJ##o };			   \
    return invoke##n(env, self, ctxAddress, function, N##n, objects, o); \
}

DEFJNI(1, 1)

DEFJNI(2, 1)
DEFJNI(2, 2)

DEFJNI(3, 1)
DEFJNI(3, 2)
DEFJNI(3, 3)

DEFJNI(4, 1)
DEFJNI(4, 2)
DEFJNI(4, 3)
DEFJNI(4, 4)

DEFJNI(5, 1)
DEFJNI(5, 2)
DEFJNI(5, 3)
DEFJNI(5, 4)
DEFJNI(5, 5)

DEFJNI(6, 1)
DEFJNI(6, 2)
DEFJNI(6, 3)
DEFJNI(6, 4)
DEFJNI(6, 5)
DEFJNI(6, 6)
