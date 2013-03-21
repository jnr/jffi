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
#include "CallContext.h"
#include "LastError.h"
#include "FaultProtect.h"
#include "com_kenai_jffi_Foreign.h"
#include "FastNumeric.h"


#if !FAULT_PROTECT_ENABLED
# define CALL(ctx, stmt) do { stmt; SAVE_ERRNO(ctx); } while(0)
#else
# define CALL(ctx, stmt) FAULTPROT_CTX(env, ctx, stmt, return 0)
#endif

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrI
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI0(JNIEnv* env, jclass self, jlong ctxAddress, jlong function)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeI0(ctx, j2p(function), &retval));

    return (jint) retval;
}


JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI1(JNIEnv* env, jclass self, jlong ctxAddress, jlong function,
        jint arg1)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeI1(ctx, j2p(function), &retval, arg1));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI2(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeI2(ctx, j2p(function), &retval, arg1, arg2));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI3(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2, jint arg3)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeI3(ctx, j2p(function), &retval, arg1, arg2, arg3));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI4(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2, jint arg3, jint arg4)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeI4(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI5(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2, jint arg3, jint arg4, jint arg5)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeI5(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI6(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2, jint arg3, jint arg4, jint arg5, jint arg6)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeI6(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5, arg6));

    return (jint) retval;
}


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrI
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI0NoErrno(JNIEnv* env, jclass self, jlong ctxAddress, jlong function)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;
    invokeI0(ctx, j2p(function), &retval);

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI1NoErrno(JNIEnv* env, jclass self, jlong ctxAddress, jlong function,
        jint arg1)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeI1(ctx, j2p(function), &retval, arg1);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI2NoErrno(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeI2(ctx, j2p(function), &retval, arg1, arg2);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI3NoErrno(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2, jint arg3)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeI3(ctx, j2p(function), &retval, arg1, arg2, arg3);

    return (int) retval;
}


JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI4NoErrno(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2, jint arg3, jint arg4)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeI4(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4);

    return (jint) retval;
}


JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI5NoErrno(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2, jint arg3, jint arg4, jint arg5)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeI5(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5);

    return (jint) retval;
}


JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeI6NoErrno(JNIEnv*env, jobject self, jlong ctxAddress, jlong function,
        jint arg1, jint arg2, jint arg3, jint arg4, jint arg5, jint arg6)
{
    CallContext *ctx = (CallContext *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeI6(ctx, j2p(function), &retval, arg1, arg2, arg3, arg4, arg5, arg6);

    return (jint) retval;
}
