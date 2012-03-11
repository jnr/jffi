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
#include "LastError.h"
#include "com_kenai_jffi_Foreign.h"


/* for return values <= sizeof(long), need to use an ffi_sarg sized return value */
#if (BYTE_ORDER == BIG_ENDIAN) || defined(__i386__)
# define RETVAL(retval, ctx) ((ctx->cif.rtype)->size > sizeof(ffi_sarg) ? (retval).j : (retval).sarg)
#else
# define RETVAL(retval, ctx) ((retval).j & ctx->resultMask)
#endif

#if defined(__x86_64__)
# define LONG_BYPASS_FFI
#endif

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrL
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL0(JNIEnv* env, jobject self, jlong ctxAddress, jlong function)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(void)) (j2p(function)))(); \
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;
    
    ffi_call0(ctx, j2p(function), &retval);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx);
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLrL
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL1(JNIEnv* env, jobject self, jlong ctxAddress, jlong function, jlong arg1)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong)) (j2p(function)))(arg1); \
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call1(ctx, j2p(function), &retval, arg1);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx);
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLrL
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL2(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong)) (j2p(function)))(arg1, arg2); \
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call2(ctx, j2p(function), &retval, arg1, arg2);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx);
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLLrL
 * Signature: (JJJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL3(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong, jlong)) (j2p(function)))(arg1, arg2, arg3);
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call3(ctx, j2p(function), &retval, arg1, arg2, arg3);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx);
#endif
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL4(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong, jlong, jlong)) (j2p(function)))(arg1, arg2, arg3, arg4);
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call4(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx);
#endif
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL5(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong, jlong, jlong, jlong)) (j2p(function)))(arg1, arg2, arg3, arg4, arg5);
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call5(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx);
#endif
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL6(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5, jlong arg6)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong, jlong, jlong, jlong, jlong)) (j2p(function)))(arg1, arg2, arg3, arg4, arg5, arg6);
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call6(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5, arg6);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx);
#endif
}
