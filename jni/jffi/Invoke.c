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
#include "Function.h"
#include "Array.h"
#include "LastError.h"
#include "com_kenai_jffi_Foreign.h"

#define PARAM_SIZE (8)

#define MAX_STACK_ARRAY (1024)

#ifdef USE_RAW
static void
invokeArray(JNIEnv* env, jlong ctxAddress, jbyteArray paramBuffer, void* returnBuffer)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue tmpValue;
    jbyte *tmpBuffer = (jbyte *) &tmpValue;
    
    if (likely(ctx->cif.nargs > 0)) {
        tmpBuffer = alloca(ctx->cif.bytes);
        
        (*env)->GetByteArrayRegion(env, paramBuffer, 0, ctx->rawParameterSize, tmpBuffer);
    }

    ffi_raw_call(&ctx->cif, FFI_FN(ctx->function), returnBuffer, (ffi_raw *) tmpBuffer);
    SAVE_ERRNO(ctx);
}

#else
static void
invokeArray(JNIEnv* env, jlong ctxAddress, jbyteArray paramBuffer, void* returnBuffer)
{
    Function* ctx = (Function *) j2p(ctxAddress);

    void** ffiArgs = { NULL };
    jbyte *tmpBuffer = NULL;
    
    if (ctx->cif.nargs > 0) {
        unsigned int i;
        tmpBuffer = alloca(ctx->cif.nargs * PARAM_SIZE);
        ffiArgs = alloca(ctx->cif.nargs * sizeof(void *));
        
        (*env)->GetByteArrayRegion(env, paramBuffer, 0, ctx->cif.nargs * PARAM_SIZE, tmpBuffer);

        for (i = 0; i < ctx->cif.nargs; ++i) {
            if (unlikely(ctx->cif.arg_types[i]->type == FFI_TYPE_STRUCT)) {
                ffiArgs[i] = *(void **) &tmpBuffer[i * PARAM_SIZE];
            } else {
                ffiArgs[i] = &tmpBuffer[i * PARAM_SIZE];
            }
        }
    }

    ffi_call(&ctx->cif, FFI_FN(ctx->function), returnBuffer, ffiArgs);

    SAVE_ERRNO(ctx);
}
#endif
/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    isRawParameterPackingEnabled
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL
Java_com_kenai_jffi_Foreign_isRawParameterPackingEnabled(JNIEnv* env, jobject self)
{
#ifdef USE_RAW
    return JNI_TRUE;
#else
    return JNI_FALSE;
#endif
}
/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayInt32
 * Signature: (J[B)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnInt(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArray(env, ctxAddress, paramBuffer, &retval);
    return_int(retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayInt64
 * Signature: (J[B)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnLong(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArray(env, ctxAddress, paramBuffer, &retval);
    return retval.s64;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayFloat
 * Signature: (J[B)F
 */
JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnFloat(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArray(env, ctxAddress, paramBuffer, &retval);
    return retval.f;
}
/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayDouble
 * Signature: (J[B)D
 */
JNIEXPORT jdouble JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnDouble(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArray(env, ctxAddress, paramBuffer, &retval);
    return retval.d;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayReturnStruct
 * Signature: (J[B[B)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayReturnStruct(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer, jbyteArray returnBuffer, jint offset)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    jbyte* retval = alloca(ctx->cif.rtype->size);
    jbyte* tmpBuffer;
    void** ffiArgs;
    int i;

    //
    // Due to the undocumented and somewhat strange struct-return handling when
    // using ffi_raw_call(), we convert from raw to ptr array, then call via normal
    // ffi_call
    //

    ffiArgs = alloca(ctx->cif.nargs * sizeof(void *));

#ifdef USE_RAW
    tmpBuffer = alloca(ctx->rawParameterSize);
    (*env)->GetByteArrayRegion(env, paramBuffer, 0, ctx->rawParameterSize, tmpBuffer);
    for (i = 0; i < (int) ctx->cif.nargs; ++i) {
        ffiArgs[i] = (tmpBuffer + ctx->rawParamOffsets[i]);
    }
#else
    tmpBuffer = alloca(ctx->cif.nargs * PARAM_SIZE);
    (*env)->GetByteArrayRegion(env, paramBuffer, 0, ctx->cif.nargs * PARAM_SIZE, tmpBuffer);
    for (i = 0; i < (int) ctx->cif.nargs; ++i) {
        ffiArgs[i] = &tmpBuffer[i * PARAM_SIZE];
    }
#endif
    ffi_call(&ctx->cif, FFI_FN(ctx->function), retval, ffiArgs);
    SAVE_ERRNO(ctx);
    (*env)->SetByteArrayRegion(env, returnBuffer, offset, ctx->cif.rtype->size, retval);
}

#define MAX_STACK_OBJECTS (4)

static void
invokeArrayWithObjects_(JNIEnv* env, jlong ctxAddress, jbyteArray paramBuffer,
        jint objectCount, jint* infoBuffer, jobject* objectBuffer, void* retval)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    jbyte *tmpBuffer = NULL;
    void **ffiArgs = NULL;
    Array *arrays = NULL;
    unsigned int i, arrayCount = 0, paramBytes = 0;

    arrays = alloca(objectCount * sizeof(Array));

#if defined(USE_RAW)
    paramBytes = ctx->rawParameterSize;
#else
    paramBytes = ctx->cif.nargs * PARAM_SIZE;
#endif

    tmpBuffer = alloca(paramBytes);
    (*env)->GetByteArrayRegion(env, paramBuffer, 0, paramBytes, tmpBuffer);

#ifndef USE_RAW
    ffiArgs = alloca(ctx->cif.nargs * sizeof(void *));
    
    for (i = 0; i < (unsigned int) ctx->cif.nargs; ++i) {
        ffiArgs[i] = &tmpBuffer[i * PARAM_SIZE];
    }
#endif    

    
    for (i = 0; i < (unsigned int) objectCount; ++i) {
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
                
                } else if (unlikely((type & com_kenai_jffi_ObjectBuffer_PINNED) != 0)) {

                    ptr = jffi_getArrayCritical(env, object, offset, length, type, &arrays[arrayCount]);
                    if (unlikely(ptr == NULL)) {
                        goto cleanup;
                    }
                } else if (true && likely(length < MAX_STACK_ARRAY)) {

                    ptr = alloca(jffi_arraySize(length + 1, type));
                    if (unlikely(jffi_getArrayBuffer(env, object, offset, length, type,
                        &arrays[arrayCount], ptr) == NULL)) {
                        goto cleanup;
                    }

                } else {
                    ptr = jffi_getArrayHeap(env, object, offset, length, type, &arrays[arrayCount]);
                    if (unlikely(ptr == NULL)) {
                        goto cleanup;
                    }
                }
                
                ++arrayCount;
                break;

            case com_kenai_jffi_ObjectBuffer_BUFFER:
                ptr = (*env)->GetDirectBufferAddress(env, object);
                if (unlikely(ptr == NULL)) {
                    throwException(env, NullPointer, "Could not get direct Buffer address");
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

#if defined(USE_RAW)
        *((void **)(tmpBuffer + ctx->rawParamOffsets[idx])) = ptr;
#else
        if (unlikely(ctx->cif.arg_types[idx]->type == FFI_TYPE_STRUCT)) {
            ffiArgs[idx] = ptr;
        } else {
            *((void **) ffiArgs[idx]) = ptr;
        }
#endif
    }
#if defined(USE_RAW)
    //
    // Special case for struct return values - unroll into a ptr array and
    // use ffi_call, since ffi_raw_call with struct return values is undocumented.
    //
    if (unlikely(ctx->cif.rtype->type == FFI_TYPE_STRUCT)) {
        ffiArgs = alloca(ctx->cif.nargs * sizeof(void *));
        for (i = 0; i < ctx->cif.nargs; ++i) {
            ffiArgs[i] = (tmpBuffer + ctx->rawParamOffsets[i]);
        }
        ffi_call(&ctx->cif, FFI_FN(ctx->function), retval, ffiArgs);
    } else {
        ffi_raw_call(&ctx->cif, FFI_FN(ctx->function), retval, (ffi_raw *) tmpBuffer);
    }
#else
    ffi_call(&ctx->cif, FFI_FN(ctx->function), retval, ffiArgs);
#endif
    SAVE_ERRNO(ctx);
cleanup:
    /* Release any array backing memory */
    for (i = 0; i < arrayCount; ++i) {
        if (arrays[i].release != NULL) {
            //printf("releasing array=%p\n", arrays[i].elems);
            (*arrays[i].release)(env, &arrays[i]);
        }
    }
}

static void
invokeArrayWithObjects(JNIEnv* env, jlong ctxAddress, jbyteArray paramBuffer,
        jint objectCount, jintArray objectInfo, jobjectArray objectArray, void* retval)
{
    jint* infoBuffer = alloca(objectCount * sizeof(jint) * 3);
    jobject* objectBuffer = alloca(objectCount * sizeof(jobject));
    int i;

    (*env)->GetIntArrayRegion(env, objectInfo, 0, objectCount * 3, infoBuffer);
    for (i = 0; i < objectCount; ++i) {
        objectBuffer[i] = (*env)->GetObjectArrayElement(env, objectArray, i);
    }

    invokeArrayWithObjects_(env, ctxAddress, paramBuffer, objectCount, infoBuffer, objectBuffer, retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsInt32
 * Signature: (J[B[I[Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsInt32(JNIEnv* env, jobject self,
        jlong ctxAddress, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo, jobjectArray objectArray)
{
    FFIValue retval;
    invokeArrayWithObjects(env, ctxAddress, paramBuffer, objectCount, objectInfo, objectArray, &retval);
    return_int(retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayO1Int32
 * Signature: (J[BILjava/lang/Object;I)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayO1Int32(JNIEnv* env, jobject self, jlong ctxAddress,
        jbyteArray paramBuffer, jobject o1, jint o1info, jint o1off, jint o1len)
{
    FFIValue retval;
    jint info[] = { o1info, o1off, o1len };
    jobject objects[] = { o1 };
    invokeArrayWithObjects_(env, ctxAddress, paramBuffer, 1, info, objects, &retval);
    return_int(retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayO2Int32
 * Signature: (J[BLjava/lang/Object;IIILjava/lang/Object;III)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayO2Int32(JNIEnv* env, jobject self, jlong ctxAddress,
        jbyteArray paramBuffer, jobject o1, jint o1info, jint o1off, jint o1len,
        jobject o2, jint o2info, jint o2off, jint o2len)
{
    FFIValue retval;
    jint info[] = { o1info, o1off, o1len, o2info, o2off, o2len };
    jobject objects[] = { o1, o2 };
    invokeArrayWithObjects_(env, ctxAddress, paramBuffer, 2, info, objects, &retval);
    return_int(retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsInt64
 * Signature: (J[BI[I[Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsInt64(JNIEnv* env, jobject self,
        jlong ctxAddress, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo, jobjectArray objectArray)
{
    FFIValue retval;
    invokeArrayWithObjects(env, ctxAddress, paramBuffer, objectCount, objectInfo, objectArray, &retval);
    return retval.s64;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayO1Int64
 * Signature: (J[BLjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayO1Int64(JNIEnv* env, jobject self, jlong ctxAddress,
        jbyteArray paramBuffer, jobject o1, jint o1info, jint o1off, jint o1len)
{
    FFIValue retval;
    jint info[] = { o1info, o1off, o1len };
    jobject objects[] = { o1 };

    invokeArrayWithObjects_(env, ctxAddress, paramBuffer, 1, info, objects, &retval);

    return retval.j;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayO2Int64
 * Signature: (J[BLjava/lang/Object;IIILjava/lang/Object;III)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayO2Int64(JNIEnv* env, jobject self, jlong ctxAddress,
        jbyteArray paramBuffer, jobject o1, jint o1info, jint o1off, jint o1len,
        jobject o2, jint o2info, jint o2off, jint o2len)
{
    FFIValue retval;
    jint info[] = { o1info, o1off, o1len, o2info, o2off, o2len };
    jobject objects[] = { o1, o2 };

    invokeArrayWithObjects_(env, ctxAddress, paramBuffer, 2, info, objects, &retval);

    return retval.j;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsFloat
 * Signature: (J[BI[I[Ljava/lang/Object;)F
 */
JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsFloat(JNIEnv* env, jobject self,
        jlong ctxAddress, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo, jobjectArray objectArray)
{
    FFIValue retval;
    invokeArrayWithObjects(env, ctxAddress, paramBuffer, objectCount, objectInfo, objectArray, &retval);
    return retval.f;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsDouble
 * Signature: (J[BI[I[Ljava/lang/Object;)D
 */
JNIEXPORT jdouble JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsDouble(JNIEnv* env, jobject self,
        jlong ctxAddress, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo, jobjectArray objectArray)
{
    FFIValue retval;
    invokeArrayWithObjects(env, ctxAddress, paramBuffer, objectCount, objectInfo, objectArray, &retval);
    return retval.d;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayWithObjectsReturnStruct
 * Signature: (J[BI[I[Ljava/lang/Object;[BI)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayWithObjectsReturnStruct(JNIEnv* env, jobject self,
       jlong ctxAddress, jbyteArray paramBuffer, jint objectCount, jintArray objectInfo,
       jobjectArray objectArray, jbyteArray returnBuffer, jint returnBufferOffset)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    jbyte* retval = alloca(ctx->cif.rtype->size);
    
    invokeArrayWithObjects(env, ctxAddress, paramBuffer, objectCount, objectInfo, objectArray, retval);
    (*env)->SetByteArrayRegion(env, returnBuffer, returnBufferOffset, ctx->cif.rtype->size, retval);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokePointerParameterArray
 * Signature: (JJ[J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_invokePointerParameterArray(JNIEnv *env, jobject self, jlong ctxAddress,
        jlong returnBuffer, jlongArray parameterArray)
{
    Function* ctx = (Function *) j2p(ctxAddress);
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
    
    ffi_call(&ctx->cif, FFI_FN(ctx->function), j2p(returnBuffer), ffiArgs);
}
