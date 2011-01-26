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

#include <sys/param.h>
#include <sys/types.h>

#include <stdlib.h>
#if defined(__sun) || defined(_AIX)
#  include <alloca.h>
#endif
#ifdef _WIN32
#  include <malloc.h>
#endif
#include <ffi.h>
#include <jni.h>
#include "com_kenai_jffi_Foreign.h"
#include "jffi.h"
#include "Exception.h"

#ifndef MAX
#  define MAX(x,y)  ((x) > (y) ? (x) : (y))
#endif
#define FFI_ALIGN(v, a)  (((((size_t) (v))-1) | ((a)-1))+1)

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    newStruct
 * Signature: ([J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_newStruct(JNIEnv* env, jobject self, jlongArray typeArray, jboolean isUnion)
{
    ffi_type* s = NULL;
    int fieldCount;
    jlong* fieldTypes;
    int i;

    if (typeArray == NULL) {
        throwException(env, NullPointer, "types array cannot be null");
        return 0L;
    }

    fieldCount = (*env)->GetArrayLength(env, typeArray);
    if (fieldCount < 1) {
        throwException(env, IllegalArgument, "No fields specified");
        return 0L;
    }

    s = calloc(1, sizeof(*s));
    if (s == NULL) {
        throwException(env, OutOfMemory, "failed to allocate memory");
        return 0L;
    }

    //
    // Need to terminate the list of field types with a NULL, so allocate 1 extra
    //
    s->elements = calloc(fieldCount + 1, sizeof(ffi_type *));
    if (s->elements == NULL) {
        throwException(env, OutOfMemory, "failed to allocate memory");
        goto error;
    }

    // Copy out all the field descriptors
    fieldTypes = alloca(fieldCount * sizeof(jlong));
    (*env)->GetLongArrayRegion(env, typeArray, 0, fieldCount, fieldTypes);
    
    s->type = FFI_TYPE_STRUCT;
    s->size = 0;
    s->alignment = 0;

    for (i = 0; i < fieldCount; ++i) {
        ffi_type* elem = (ffi_type *) j2p(fieldTypes[i]);

        if (elem == NULL) {
            throwException(env, IllegalArgument, "type for field %d is NULL", i);
            goto error;
        }
        if (elem->size == 0) {
            throwException(env, IllegalArgument, "type for field %d has size 0", i);
            goto error;
        }

        s->elements[i] = elem;
        if (!isUnion) {
            s->size = FFI_ALIGN(s->size, elem->alignment) + elem->size;
        } else {
            s->size = MAX(s->size, elem->size);
        }
        s->alignment = MAX(s->alignment, elem->alignment);
    }
    if (s->size == 0) {
        throwException(env, Runtime, "struct size is zero");
        goto error;
    }
    
    // Include tail padding
    s->size = FFI_ALIGN(s->size, s->alignment);
    return p2j(s);
    
error:
    if (s != NULL) {
        if (s->elements != NULL) {
            free(s->elements);
        }
        free(s);
    }

    return 0L;
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_newArray(JNIEnv* env, jobject self, jlong type, jint length)
{
    ffi_type* elem = (ffi_type *) j2p(type);
    ffi_type* s = NULL;
    int i;

    if (elem == NULL) {
        throwException(env, NullPointer, "element type cannot be null");
        return 0L;
    }

    if (elem->size == 0) {
        throwException(env, IllegalArgument, "element type size 0");
        return 0L;
    }

    if (length < 1) {
        throwException(env, IllegalArgument, "array length == 0");
        return 0L;
    }

    s = calloc(1, sizeof(*s));
    if (s == NULL) {
        throwException(env, OutOfMemory, "failed to allocate memory");
        return 0L;
    }

    s->type = FFI_TYPE_STRUCT;
    s->alignment = elem->alignment;
    s->size = length * elem->size;

    // Need to terminate the list of field types with a NULL, so allocate 1 extra
    s->elements = calloc(length + 1, sizeof(ffi_type *));
    if (s->elements == NULL) {
        throwException(env, OutOfMemory, "failed to allocate memory");
        free(s);
        return 0L;
    }

    for (i = 0; i < length; ++i) {
        s->elements[i] = elem;
    }
    
    return p2j(s);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeStruct
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_freeAggregate(JNIEnv* env, jobject self, jlong handle)
{
    ffi_type* s = (ffi_type *) j2p(handle);
    
    if (s != NULL) {
        free(s->elements);
        free(s);
    }
}
