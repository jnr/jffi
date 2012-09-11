/* 
 * Copyright (C) 2007, 2008 Wayne Meissner
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
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <jni.h>

#include "jffi.h"
#include "Exception.h"
#include "com_kenai_jffi_ObjectBuffer.h"

#include "Array.h"

#define ARGPRIM_MASK com_kenai_jffi_ObjectBuffer_PRIM_MASK
#define ARGTYPE_MASK com_kenai_jffi_ObjectBuffer_TYPE_MASK
#define ARGTYPE_SHIFT com_kenai_jffi_ObjectBuffer_TYPE_SHIFT
#define ARGFLAGS_MASK com_kenai_jffi_ObjectBuffer_FLAGS_MASK

static void 
releaseHeapArray(JNIEnv* env, Array* array) 
{
    free(array->elems);
}

#define COPY_DATA(JTYPE, NTYPE, flags, obj, offset, length, array) do { \
    if (IS_IN_ARRAY(flags)) { \
        (*env)->Get##JTYPE##ArrayRegion(env, obj, offset, length, (NTYPE *) array->elems); \
        if (unlikely((*env)->ExceptionCheck(env) != JNI_FALSE)) return NULL; \
    } else if (unlikely((flags & ARRAY_CLEAR) != 0)) { \
        memset(array->elems, 0, length * sizeof(NTYPE)); \
    } \
} while (0)

#define SET_COPYOUT(JTYPE, array, flags) \
    (array)->copyout = IS_OUT_ARRAY(flags) \
        ? (void (JNICALL *)(JNIEnv*, jobject, jsize, jsize, const void *))(*env)->Set##JTYPE##ArrayRegion \
        : NULL

#define SET_COPYIN(JTYPE, array, flags) \
    (array)->copyin = IS_IN_ARRAY(flags) \
        ? (void (JNICALL *)(JNIEnv*, jobject, jsize, jsize, void *))(*env)->Get##JTYPE##ArrayRegion \
        : NULL

#define GET_ARRAY_BUFFER(JTYPE, NTYPE, flags, obj, offset, length, array) do { \
    COPY_DATA(JTYPE, NTYPE, flags, obj, offset, length, array); \
    SET_COPYOUT(JTYPE, array, flags); \
} while (0)

#define GET_ARRAY_HEAP(JTYPE, NTYPE, flags, obj, offset, length, array) do { \
    int allocSize = sizeof(NTYPE) * (length + 1); \
    (array)->elems = malloc(allocSize); \
    if (unlikely((array)->elems == NULL)) { \
        throwException(env, OutOfMemory, "failed to allocate native array of %d bytes", allocSize); \
        return NULL; \
    } \
    COPY_DATA(JTYPE, NTYPE, flags, obj, offset, length, array); \
    SET_COPYOUT(JTYPE, array, flags); \
} while(0)

void*
jffi_getArrayHeap(JNIEnv* env, jobject buf, jsize offset, jsize length, int type,
        Array* array) 
{
    array->array = buf;
    array->offset = offset;
    array->length = length;
    array->type = type;
    array->copyin = NULL;
    array->copyout = NULL;
    array->release = releaseHeapArray;
    
    /*
     * Byte arrays are used for struct backing in both jaffl and jruby ffi, so
     * are the most likely path.
     */
    if (likely((type & ARGPRIM_MASK) == com_kenai_jffi_ObjectBuffer_BYTE)) {
        GET_ARRAY_HEAP(Byte, jbyte, type, buf, offset, length, array);
        // If the array was really a string, nul terminate it
        if ((type & (ARRAY_NULTERMINATE | ARRAY_IN | ARRAY_OUT)) != ARRAY_OUT) {
            *(((char *) array->elems) + length) = '\0';
        }
    } else {
        switch (type & ARGPRIM_MASK) {
            case com_kenai_jffi_ObjectBuffer_SHORT:
                GET_ARRAY_HEAP(Short, jshort, type, buf, offset, length, array);
                break;
            
            case com_kenai_jffi_ObjectBuffer_INT:
                GET_ARRAY_HEAP(Int, jint, type, buf, offset, length, array);
                break;
            
            case com_kenai_jffi_ObjectBuffer_LONG:
                GET_ARRAY_HEAP(Long, jlong, type, buf, offset, length, array);
                break;
            
            case com_kenai_jffi_ObjectBuffer_FLOAT:
                GET_ARRAY_HEAP(Float, jfloat, type, buf, offset, length, array);
                break;
            
            case com_kenai_jffi_ObjectBuffer_DOUBLE:
                GET_ARRAY_HEAP(Double, jdouble, type, buf, offset, length, array);
                break;
            
            case com_kenai_jffi_ObjectBuffer_BOOLEAN:
                GET_ARRAY_HEAP(Boolean, jboolean, type, buf, offset, length, array);
                break;
            
            case com_kenai_jffi_ObjectBuffer_CHAR:
                GET_ARRAY_HEAP(Char, jchar, type, buf, offset, length, array);
                break;
            
            default:
                throwException(env, IllegalArgument, "invalid array type: %#x\n", type);
                return NULL;
        }
    }
    
    return array->elems;
}

void*
jffi_getArrayBuffer(JNIEnv* env, jobject buf, jint offset, jint length, int type, struct Array* array, void* buffer)
{
    array->array = buf;
    array->elems = buffer;
    array->offset = offset;
    array->length = length;
    array->type = type;
    array->release = NULL;
    array->copyin = NULL;
    array->copyout = NULL;

    switch (type & ARGPRIM_MASK) {
        case com_kenai_jffi_ObjectBuffer_BYTE:
            GET_ARRAY_BUFFER(Byte, jbyte, type, buf, offset, length, array);
            // If the array was really a string, nul terminate it
            if ((type & (ARRAY_NULTERMINATE | ARRAY_IN | ARRAY_OUT)) != ARRAY_OUT) {
                *(((char *) array->elems) + length) = '\0';
            }
            break;

        case com_kenai_jffi_ObjectBuffer_SHORT:
            GET_ARRAY_BUFFER(Short, jshort, type, buf, offset, length, array);
            break;

        case com_kenai_jffi_ObjectBuffer_INT:
            GET_ARRAY_BUFFER(Int, jint, type, buf, offset, length, array);
            break;

        case com_kenai_jffi_ObjectBuffer_LONG:
            GET_ARRAY_BUFFER(Long, jlong, type, buf, offset, length, array);
            break;

        case com_kenai_jffi_ObjectBuffer_FLOAT:
            GET_ARRAY_BUFFER(Float, jfloat, type, buf, offset, length, array);
            break;

        case com_kenai_jffi_ObjectBuffer_DOUBLE:
            GET_ARRAY_BUFFER(Double, jdouble, type, buf, offset, length, array);
            break;
        
        case com_kenai_jffi_ObjectBuffer_BOOLEAN:
            GET_ARRAY_BUFFER(Boolean, jboolean, type, buf, offset, length, array);
            break;
        
        case com_kenai_jffi_ObjectBuffer_CHAR:
            GET_ARRAY_BUFFER(Char, jchar, type, buf, offset, length, array);
            break;
        default:
            throwException(env, IllegalArgument, "Invalid array type: %#x\n", type);
            return NULL;
    }

    return array->elems;
}

int
jffi_arraySize(int length, int type)
{
    switch (type & ARGPRIM_MASK) {
        case com_kenai_jffi_ObjectBuffer_BYTE:
            return length * sizeof(jbyte);

        case com_kenai_jffi_ObjectBuffer_SHORT:
            return length * sizeof(jshort);

        case com_kenai_jffi_ObjectBuffer_INT:
            return length * sizeof(jint);

        case com_kenai_jffi_ObjectBuffer_LONG:
            return length * sizeof(jlong);

        case com_kenai_jffi_ObjectBuffer_FLOAT:
            return length * sizeof(jfloat);

        case com_kenai_jffi_ObjectBuffer_DOUBLE:
            return length * sizeof(jdouble);

        case com_kenai_jffi_ObjectBuffer_BOOLEAN:
            return length * sizeof(jboolean);

        case com_kenai_jffi_ObjectBuffer_CHAR:
            return length * sizeof(jchar);

        default:
            return length * 8;
    }
}

static void 
jffi_releaseCriticalArray(JNIEnv* env, Array *array)
{
    (*env)->ReleasePrimitiveArrayCritical(env, array->array, array->elems, 0);
}


void*
jffi_getArrayCritical(JNIEnv* env, jobject buf, jsize offset, jsize length, int type, struct Array* array)
{
    array->array = buf;
    array->offset = offset;
    array->length = length;
    array->type = type;
    array->copyin = NULL;
    array->copyout = NULL;
    array->elems = (*env)->GetPrimitiveArrayCritical(env, array->array, NULL);

    if (unlikely(array->elems == NULL)) {
        if (!(*env)->ExceptionCheck(env)) {
            throwException(env, NullPointer, "failed to pin native array");
        }
        return NULL;
    }
    array->release = jffi_releaseCriticalArray;

    return (char *) array->elems + offset;
}

void
jffi_releaseArrays(JNIEnv *env, Array* arrays, int arrayCount)
{
    int aryIdx;
    for (aryIdx = arrayCount - 1; aryIdx >= 0; aryIdx--) {
        
        Array* array = &arrays[aryIdx];
        if (IS_OUT_ARRAY(array->type) && array->copyout != NULL) {
            if ((*env)->ExceptionCheck(env) == JNI_FALSE) {
                (*array->copyout)(env, array->array, array->offset, array->length, array->elems);
            }
        }
        
        if (unlikely(array->release != NULL)) {
            (*array->release)(env, array);
        }
    }
}
