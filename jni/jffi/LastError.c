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

#include <stdlib.h>
#include <errno.h>
#ifdef _WIN32
#  include <windows.h>
#endif
#include <jni.h>
#include "LastError.h"
#include "CallContext.h"

#if defined(_WIN32)
static __thread int last_error = 0;
#endif

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getLastError
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_getLastError(JNIEnv* env, jobject self)
{
#ifdef _WIN32
    // printf("Getting ERRNO: %d on thread %d\n", last_error, (int)GetCurrentThreadId());
    return last_error;
#else
    return thread_data_get()->error;
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    setLastError
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_setLastError(JNIEnv* env, jobject self, jint value)
{
#ifdef _WIN32
    // printf("Setting ERRNO: %d on thread %d\n", value, (int)GetCurrentThreadId());
    SetLastError(value);
    last_error = value;
#else
    thread_data_get()->error = errno = value;
#endif
}

void
jffi_save_errno(void)
{
#ifdef _WIN32
    last_error = GetLastError();
    // printf("JFFI Saving ERRNO: %d on thread %d\n", last_error, (int)GetCurrentThreadId());
#else
    thread_data_get()->error = errno;
#endif
}

void
jffi_save_errno_ctx(CallContext* ctx)
{
#ifdef _WIN32
    if (unlikely(ctx->error_fn != NULL)) {
	last_error = (*ctx->error_fn)();
    } else {
	last_error = GetLastError();
    }
#else
    if (unlikely(ctx->error_fn != NULL)) {
	thread_data_get()->error = (*ctx->error_fn)();
    } else {
	thread_data_get()->error = errno;
    }
#endif
}

#ifndef _WIN32
void
jffi_save_errno_td(ThreadData* td, CallContext* ctx)
{
    if (unlikely(ctx->error_fn != NULL)) {
	    td->error = (*ctx->error_fn)();
    } else {
	    td->error = errno;
    }
}
#endif
