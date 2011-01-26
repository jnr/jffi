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
#include "LastError.h"
#include "com_kenai_jffi_Foreign.h"

typedef unsigned int u32;

#if defined(__i386__)
# define INT_BYPASS_FFI
# define FLOAT_BYPASS_FFI
#elif defined(__x86_64__)
# define INT_BYPASS_FFI
#endif


#if defined(INT_BYPASS_FFI)
# define invokeVrI(ctx, fn, retval) do { \
            *(retval) = ((jint (*)()) (fn))(); \
    } while (0)

# define invokeIrI(ctx, fn, retval, arg1) do { \
            *(retval) = ((jint (*)(jint)) (fn))(arg1); \
    } while (0)

# define invokeIIrI(ctx, fn, retval, arg1, arg2) do { \
            *(retval) = ((jint (*)(jint, jint)) (fn))((arg1), (arg2)); \
    } while (0)

# define invokeIIIrI(ctx, fn, retval, arg1, arg2, arg3) do { \
            *(retval) = ((jint (*)(jint, jint, jint)) (fn))(arg1, arg2, arg3); \
    } while (0)

# define invokeIIIIrI(ctx, fn, retval, arg1, arg2, arg3, arg4) do { \
            *(retval) = ((jint (*)(jint, jint, jint, jint)) (fn))(arg1, arg2, arg3, arg4); \
    } while (0)

# define invokeIIIIIrI(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5) do { \
            *(retval) = ((jint (*)(jint, jint, jint, jint, jint)) (fn))(arg1, arg2, arg3, arg4, arg5); \
    } while (0)

# define invokeIIIIIIrI(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5, arg6) do { \
            *(retval) = ((jint (*)(jint, jint, jint, jint, jint, jint)) (fn))(arg1, arg2, arg3, arg4, arg5, arg6); \
    } while (0)

#else /* non-i386, non-x86_64 */

# define invokeVrI ffi_call0
# define invokeIrI ffi_call1
# define invokeIIrI ffi_call2
# define invokeIIIrI ffi_call3
# define invokeIIIIrI ffi_call4
# define invokeIIIIIrI ffi_call5
# define invokeIIIIIIrI ffi_call6

#endif

#if defined(FLOAT_BYPASS_FFI)
# define invokeVrF(ctx, fn, rvp) \
    *(float *) (rvp) = ((float (*)(void)) (fn))()

# define invokeIrF(ctx, fn, rvp, arg1) \
    *(float *) (rvp) = ((float (*)(int)) (fn))(arg1)

# define invokeIIrF(ctx, fn, rvp, arg1, arg2) \
    *(float *) (rvp) = ((float (*)(int, int)) (fn))(arg1, arg2)

# define invokeIIIrF(ctx, fn, rvp, arg1, arg2, arg3) \
    *(float *) (rvp) = ((float (*)(int, int, int)) (fn))(arg1, arg2, arg3)
#else

# define invokeVrF ffi_call0
# define invokeIrF ffi_call1
# define invokeIIrF ffi_call2
# define invokeIIIrF ffi_call3

#endif

#if defined(FLOAT_BYPASS_FFI) && defined(__i386__)
// Doing the test before the call produces slightly better i386 asm for float calls
# define CALL(ctx, stmt) do { \
    if (likely(!ctx->saveErrno)) { \
        stmt; \
    } else { \
        stmt; \
        jffi_save_errno(); \
    } \
   } while (0)
#else
// This version produces smaller code for ffi_call paths
# define CALL(ctx, stmt) do { stmt; SAVE_ERRNO(ctx); } while(0)
#endif

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrI
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeVrI(JNIEnv* env, jclass self, jlong ctxAddress)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeVrI(ctx, ctx->function, &retval));

    return (jint) retval;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrI
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoVrI(JNIEnv* env, jclass self, jlong ctxAddress)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;
    invokeVrI(ctx, ctx->function, &retval);

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIrI(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeIrI(ctx, ctx->function, &retval, arg1));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoIrI(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIrI(ctx, ctx->function, &retval, arg1);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeIIrI(ctx, ctx->function, &retval, arg1, arg2));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIIrI(ctx, ctx->function, &retval, arg1, arg2);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeIIIrI(ctx, ctx->function, &retval, arg1, arg2, arg3));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3, jint arg4)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeIIIIrI(ctx, ctx->function, &retval, arg1, arg2, arg3, arg4));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIIIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3, jint arg4, jint arg5)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeIIIIIrI(ctx, ctx->function, &retval, arg1, arg2, arg3, arg4, arg5));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIIIIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3, jint arg4, jint arg5, jint arg6)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeIIIIIIrI(ctx, ctx->function, &retval, arg1, arg2, arg3, arg4, arg5, arg6));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIIIrI(ctx, ctx->function, &retval, arg1, arg2, arg3);

    return (int) retval;
}

JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeVrF(JNIEnv* env, jclass self, jlong ctxAddress)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    float retval;

    CALL(ctx, invokeVrF(ctx, ctx->function, &retval));

    return retval;
}

JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeIrF(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    float retval;

    CALL(ctx, invokeIrF(ctx, ctx->function, &retval, arg1));

    return retval;
}

JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeIIrF(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    float retval;

    CALL(ctx, invokeIIrF(ctx, ctx->function, &retval, arg1, arg2));

    return retval;
}



JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeIIIrF(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    float retval;

    CALL(ctx, invokeIIIrF(ctx, ctx->function, &retval, arg1, arg2, arg3));

    return retval;
}
