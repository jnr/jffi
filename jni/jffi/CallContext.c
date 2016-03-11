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

#include <sys/param.h>
#include <sys/types.h>
#include <stdint.h>
#include <stdlib.h>
#if defined(__sun) || defined(_AIX)
#  include <sys/sysmacros.h>
#  include <alloca.h>
#endif
#ifdef _WIN32
#  include <malloc.h>
#endif
#include <ffi.h>
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "CallContext.h"
#include "com_kenai_jffi_Foreign.h"

#ifndef MAX
#  define MAX(a,b) ((a) > (b) ? (a) : (b))
#endif

static inline int FFI_ALIGN(int v, int a) {
    return ((((size_t) v) - 1) | (a - 1)) +1;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    newCallContext
 * Signature: (I[II)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_newCallContext(JNIEnv* env, jobject self,
        jlong returnType, jlongArray paramArray, jint flags)
{
    CallContext* ctx = NULL;
    jlong* paramTypes;
    int paramCount, i, rawOffset = 0;
    bool isFastInt = false, isFastLong = false;
    ffi_type* ffiParamTypes;
    int ffiStatus;
    int abi;

    paramCount = (*env)->GetArrayLength(env, paramArray);
    ctx = calloc(1, sizeof(*ctx));
    if (ctx == NULL) {
        throwException(env, OutOfMemory, "Failed to allocate CallContext");
        goto cleanup;
    }
    ctx->ffiParamTypes = calloc(MAX(1, paramCount), sizeof(ffi_type *));
    if (ctx->ffiParamTypes == NULL) {
        throwException(env, OutOfMemory, "Failed to allocate CallContext#ffiParamTypes");
        goto cleanup;
    }
    ctx->rawParamOffsets = calloc(MAX(1, paramCount), sizeof(*ctx->rawParamOffsets));
    if (ctx->rawParamOffsets == NULL) {
        throwException(env, OutOfMemory, "Failed to allocate CallContext#rawParamOffsets");
        goto cleanup;
    }

    paramTypes = alloca(paramCount * sizeof(jlong));
    (*env)->GetLongArrayRegion(env, paramArray, 0, paramCount, paramTypes);

    ctx->resultMask = (((ffi_type *) j2p(returnType))->size > 4) ? ~0UL : 0xffffffffUL;
#if defined(__i386__) || defined(__x86_64__) 
    isFastInt = true;
    isFastLong = true;

    switch (((ffi_type *) j2p(returnType))->type) {
        case FFI_TYPE_INT:
        case FFI_TYPE_SINT8:
        case FFI_TYPE_UINT8:
        case FFI_TYPE_SINT16:
        case FFI_TYPE_UINT16:
        case FFI_TYPE_SINT32:
        case FFI_TYPE_UINT32:
#if defined(__i386__)
        case FFI_TYPE_POINTER:
#endif
#if !defined(__x86_64__)
            isFastLong = false;
#endif
            break;

        case FFI_TYPE_SINT64:
        case FFI_TYPE_UINT64:
#if defined(__x86_64__)
        case FFI_TYPE_POINTER:
#endif
            isFastInt = false;
            break;

        case FFI_TYPE_VOID:
            break;

        default:
            isFastInt = false;
            isFastLong = false;
            break;
    }
#endif

    for (i = 0; i < paramCount; ++i) {
        ffi_type* type = (ffi_type *) j2p(paramTypes[i]);
        if (type == NULL) {
            throwException(env, IllegalArgument, "Invalid parameter type: %#x", paramTypes[i]);
            goto cleanup;
        }
        ctx->ffiParamTypes[i] = type;
        ctx->rawParamOffsets[i] = rawOffset;
        rawOffset += FFI_ALIGN(type->size, FFI_SIZEOF_ARG);
#if defined(__i386__) || defined(__x86_64__) 
        switch (type->type) {
            case FFI_TYPE_INT:
            case FFI_TYPE_SINT8:
            case FFI_TYPE_UINT8:
            case FFI_TYPE_SINT16:
            case FFI_TYPE_UINT16:
            case FFI_TYPE_SINT32:
            case FFI_TYPE_UINT32:
#if defined(__i386__)
            case FFI_TYPE_POINTER:
#endif

#if !defined(__x86_64__)
                isFastLong = false;
#endif
                break;

            case FFI_TYPE_SINT64:
            case FFI_TYPE_UINT64:
#if defined(__x86_64__)
            case FFI_TYPE_POINTER:
#endif
                isFastInt = false;
                break;

            default:
                isFastInt = false;
                isFastLong = false;
                break;
        }
#endif
    }

    // On win32, we might need to set the abi to stdcall - but win64 only supports cdecl/default
#if defined(_WIN32) && !defined(_WIN64)
    abi = (flags & com_kenai_jffi_Foreign_F_STDCALL) != 0 ? FFI_STDCALL : FFI_DEFAULT_ABI;
#else
    abi = FFI_DEFAULT_ABI;
#endif

    // Cannot bypass FFI unless ABI is cdecl
    if (abi != FFI_DEFAULT_ABI) {
        isFastInt = false;
        isFastLong = false;
    }

    ffiStatus = ffi_prep_cif(&ctx->cif, abi, paramCount, (ffi_type *) j2p(returnType),
            ctx->ffiParamTypes);
    switch (ffiStatus) {
        case FFI_OK:
            break;
        case FFI_BAD_TYPEDEF:
            throwException(env, IllegalArgument, "Bad typedef");
            goto cleanup;
        case FFI_BAD_ABI:
            throwException(env, Runtime, "Invalid ABI");
            goto cleanup;
        default:
            throwException(env, Runtime, "Unknown FFI error");
    }
    ctx->rawParameterSize = rawOffset;
    ctx->flags |= (flags & com_kenai_jffi_Foreign_F_NOERRNO) == 0 ? CALL_CTX_SAVE_ERRNO : 0;
    ctx->flags |= isFastInt ? CALL_CTX_FAST_INT : 0;
    ctx->flags |= isFastLong ? CALL_CTX_FAST_LONG : 0;
    ctx->flags |= (flags & com_kenai_jffi_Foreign_F_PROTECT) != 0 ? CALL_CTX_FAULT_PROT : 0;

    return p2j(ctx);
cleanup:
    if (ctx != NULL) {
        if (ctx->rawParamOffsets != NULL) {
            free(ctx->rawParamOffsets);
        }
        if (ctx->ffiParamTypes != NULL) {
            free(ctx->ffiParamTypes);
        }
       free(ctx);
    }
    return 0LL;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeCallContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_freeCallContext(JNIEnv* env, jobject self, jlong handle)
{
    CallContext* ctx = (CallContext *) j2p(handle);
    if (ctx != NULL) {
        if (ctx->rawParamOffsets != NULL) {
            free(ctx->rawParamOffsets);
        }
        if (ctx->ffiParamTypes != NULL) {
            free(ctx->ffiParamTypes);
        }
        free(ctx);
    }
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getCallContextRawParameterSize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_getCallContextRawParameterSize(JNIEnv* env, jobject self, jlong handle)
{
    CallContext* ctx = (CallContext *) j2p(handle);
    return ctx->rawParameterSize;
}

JNIEXPORT void JNICALL 
Java_com_kenai_jffi_Foreign_setCallContextErrorFunction(JNIEnv* env, jobject self, jlong handle, jlong fn)
{
    CallContext* ctx = (CallContext *) j2p(handle);
    ctx->error_fn = j2p(fn);
}
