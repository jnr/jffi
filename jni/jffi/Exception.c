/* 
 * Copyright (C) 2008 Wayne Meissner
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

#include <stdio.h>
#include <stdarg.h>
#include <jni.h>
#include "Exception.h"

void 
jffi_throwExceptionByName(JNIEnv* env, const char* exceptionName, const char* fmt, ...)
{
    va_list ap;
    char buf[1024] = { 0 };
    va_start(ap, fmt);
    vsnprintf(buf, sizeof(buf) - 1, fmt, ap);
    
    (*env)->PushLocalFrame(env, 10);
    jclass exceptionClass = (*env)->FindClass(env, exceptionName);
    if (exceptionClass != NULL) {
        (*env)->ThrowNew(env, exceptionClass, buf);
    }
    (*env)->PopLocalFrame(env, NULL);
    va_end(ap);
}

const char* jffi_IllegalArgumentException = "java/lang/IllegalArgumentException";
const char* jffi_NullPointerException = "java/lang/NullPointerException";
const char* jffi_OutOfBoundsException = "java/lang/IndexOutOfBoundsException";
const char* jffi_OutOfMemoryException = "java/lang/OutOfMemoryError";
const char* jffi_RuntimeException = "java/lang/RuntimeException";
const char* jffi_UnsatisfiedLinkException = "java/lang/UnsatisfiedLinkError";

