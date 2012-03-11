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

    jlong ctxAddress = Java_com_kenai_jffi_Foreign_newCallContext(env, self, returnType, paramArray, flags);
    ctx = (Function *) j2p(ctxAddress);
    ctx->function = j2p(function);

    return ctxAddress;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeCallContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_freeFunction(JNIEnv* env, jobject self, jlong handle)
{
    Java_com_kenai_jffi_Foreign_freeCallContext(env, self, handle);
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
