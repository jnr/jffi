/* 
 * Copyright (C) 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
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
const char* jffi_RuntimeException = "java/lang/RuntimeError";
const char* jffi_UnsatisfiedLinkException = "java/lang/UnsatisfiedLinkError";

