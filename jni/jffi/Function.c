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
#include "Function.h"
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
Java_com_kenai_jffi_Foreign_newFunction(JNIEnv* env, jobject self,
        jlong function, jlong returnType, jlongArray paramArray, jint flags)
{
    Function* ctx = NULL;
    jlong* paramTypes;
    int paramCount, i, rawOffset = 0;
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

    for (i = 0; i < paramCount; ++i) {
        ffi_type* type = (ffi_type *) j2p(paramTypes[i]);
        if (type == NULL) {
            throwException(env, IllegalArgument, "Invalid parameter type: %#x", paramTypes[i]);
            goto cleanup;
        }
        ctx->ffiParamTypes[i] = type;
        ctx->rawParamOffsets[i] = rawOffset;
        rawOffset += FFI_ALIGN(type->size, FFI_SIZEOF_ARG);
    }

    // On win32, we might need to set the abi to stdcall - but win64 only supports cdecl/default
#if defined(_WIN32) && !defined(_WIN64)
    abi = (flags & com_kenai_jffi_Foreign_F_STDCALL) != 0 ? FFI_STDCALL : FFI_DEFAULT_ABI;
#else
    abi = FFI_DEFAULT_ABI;
#endif

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
    ctx->function = j2p(function);
    /* Save errno unless explicitly told not to do so */
    ctx->saveErrno = (flags & com_kenai_jffi_Foreign_F_NOERRNO) == 0;

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
Java_com_kenai_jffi_Foreign_freeFunction(JNIEnv* env, jobject self, jlong handle)
{
    Function* ctx = (Function *) j2p(handle);
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
 * Method:    getFunctionRawParameterSize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_getFunctionRawParameterSize(JNIEnv* env, jobject self, jlong handle)
{
    Function* ctx = (Function *) j2p(handle);
    return ctx->rawParameterSize;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getFunctionAddress
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_getFunctionAddress(JNIEnv* env, jobject self, jlong handle)
{
    return p2j(((Function *) j2p(handle))->function);
}
