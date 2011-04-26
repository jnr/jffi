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
#include <errno.h>
#include <ffi.h>
#include <jni.h>
#include "endian.h"
#include "jffi.h"
#include "Exception.h"
#include "Function.h"
#include "CallContext.h"
#include "Array.h"
#include "LastError.h"
#include "com_kenai_jffi_Foreign.h"


/* for return values <= sizeof(long), need to use an ffi_sarg sized return value */
#define RETVAL(retval, rtype) ((rtype)->size > sizeof(ffi_sarg) ? (retval).j : (retval).sarg)

#if defined(__i386__) && 1
# define INT_BYPASS_FFI
#endif

#if defined(__x86_64__) && 1
# define INT_BYPASS_FFI
# define LONG_BYPASS_FFI
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


static void call1(CallContext* ctx, void* function, FFIValue* retval, 
        jlong n1);
static void call2(CallContext* ctx, void* function, FFIValue* retval, 
        jlong n1, jlong n2);
static void call3(CallContext* ctx, void* function, FFIValue* retval, 
        jlong n1, jlong n2, jlong n3);
static void call4(CallContext* ctx, void* function, FFIValue* retval, 
        jlong n1, jlong n2, jlong n3, jlong n4);
static void call5(CallContext* ctx, void* function, FFIValue* retval, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5);
static void call6(CallContext* ctx, void* function, FFIValue* retval, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5, jlong n6);
static bool pin_arrays(JNIEnv* env, Pinned* pinned, int pinnedCount, 
        Array* arrays, int *arrayCount, jlong* v);
static bool object_to_ptr(JNIEnv* env, jobject obj, int off, int len, int f, void** pptr, 
        Array* arrays, int* arrayCount, Pinned* pinned, int* pinnedCount);

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrL
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeVrN(JNIEnv* env, jobject self, jlong ctxAddress)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;
    
    if (0) {
#if defined(LONG_BYPASS_FFI) && defined(__x86_64__)
    } else if (likely(ctx->isFastLong)) {
        retval.j = ((jlong (*)(void)) ctx->function)();
#endif    

#if defined(INT_BYPASS_FFI) && defined(__i386__)
    } else if (likely(ctx->isFastInt)) {
        retval.sarg = ((jint (*)(void)) ctx->function)();
#endif
    } else {
        ffi_call0(ctx, ctx->function, &retval);
    }

    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLrL
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeNrN(JNIEnv* env, jobject self, jlong ctxAddress, jlong arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    call1((CallContext *) ctx, ctx->function, &retval, arg1);
    
    return RETVAL(retval, ctx->cif.rtype);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLrL
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeNNrN(JNIEnv* env, jobject self, jlong ctxAddress, jlong arg1, jlong arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    call2((CallContext *) ctx, ctx->function, &retval, arg1, arg2);
    
    return RETVAL(retval, ctx->cif.rtype);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLLrL
 * Signature: (JJJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeNNNrN(JNIEnv* env, jobject self, jlong ctxAddress,
        jlong arg1, jlong arg2, jlong arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    call3((CallContext *) ctx, ctx->function, &retval, arg1, arg2, arg3);
    
    return RETVAL(retval, ctx->cif.rtype);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeNNNNrN(JNIEnv* env, jobject self, jlong ctxAddress,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    call4((CallContext *) ctx, ctx->function, &retval, arg1, arg2, arg3, arg4);
    
    return RETVAL(retval, ctx->cif.rtype);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeNNNNNrN(JNIEnv* env, jobject self, jlong ctxAddress,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    call5((CallContext *) ctx, ctx->function, &retval, arg1, arg2, arg3, arg4, arg5);
    
    return RETVAL(retval, ctx->cif.rtype);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeNNNNNNrN(JNIEnv* env, jobject self, jlong ctxAddress,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5, jlong arg6)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    call6((CallContext *) ctx, ctx->function, &retval, arg1, arg2, arg3, arg4, arg5, arg6);
    
    return RETVAL(retval, ctx->cif.rtype);
}

static void
call1(CallContext* ctx, void* function, FFIValue* retval, jlong n1)
{
    if (0) {
#if defined(LONG_BYPASS_FFI) && defined(__x86_64__)
    } else if (likely(ctx->isFastLong)) {
        retval->j = ((jlong (*)(jlong)) function)(n1);
#endif    

#if defined(INT_BYPASS_FFI) && defined(__i386__)
    } else if (likely(ctx->isFastInt)) {
        retval->sarg = ((jint (*)(jint)) function)(*(jint *) &n1);
#endif

    } else {
        ffi_call1(ctx, function, retval, n1);
    }
    
    SAVE_ERRNO(ctx);
}

static void 
call2(CallContext* ctx, void* function, FFIValue* retval, jlong n1, jlong n2)
{
    if (0) {
#if defined(LONG_BYPASS_FFI) && defined(__x86_64__)
    } else if (likely(ctx->isFastLong)) {
        retval->j = ((jlong (*)(jlong, jlong)) function)(n1, n2);
#endif    

#if defined(INT_BYPASS_FFI) && defined(__i386__)
    } else if (likely(ctx->isFastInt)) {
        retval->sarg = ((jint (*)(jint, jint)) function)(
                *(jint *) &n1, *(jint *) &n2);
#endif

    } else {
        ffi_call2(ctx, function, retval, n1, n2);
    }
    
    SAVE_ERRNO(ctx);
}

static void 
call3(CallContext* ctx, void* function, FFIValue* retval, jlong n1, jlong n2, jlong n3)
{
    if (0) {
#if defined(LONG_BYPASS_FFI) && defined(__x86_64__)
    } else if (likely(ctx->isFastLong)) {
        retval->j = ((jlong (*)(jlong, jlong, jlong)) function)(n1, n2, n3);
#endif    

#if defined(INT_BYPASS_FFI) && defined(__i386__)
    } else if (likely(ctx->isFastInt)) {
        retval->sarg = ((jint (*)(jint, jint, jint)) function)(
                *(jint *) &n1, *(jint *) &n2, *(jint *) &n3);
#endif

    } else {
        ffi_call3(ctx, function, retval, n1, n2, n3);
    }
    
    SAVE_ERRNO(ctx);
}



static void 
call4(CallContext* ctx, void* function, FFIValue* retval, jlong n1, jlong n2, jlong n3, jlong n4)
{
    if (0) {
#if defined(LONG_BYPASS_FFI) && defined(__x86_64__)
    } else if (likely(ctx->isFastLong)) {
        retval->j = ((jlong (*)(jlong, jlong, jlong, jlong)) function)(n1, n2, n3, n4);
#endif    

#if defined(INT_BYPASS_FFI) && defined(__i386__)
    } else if (likely(ctx->isFastInt)) {
        retval->sarg = ((jint (*)(jint, jint, jint, jint)) function)(
                *(jint *) &n1, *(jint *) &n2, *(jint *) &n3, *(jint *) &n4);
#endif

    } else {
        ffi_call4(ctx, function, retval, n1, n2, n3, n4);
    }
    
    SAVE_ERRNO(ctx);
}


static void 
call5(CallContext* ctx, void* function, FFIValue* retval, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5)
{
    if (0) {
#if defined(LONG_BYPASS_FFI) && defined(__x86_64__)
    } else if (likely(ctx->isFastLong)) {
        retval->j = ((jlong (*)(jlong, jlong, jlong, jlong, jlong)) function)(n1, n2, n3, n4, n5);
#endif    

#if defined(INT_BYPASS_FFI) && defined(__i386__)
    } else if (likely(ctx->isFastInt)) {
        retval->sarg = ((jint (*)(jint, jint, jint, jint, jint)) function)(
                *(jint *) &n1, *(jint *) &n2, *(jint *) &n3, 
                *(jint *) &n4, *(jint *) &n5);
#endif

    } else {
        ffi_call5(ctx, function, retval, n1, n2, n3, n4, n5);
    }
    
    SAVE_ERRNO(ctx);
}

static void 
call6(CallContext* ctx, void* function, FFIValue* retval, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5, jlong n6)
{
    if (0) {
#if defined(LONG_BYPASS_FFI) && defined(__x86_64__)
    } else if (likely(ctx->isFastLong)) {
        retval->j = ((jlong (*)(jlong, jlong, jlong, jlong, jlong, jlong)) function)(
                n1, n2, n3, n4, n5, n6);
#endif    

#if defined(INT_BYPASS_FFI) && defined(__i386__)
    } else if (likely(ctx->isFastInt)) {
        retval->sarg = ((jint (*)(jint, jint, jint, jint, jint, jint)) function)(
                *(jint *) &n1, *(jint *) &n2, *(jint *) &n3, 
                *(jint *) &n4, *(jint *) &n5, *(jint *) &n6);
#endif

    } else {
        ffi_call6(ctx, function, retval, n1, n2, n3, n4, n5, n6);
    }
    
    SAVE_ERRNO(ctx);
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
object_to_ptr(JNIEnv* env, jobject obj, int off, int len, int f, void** pptr, 
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
    
    } else if (IS_ARRAY(f)) {
        *pptr = jffi_getArrayHeap(env, obj, off, len, f, &arrays[*arrayCount]);
        if (unlikely(*pptr == NULL)) {
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
        *pptr = addr + off;
    
    } else {
        throwException(env, IllegalArgument, "unsupported object type for parameter %d: %#x", OBJIDX(f), f);
        return false;
    }
    
    return true;
}

static void 
free_arrays(JNIEnv* env, Array* arrays, int arrayCount)
{
    int aryIdx;
    for (aryIdx = arrayCount; aryIdx >= 0; aryIdx--) {
        if (arrays[aryIdx].release != NULL) {
            (*arrays[aryIdx].release)(env, &arrays[aryIdx]);
        }
    }
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
    FFIValue retval; \
    jlong v[] = { N##n }

#define FREE_ARRAYS(env, arrays, arrayCount) do { \
    int aryIdx; \
    for (aryIdx = arrayCount; aryIdx >= 0; aryIdx--) { \
        if (arrays[aryIdx].release != NULL) (*arrays[aryIdx].release)(env, &arrays[aryIdx]); \
    } \
} while(0)

#define END \
    error: \
        RELEASE_ARRAYS(env, arrays, arrayCount); \
        return RETVAL(retval, ctx->cif.rtype)

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
    } else if (!object_to_ptr(env, obj, off, len, flags, (void **) ARGPTR(&v[idx], ctx->cif.arg_types[idx]), arrays, &arrayCount, pinned, &pinnedCount)) { \
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
    call##n(ctx, j2p(function), &retval, args); \
    END

#define CALL1 CALL(1, v[0])
#define CALL2 CALL(2, v[0], v[1])
#define CALL3 CALL(3, v[0], v[1], v[2])
#define CALL4 CALL(4, v[0], v[1], v[2], v[3])
#define CALL5 CALL(5, v[0], v[1], v[2], v[3], v[4])
#define CALL6 CALL(6, v[0], v[1], v[2], v[3], v[4], v[5])

#if defined(__OPTIMIZE_SIZE__) || 1

#define OBJPARAM1 \
    ObjectParam objects[] = { \
        { o1, o1off, o1len, o1flags } \
    }

#define OBJPARAM2 \
    ObjectParam objects[] = { \
        { o1, o1off, o1len, o1flags }, \
        { o2, o2off, o2len, o2flags } \
    }

#define OBJPARAM3 \
    ObjectParam objects[] = { \
        { o1, o1off, o1len, o1flags }, \
        { o2, o2off, o2len, o2flags }, \
        { o3, o3off, o3len, o3flags } \
    }

#define INVOKE(n, o) \
    OBJPARAM##o; \
    return invoke##n(env, ctxAddress, function, N##n, objects, o);

  
#define IMPL(n) \
    INIT(n); \
    int objIdx; \
    for (objIdx = 0; objIdx < nobjects; objIdx++) { \
        ADDOBJ(objects[objIdx].object, objects[objIdx].offset, objects[objIdx].length, \
                objects[objIdx].flags); \
    } \
    CALL##n;

static jlong 
invoke1(JNIEnv* env, jlong ctxAddress, jlong function, 
        jlong n1,
        ObjectParam* objects, int nobjects)
{
    IMPL(1);
}

static jlong 
invoke2(JNIEnv* env, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2,
        ObjectParam* objects, int nobjects)
{
    IMPL(2);
}

static jlong 
invoke3(JNIEnv* env, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3,
        ObjectParam* objects, int nobjects)
{
    IMPL(3);
}

static jlong 
invoke4(JNIEnv* env, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4,
        ObjectParam* objects, int nobjects)
{
    IMPL(4);
}

static jlong 
invoke5(JNIEnv* env, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5,
        ObjectParam* objects, int nobjects)
{
    IMPL(5);
}

static jlong 
invoke6(JNIEnv* env, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5, jlong n6,
        ObjectParam* objects, int nobjects)
{
    IMPL(6);
}

#else

#define ADDOBJ1 \
    ADDOBJ(o1, o1off, o1len, o1flags)

#define ADDOBJ2 \
    ADDOBJ1; ADDOBJ(o2, o2off, o2len, o2flags)

#define ADDOBJ3 \
    ADDOBJ2; ADDOBJ(o3, o3off, o3len, o3flags)


# define INVOKE(n, o) \
    INIT(n); \
    ADDOBJ##o; \
    CALL##n

#endif

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNO1rN
 * Signature: (JJJLjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN1O1rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1,
        jobject o1, jint o1flags, jint o1off, jint o1len)
{
    INVOKE(1, 1);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNO1rN
 * Signature: (JJJJLjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN2O1rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2,
        jobject o1, jint o1flags, jint o1off, jint o1len)
{
    INVOKE(2, 1);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNO2rN
 * Signature: (JJJJLjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN2O2rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong n1, jlong n2,
        jobject o1, jint o1flags, jint o1off, jint o1len,
        jobject o2, jint o2flags, jint o2off, jint o2len)
{
    INVOKE(2, 2);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNO1rN
 * Signature: (JJJJJLjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN3O1rN(JNIEnv* env, jobject self, 
        jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, 
        jobject o1, jint o1flags, jint o1off, jint o1len)
{
    INVOKE(3, 1);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNO2rN
 * Signature: (JJJJJLjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN3O2rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3,
        jobject o1, jint o1flags, jint o1off, jint o1len,
        jobject o2, jint o2flags, jint o2off, jint o2len)
{
    INVOKE(3, 2);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNO3rN
 * Signature: (JJLjava/lang/Object;IIILjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN3O3rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong n1, jlong n2, jlong n3,
        jobject o1, jint o1flags, jint o1off, jint o1len,
        jobject o2, jint o2flags, jint o2off, jint o2len,
        jobject o3, jint o3flags, jint o3off, jint o3len)
{
    INVOKE(3, 3);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNNO1rN
 * Signature: (JJJJJJLjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN4O1rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4,
        jobject o1, jint o1flags, jint o1off, jint o1len)
{
    INVOKE(4, 1);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNNO2rN
 * Signature: (JJJJJJLjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN4O2rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4,
        jobject o1, jint o1flags, jint o1off, jint o1len,
        jobject o2, jint o2flags, jint o2off, jint o2len)
{
    INVOKE(4, 2);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNNO3rN
 * Signature: (JJJJJJLjava/lang/Object;IIILjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN4O3rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4,
        jobject o1, jint o1flags, jint o1off, jint o1len,
        jobject o2, jint o2flags, jint o2off, jint o2len,
        jobject o3, jint o3flags, jint o3off, jint o3len)
{
    INVOKE(4, 3);
}


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNNNO1rN
 * Signature: (JJJJJJJLjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN5O1rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5,
        jobject o1, jint o1flags, jint o1off, jint o1len)
{
    INVOKE(5, 1);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNNNO2rN
 * Signature: (JJJJJJJLjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN5O2rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5,
        jobject o1, jint o1flags, jint o1off, jint o1len,
        jobject o2, jint o2flags, jint o2off, jint o2len)
{
    INVOKE(5, 2);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNNNO3rN
 * Signature: (JJJJJJJLjava/lang/Object;IIILjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN5O3rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5,
        jobject o1, jint o1flags, jint o1off, jint o1len,
        jobject o2, jint o2flags, jint o2off, jint o2len,
        jobject o3, jint o3flags, jint o3off, jint o3len)
{
    INVOKE(5, 3);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNNNNO1rN
 * Signature: (JJJJJJJJLjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN6O1rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5, jlong n6,
        jobject o1, jint o1flags, jint o1off, jint o1len)
{
    INVOKE(6, 1);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNNNNO2rN
 * Signature: (JJJJJJJJLjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN6O2rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5, jlong n6,
        jobject o1, jint o1flags, jint o1off, jint o1len,
        jobject o2, jint o2flags, jint o2off, jint o2len)
{
    INVOKE(6, 2);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeNNNNNNO3rN
 * Signature: (JJJJJJJJLjava/lang/Object;IIILjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL 
Java_com_kenai_jffi_Foreign_invokeN6O3rN(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, 
        jlong n1, jlong n2, jlong n3, jlong n4, jlong n5, jlong n6,
        jobject o1, jint o1flags, jint o1off, jint o1len,
        jobject o2, jint o2flags, jint o2off, jint o2len,
        jobject o3, jint o3flags, jint o3off, jint o3len)
{
    INVOKE(6, 3);
}

