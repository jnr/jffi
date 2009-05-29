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
#define ARGPRIM_MASK com_kenai_jffi_ObjectBuffer_PRIM_MASK
#define ARGTYPE_MASK com_kenai_jffi_ObjectBuffer_TYPE_MASK
#define ARGTYPE_SHIFT com_kenai_jffi_ObjectBuffer_TYPE_SHIFT
#define ARGFLAGS_MASK com_kenai_jffi_ObjectBuffer_FLAGS_MASK

static JavaVM* jffi_vm;

#define RELEASE(JTYPE, NTYPE) \
static void release##JTYPE##Array(JNIEnv *env, Array *array) \
{ \
    if (array->mode == JNI_COMMIT || array->mode == 0) { \
        (*env)->Set##JTYPE##ArrayRegion(env, array->array, array->offset, array->length, \
            (NTYPE *) array->elems); \
    } \
    if ((array->mode == JNI_ABORT || array->mode == 0) && !array->stack) { \
        free(array->elems); \
    } \
}
RELEASE(Byte, jbyte);
RELEASE(Short, jshort);
RELEASE(Int, jint);
RELEASE(Long, jlong);
RELEASE(Float, jfloat);
RELEASE(Double, jdouble);

#define ARRAY(JTYPE, NTYPE, flags, obj, offset, length, array) do { \
  if (((array)->array = buf) != NULL) { \
    int allocSize = sizeof(NTYPE) * (length + ((flags & ARRAY_NULTERMINATE) != 0 ? 1 : 0)); \
    if (((array)->elems = allocStack(stackAllocator, allocSize)) != NULL) { \
        (array)->stack = 1; \
    } else { \
        (array)->elems = malloc(allocSize); \
        (array)->stack = 0; \
    } \
    if ((flags & (ARRAY_IN | ARRAY_OUT)) != ARRAY_OUT) { \
        (*env)->Get##JTYPE##ArrayRegion(env, obj, offset, length, (NTYPE *) array->elems); \
    } \
    (array)->release = release##JTYPE##Array; \
  } \
} while(0)

void*
jffi_getArray(JNIEnv* env, jobject buf, jsize offset, jsize length, int paramType,
        StackAllocator* stackAllocator, Array* array) 
{
    if (buf == NULL) {
        return NULL;
    }

#ifdef notyet
    if ((paramType & ARRAY_PINNED) != 0) {
        return jffi_getArrayCritical(env, buf, offset, length, paramType, array);
    }
#endif
    
    switch (paramType & ARGPRIM_MASK) {
    case com_kenai_jffi_ObjectBuffer_BYTE:
        ARRAY(Byte, jbyte, paramType, buf, offset, length, array);
        // If the array was really a string, nul terminate it
        if ((paramType & (ARRAY_NULTERMINATE | ARRAY_IN | ARRAY_OUT)) != ARRAY_OUT) {
            if (array->elems != NULL) {
                *(((char *) array->elems) + length) = '\0';
            }
        }
        break;
    case com_kenai_jffi_ObjectBuffer_SHORT:
        ARRAY(Short, jshort, paramType, buf, offset, length, array);
        break;
    case com_kenai_jffi_ObjectBuffer_INT:
        ARRAY(Int, jint, paramType, buf, offset, length, array);
        break;
    case com_kenai_jffi_ObjectBuffer_LONG:
        ARRAY(Long, jlong, paramType, buf, offset, length, array);
        break;
    case com_kenai_jffi_ObjectBuffer_FLOAT:
        ARRAY(Float, jfloat, paramType, buf, offset, length, array);
        break;
    case com_kenai_jffi_ObjectBuffer_DOUBLE:
        ARRAY(Double, jdouble, paramType, buf, offset, length, array);
        break;
    default:
        throwException(env, IllegalArgument, "Invalid array type: %#x\n", paramType);
        return NULL;
    }
    array->array = buf;
    array->offset = offset;
    array->length = length;
    
    /* If its an IN-only array, don't bother copying the native data back. */
    array->mode = ((paramType & (ARRAY_IN | ARRAY_OUT)) == ARRAY_IN) ? JNI_ABORT : 0;
    return array->elems;
}

static void 
jffi_releaseCriticalArray(JNIEnv* env, Array *array)
{
    (*env)->ReleasePrimitiveArrayCritical(env, array->array, array->elems, array->mode);
}

void*
jffi_getArrayCritical(JNIEnv* env, jobject buf, jsize offset, jsize length, int paramType, struct Array* array)
{
    if (buf == NULL) {
        return NULL;
    }
    array->array = buf;
    array->elems = (*env)->GetPrimitiveArrayCritical(env, array->array, NULL);
    array->release = jffi_releaseCriticalArray;
    /* If its an IN-only array, don't bother copying the native data back. */
    array->mode = ((paramType & (ARRAY_IN | ARRAY_OUT)) == ARRAY_IN) ? JNI_ABORT : 0;
    return array->elems != NULL ? (char *) array->elems + offset : NULL;
}

