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
#include <stdio.h>
#include <stdlib.h>
#ifdef __sun
# include <alloca.h>
#endif
#include <errno.h>
#include <ffi.h>
#include <jni.h>
#include "endian.h"
#include "jffi.h"
#include "Exception.h"
#include "CallContext.h"
#include "LastError.h"
#include "FaultProtect.h"
#include "com_kenai_jffi_Foreign.h"


/* for return values <= sizeof(long), need to use an ffi_sarg sized return value */
#if BYTE_ORDER == BIG_ENDIAN
# define RETVAL(retval, ctx) ((ctx->cif.rtype)->size > sizeof(ffi_sarg) ? (retval).j : (retval).sarg)
#else
# define RETVAL(retval, ctx) ((retval).j)
#endif

#if defined(__x86_64__) && defined(__GNUC__)
# define LONG_BYPASS_FFI
#endif

#if defined(LONG_BYPASS_FFI)

# if defined(__x86_64) || defined(__amd64)
#  define CLEAR_VARARGS ({__asm__ __volatile__("xorq %%rax, %%rax" ::: "rax");})
# else
#  define CLEAR_VARARGS do { } while(0)
# endif

# define invokeL0(ctx, fn, retval) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)()) (fn))(); \
    } while (0)

# define invokeL1(ctx, fn, retval, arg1) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong)) (fn))(arg1); \
    } while (0)

# define invokeL2(ctx, fn, retval, arg1, arg2) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong)) (fn))((arg1), (arg2)); \
    } while (0)

# define invokeL3(ctx, fn, retval, arg1, arg2, arg3) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong, jlong)) (fn))(arg1, arg2, arg3); \
    } while (0)

# define invokeL4(ctx, fn, retval, arg1, arg2, arg3, arg4) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong, jlong, jlong)) (fn))(arg1, arg2, arg3, arg4); \
    } while (0)

# define invokeL5(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong, jlong, jlong, jlong)) (fn))(arg1, arg2, arg3, arg4, arg5); \
    } while (0)

# define invokeL6(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5, arg6) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong, jlong, jlong, jlong, jlong)) (fn))(arg1, arg2, arg3, arg4, arg5, arg6); \
    } while (0)

#else /* non-i386, non-x86_64 */

# define invokeL0 ffi_call0
# define invokeL1 ffi_call1
# define invokeL2 ffi_call2
# define invokeL3 ffi_call3
# define invokeL4 ffi_call4
# define invokeL5 ffi_call5
# define invokeL6 ffi_call6

#endif


#if !FAULT_PROTECT_ENABLED
# define CALL(env, ctx, stmt) do { stmt; SAVE_ERRNO(ctx); } while(0)
#else
# define CALL(env, ctx, stmt) FAULTPROT_CTX(env, ctx, stmt, return 0)
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
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL0(ctx, j2p(function), &retval), return 0);
    return RETVAL(retval, ctx);
}


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrL
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL0NoErrno(JNIEnv* env, jobject self, jlong ctxAddress, jlong function)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL0(ctx, j2p(function), &retval), return 0);

    return RETVAL(retval, ctx);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL1(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL1(ctx, j2p(function), &retval, arg1), return 0);

    return RETVAL(retval, ctx);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL1NoErrno(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL1(ctx, j2p(function), &retval, arg1), return 0);

    return RETVAL(retval, ctx);
}


JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL2(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL2(ctx, j2p(function), &retval, arg1, arg2), return 0);

    return RETVAL(retval, ctx);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL2NoErrno(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL2(ctx, j2p(function), &retval, arg1, arg2), return 0);

    return RETVAL(retval, ctx);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL3(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL3(ctx, j2p(function), &retval, arg1, arg2, arg3), return 0);

    return RETVAL(retval, ctx);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL3NoErrno(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL3(ctx, j2p(function), &retval, arg1, arg2, arg3), return 0);

    return RETVAL(retval, ctx);
}


JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL4(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);

    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL4(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4), return 0);

    return RETVAL(retval, ctx);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL4NoErrno(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);

    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL4(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4), return 0);

    return RETVAL(retval, ctx);
}


JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL5(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL5(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5), return 0);

    return RETVAL(retval, ctx);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL5NoErrno(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL5(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5), return 0);

    return RETVAL(retval, ctx);
}


JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL6(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5, jlong arg6)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL6(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5, arg6), return 0);

    return RETVAL(retval, ctx);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeL6NoErrno(JNIEnv* env, jobject self, jlong ctxAddress, jlong function,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5, jlong arg6)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    FFIValue retval;

    FAULTPROT_CTX(env, ctx, invokeL6(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5, arg6), return 0);

    return RETVAL(retval, ctx);
}
