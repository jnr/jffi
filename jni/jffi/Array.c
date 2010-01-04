/* 
 * Copyright (C) 2007, 2008 Wayne Meissner
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
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <jni.h>

#include "jffi.h"
#include "Exception.h"
#include "com_kenai_jffi_ObjectBuffer.h"

#include "Array.h"

#define ARRAY_NULTERMINATE com_kenai_jffi_ObjectBuffer_ZERO_TERMINATE
#define ARRAY_IN com_kenai_jffi_ObjectBuffer_IN
#define ARRAY_OUT com_kenai_jffi_ObjectBuffer_OUT
#define ARRAY_PINNED com_kenai_jffi_ObjectBuffer_PINNED
#define ARRAY_CLEAR com_kenai_jffi_ObjectBuffer_CLEAR
#define ARGPRIM_MASK com_kenai_jffi_ObjectBuffer_PRIM_MASK
#define ARGTYPE_MASK com_kenai_jffi_ObjectBuffer_TYPE_MASK
#define ARGTYPE_SHIFT com_kenai_jffi_ObjectBuffer_TYPE_SHIFT
#define ARGFLAGS_MASK com_kenai_jffi_ObjectBuffer_FLAGS_MASK

#define RELEASE(JTYPE, NTYPE) \
static void release##JTYPE##ArrayHeap(JNIEnv *env, Array *array) \
{ \
    (*env)->Set##JTYPE##ArrayRegion(env, array->array, array->offset, array->length, \
            (NTYPE *) array->elems); \
    free(array->elems); \
} \
static void free##JTYPE##Array(JNIEnv *env, Array *array) \
{ \
    free(array->elems); \
} \
static void release##JTYPE##ArrayBuffer(JNIEnv *env, Array *array) \
{ \
    (*env)->Set##JTYPE##ArrayRegion(env, array->array, array->offset, array->length, \
            (NTYPE *) array->elems); \
}

RELEASE(Byte, jbyte);
RELEASE(Short, jshort);
RELEASE(Int, jint);
RELEASE(Long, jlong);
RELEASE(Float, jfloat);
RELEASE(Double, jdouble);

#define isOut(flags) (((flags) & (ARRAY_IN | ARRAY_OUT)) != ARRAY_IN)
#define isIn(flags) (((flags) & (ARRAY_IN | ARRAY_OUT)) != ARRAY_OUT)

#define COPY_DATA(JTYPE, NTYPE, flags, obj, offset, length, array) do { \
    if (isIn(flags)) { \
        (*env)->Get##JTYPE##ArrayRegion(env, obj, offset, length, (NTYPE *) array->elems); \
    } else if (unlikely((flags & ARRAY_CLEAR) != 0)) { \
        memset(array->elems, 0, length * sizeof(NTYPE)); \
    } \
} while (0)

#define GET_ARRAY_BUFFER(JTYPE, NTYPE, flags, obj, offset, length, array) do { \
    COPY_DATA(JTYPE, NTYPE, flags, obj, offset, length, array); \
    (array)->release = isOut(flags) ? release##JTYPE##ArrayBuffer : NULL; \
} while (0)

#define GET_ARRAY_HEAP(JTYPE, NTYPE, flags, obj, offset, length, array) do { \
    int allocSize = sizeof(NTYPE) * (length + 1); \
    (array)->elems = malloc(allocSize); \
    if (unlikely((array)->elems == NULL)) { \
        throwException(env, OutOfMemory, "failed to allocate native array of %d bytes", allocSize); \
        return NULL; \
    } \
    COPY_DATA(JTYPE, NTYPE, flags, obj, offset, length, array); \
    (array)->release = isOut(flags) ? release##JTYPE##ArrayHeap : free##JTYPE##Array; \
} while(0)

void*
jffi_getArrayHeap(JNIEnv* env, jobject buf, jsize offset, jsize length, int paramType,
        Array* array) 
{
    array->array = buf;
    array->offset = offset;
    array->length = length;
    
    /*
     * Byte arrays are used for struct backing in both jaffl and jruby ffi, so
     * are the most likely path.
     */
    if (likely((paramType & ARGPRIM_MASK) == com_kenai_jffi_ObjectBuffer_BYTE)) {
        GET_ARRAY_HEAP(Byte, jbyte, paramType, buf, offset, length, array);
        // If the array was really a string, nul terminate it
        if ((paramType & (ARRAY_NULTERMINATE | ARRAY_IN | ARRAY_OUT)) != ARRAY_OUT) {
            *(((char *) array->elems) + length) = '\0';
        }
    } else {
        switch (paramType & ARGPRIM_MASK) {
            case com_kenai_jffi_ObjectBuffer_SHORT:
                GET_ARRAY_HEAP(Short, jshort, paramType, buf, offset, length, array);
                break;
            case com_kenai_jffi_ObjectBuffer_INT:
                GET_ARRAY_HEAP(Int, jint, paramType, buf, offset, length, array);
                break;
            case com_kenai_jffi_ObjectBuffer_LONG:
                GET_ARRAY_HEAP(Long, jlong, paramType, buf, offset, length, array);
                break;
            case com_kenai_jffi_ObjectBuffer_FLOAT:
                GET_ARRAY_HEAP(Float, jfloat, paramType, buf, offset, length, array);
                break;
            case com_kenai_jffi_ObjectBuffer_DOUBLE:
                GET_ARRAY_HEAP(Double, jdouble, paramType, buf, offset, length, array);
                break;
            default:
                throwException(env, IllegalArgument, "Invalid array type: %#x\n", paramType);
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
        default:
            return 0;
    }
}

static void 
jffi_releaseCriticalArray(JNIEnv* env, Array *array)
{
    (*env)->ReleasePrimitiveArrayCritical(env, array->array, array->elems, 0);
}


void*
jffi_getArrayCritical(JNIEnv* env, jobject buf, jsize offset, jsize length, int paramType, struct Array* array)
{
    array->array = buf;
    array->offset = offset;
    array->length = length;
    array->elems = (*env)->GetPrimitiveArrayCritical(env, array->array, NULL);

    if (unlikely(array->elems == NULL)) {
        throwException(env, NullPointer, "could not access array");
        return NULL;
    }
    array->release = jffi_releaseCriticalArray;

    return (char *) array->elems + offset;
}

