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
#include <stdlib.h>
#if defined (__sun) || defined(_AIX)
#  include <alloca.h>
#endif
#ifdef _WIN32
#  include <malloc.h>
#endif
#include <errno.h>
#include <ffi.h>
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "CallContext.h"
#include "Array.h"
#include "LastError.h"
#include "FaultProtect.h"
#include "com_kenai_jffi_Foreign.h"

#define PARAM_SIZE (8)

#define MAX_STACK_ARRAY (1024)

typedef struct Pinned {
    jobject object;
    jsize offset;
    jsize length;
    int type;
} Pinned;

#  define COPY_ARGS(ctx, src, ffiArgs) do { \
    int idx; \
    for (idx = 0; idx < (int) ctx->cif.nargs; ++idx) { \
        if (unlikely(ctx->cif.arg_types[idx]->type == FFI_TYPE_STRUCT)) { \
            ffiArgs[idx] = *(void **) &src[idx * PARAM_SIZE]; \
        } else { \
            ffiArgs[idx] = &src[idx * PARAM_SIZE]; \
        } \
    } \
} while (0)
#  define ARG_BUFFER_SIZE(ctx) ((ctx)->cif.nargs * PARAM_SIZE)

static void
invokeArrayWithObjects_(JNIEnv* env, jlong ctxAddress, jlong function, jbyteArray paramBuffer,
			jint objectCount, jint* infoBuffer, jobject* objectBuffer, void* retval);


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    isRawParameterPackingEnabled
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_com_kenai_jffi_Foreign_isRawParameterPackingEnabled(JNIEnv* env, jobject self)
{
    return JNI_FALSE;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayInt32
 * Signature: (J[B)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnInt(JNIEnv* env, jclass self, jlong ctxAddress, jlong function,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, 0, NULL, NULL, &retval);
    return_int(retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayInt64
 * Signature: (J[B)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnLong(JNIEnv* env, jclass self, jlong ctxAddress, jlong function,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, 0, NULL, NULL, &retval);
    return retval.s64;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayFloat
 * Signature: (J[B)F
 */
JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnFloat(JNIEnv* env, jclass self, jlong ctxAddress, jlong function,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, 0, NULL, NULL, &retval);
    return retval.f;
}
/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayDouble
 * Signature: (J[B)D
 */
JNIEXPORT jdouble JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnDouble(JNIEnv* env, jclass self, jlong ctxAddress, jlong function,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, 0, NULL, NULL, &retval);
    return retval.d;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayReturnStruct
 * Signature: (J[B[B)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnStruct(JNIEnv* env, jclass self, jlong ctxAddress, jlong function,
        jbyteArray paramBuffer, jbyteArray returnBuffer, jint offset)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    jbyte* retval = alloca(ctx->cif.rtype->size);

    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, 0, NULL, NULL, retval);
    (*env)->SetByteArrayRegion(env, returnBuffer, offset, ctx->cif.rtype->size, retval);
}

static void
invokeArrayWithObjects_(JNIEnv* env, jlong ctxAddress, jlong function, jbyteArray paramBuffer,
        jint objectCount, jint* infoBuffer, jobject* objectBuffer, void* retval)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    void **ffiArgs = { NULL };
    Array *arrays = NULL;
    Pinned *pinned = NULL;
    int i, arrayCount = 0, pinnedCount = 0, paramBytes = 0;

    if (unlikely(objectCount > 0)) {
        arrays = alloca(objectCount * sizeof(Array));
        pinned = alloca(objectCount * sizeof(Pinned));
    }

    if (ctx->cif.nargs > 0) {
        jbyte* tmpBuffer = alloca(ARG_BUFFER_SIZE(ctx));
        (*env)->GetByteArrayRegion(env, paramBuffer, 0, ARG_BUFFER_SIZE(ctx), tmpBuffer);
        ffiArgs = alloca(ctx->cif.nargs * sizeof(void *));
        COPY_ARGS(ctx, tmpBuffer, ffiArgs);
    }
    
    for (i = 0; i < objectCount; ++i) {
        int type = infoBuffer[i * 3];
        jsize offset = infoBuffer[(i * 3) + 1];
        jsize length = infoBuffer[(i * 3) + 2];
        jobject object = objectBuffer[i];
        int idx = (type & com_kenai_jffi_ObjectBuffer_INDEX_MASK) >> com_kenai_jffi_ObjectBuffer_INDEX_SHIFT;
        void* ptr;

        switch (type & com_kenai_jffi_ObjectBuffer_TYPE_MASK & ~com_kenai_jffi_ObjectBuffer_PRIM_MASK) {
            case com_kenai_jffi_ObjectBuffer_ARRAY:
                if (unlikely(object == NULL)) {
                    throwException(env, NullPointer, "null object for parameter %d", idx);
                    goto cleanup;
                }
                
                if (unlikely((type & com_kenai_jffi_ObjectBuffer_PINNED) != 0)) {

                    // Record the pinned array, but the actual pinning will be done just before the ffi_call
                    Pinned* p = &pinned[pinnedCount++];
                    p->object = object;
                    p->offset = offset;
                    p->length = length;
                    p->type = type;
                    ptr = NULL;
                    
                } else if (likely(length < MAX_STACK_ARRAY)) {

                    ptr = alloca(jffi_arraySize(length + 1, type));
                    if (unlikely(jffi_getArrayBuffer(env, object, offset, length, type,
                        &arrays[arrayCount], ptr) == NULL)) {
                        goto cleanup;
                    }
                    ++arrayCount;
                    

                } else {
                    ptr = jffi_getArrayHeap(env, object, offset, length, type, &arrays[arrayCount]);
                    if (unlikely(ptr == NULL)) {
                        goto cleanup;
                    }
                    ++arrayCount;
                }
                
                
                break;

            case com_kenai_jffi_ObjectBuffer_BUFFER:
                if (unlikely(object == NULL)) {
                    throwException(env, NullPointer, "null object for parameter %d", idx);
                    goto cleanup;
                }
                
                ptr = (*env)->GetDirectBufferAddress(env, object);
                if (unlikely(ptr == NULL)) {
                    throwException(env, NullPointer, "null direct buffer address for parameter %d", idx);
                    goto cleanup;
                }
                ptr = ((char *) ptr + offset);
                break;

            case com_kenai_jffi_ObjectBuffer_JNI:
                switch (type & com_kenai_jffi_ObjectBuffer_TYPE_MASK) {
                    case com_kenai_jffi_ObjectBuffer_JNIENV:
                        ptr = env;
                        break;
                    case com_kenai_jffi_ObjectBuffer_JNIOBJECT:
                        ptr = (void *) object;
                        break;
                    default:
                        throwException(env, IllegalArgument, "Unsupported object type: %#x",
                            type & com_kenai_jffi_ObjectBuffer_TYPE_MASK);
                        goto cleanup;
                }
                
                break;
            default:
                throwException(env, IllegalArgument, "Unsupported object type: %#x", 
                        type & com_kenai_jffi_ObjectBuffer_TYPE_MASK);
                goto cleanup;
        }

	if (likely(ctx->cif.arg_types[idx]->type == FFI_TYPE_POINTER)) {
            *((void **) ffiArgs[idx]) = ptr;
	} else {
            ffiArgs[idx] = ptr;
	}
    }
    
    //
    // Pin all the arrays just before calling the native function.
    //
    // Although hotspot allows it, other JVMs do not allow JNI operations
    // once any array has been pinned, so pinning must be done last, just before 
    // the native function is called.
    // 
    for (i = 0; i < pinnedCount; i++) {
        Pinned* p = &pinned[i];
        Array* ary = &arrays[arrayCount];    
        int idx = (p->type & com_kenai_jffi_ObjectBuffer_INDEX_MASK) >> com_kenai_jffi_ObjectBuffer_INDEX_SHIFT;
        
        void* ptr = jffi_getArrayCritical(env, p->object, p->offset, p->length, p->type, &arrays[arrayCount]);
        if (unlikely(ptr == NULL)) {
            goto cleanup;
        }

       	if (likely(ctx->cif.arg_types[idx]->type == FFI_TYPE_POINTER)) {
            *((void **) ffiArgs[idx]) = ptr;
	} else {
            ffiArgs[idx] = ptr;
	}

        ++arrayCount;
    }

    FAULTPROT_CTX(env, ctx, ffi_call(&ctx->cif, FFI_FN(j2p(function)), retval, ffiArgs), );

cleanup:
    /* Release any array backing memory */
    RELEASE_ARRAYS(env, arrays, arrayCount);
}

static void
invokeArrayWithObjects(JNIEnv* env, jlong ctxAddress, jlong function, jbyteArray paramBuffer,
        jint objectCount, jintArray objectInfo, jobjectArray objectArray, void* retval)
{
    jint* infoBuffer = alloca(objectCount * sizeof(jint) * 3);
    jobject* objectBuffer = alloca(objectCount * sizeof(jobject));
    int i;

    (*env)->GetIntArrayRegion(env, objectInfo, 0, objectCount * 3, infoBuffer);
    for (i = 0; i < objectCount; ++i) {
        objectBuffer[i] = (*env)->GetObjectArrayElement(env, objectArray, i);
    }

    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, objectCount, infoBuffer, objectBuffer, retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsInt32
 * Signature: (J[B[I[Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsInt32(JNIEnv* env, jobject self,
        jlong ctxAddress, jlong function, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo, jobjectArray objectArray)
{
    FFIValue retval;
    invokeArrayWithObjects(env, ctxAddress, function, paramBuffer, objectCount, objectInfo, objectArray, &retval);
    return_int(retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayO1Int32
 * Signature: (J[BILjava/lang/Object;I)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayO1Int32(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jbyteArray paramBuffer, jobject o1, jint o1info, jint o1off, jint o1len)
{
    FFIValue retval;
    jint info[] = { o1info, o1off, o1len };
    jobject objects[] = { o1 };
    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, 1, info, objects, &retval);
    return_int(retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayO2Int32
 * Signature: (J[BLjava/lang/Object;IIILjava/lang/Object;III)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayO2Int32(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jbyteArray paramBuffer, jobject o1, jint o1info, jint o1off, jint o1len,
        jobject o2, jint o2info, jint o2off, jint o2len)
{
    FFIValue retval;
    jint info[] = { o1info, o1off, o1len, o2info, o2off, o2len };
    jobject objects[] = { o1, o2 };
    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, 2, info, objects, &retval);
    return_int(retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsInt64
 * Signature: (J[BI[I[Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsInt64(JNIEnv* env, jobject self,
        jlong ctxAddress, jlong function, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo, jobjectArray objectArray)
{
    FFIValue retval;
    invokeArrayWithObjects(env, ctxAddress, function, paramBuffer, objectCount, objectInfo, objectArray, &retval);
    return retval.s64;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayO1Int64
 * Signature: (J[BLjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayO1Int64(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jbyteArray paramBuffer, jobject o1, jint o1info, jint o1off, jint o1len)
{
    FFIValue retval;
    jint info[] = { o1info, o1off, o1len };
    jobject objects[] = { o1 };

    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, 1, info, objects, &retval);

    return retval.j;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayO2Int64
 * Signature: (J[BLjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayO2Int64(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jbyteArray paramBuffer, jobject o1, jint o1info, jint o1off, jint o1len,
        jobject o2, jint o2info, jint o2off, jint o2len)
{
    FFIValue retval;
    jint info[] = { o1info, o1off, o1len, o2info, o2off, o2len };
    jobject objects[] = { o1, o2 };

    invokeArrayWithObjects_(env, ctxAddress, function, paramBuffer, 2, info, objects, &retval);

    return retval.j;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsFloat
 * Signature: (J[BI[I[Ljava/lang/Object;)F
 */
JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsFloat(JNIEnv* env, jobject self,
        jlong ctxAddress, jlong function, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo, jobjectArray objectArray)
{
    FFIValue retval;
    invokeArrayWithObjects(env, ctxAddress, function, paramBuffer, objectCount, objectInfo, objectArray, &retval);
    return retval.f;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsDouble
 * Signature: (J[BI[I[Ljava/lang/Object;)D
 */
JNIEXPORT jdouble JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsDouble(JNIEnv* env, jobject self,
        jlong ctxAddress, jlong function, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo, jobjectArray objectArray)
{
    FFIValue retval;
    invokeArrayWithObjects(env, ctxAddress, function, paramBuffer, objectCount, objectInfo, objectArray, &retval);
    return retval.d;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsReturnStruct
 * Signature: (J[BI[I[Ljava/lang/Object;[BI)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsReturnStruct(JNIEnv* env, jobject self,
       jlong ctxAddress, jlong function, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo,
       jobjectArray objectArray, jbyteArray returnBuffer, jint returnBufferOffset)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    jbyte* retval = alloca(ctx->cif.rtype->size);
    
    invokeArrayWithObjects(env, ctxAddress, function, paramBuffer, objectCount, objectInfo, objectArray, retval);
    (*env)->SetByteArrayRegion(env, returnBuffer, returnBufferOffset, ctx->cif.rtype->size, retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokePointerParameterArray
 * Signature: (JJ[J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_invokePointerParameterArray(JNIEnv *env, jobject self, jlong ctxAddress, jlong function,
        jlong returnBuffer, jlongArray parameterArray)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    int parameterCount;
    jlong* params = NULL;
    void** ffiArgs = NULL;
    int i;

    if (unlikely(ctxAddress == 0LL)) {
        throwException(env, NullPointer, "context address is null");
        return;
    }

    if (unlikely(returnBuffer == 0LL)) {
        throwException(env, NullPointer, "result buffer is null");
        return;
    }

    if (unlikely(parameterArray == NULL)) {
        throwException(env, NullPointer, "parameter array is null");
        return;
    }

    parameterCount = (*env)->GetArrayLength(env, parameterArray);
    if (parameterCount > 0) {
         params = alloca(parameterCount * sizeof(jlong));
         ffiArgs = alloca(parameterCount * sizeof(void *));
        (*env)->GetLongArrayRegion(env, parameterArray, 0, parameterCount, params);
        for (i = 0; i < parameterCount; ++i) {
            ffiArgs[i] = j2p(params[i]);
        }
    }
    
    ffi_call(&ctx->cif, FFI_FN(j2p(function)), j2p(returnBuffer), ffiArgs);
}
