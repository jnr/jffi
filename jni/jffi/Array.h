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

#ifndef jffi_Array_h
#define jffi_Array_h

typedef struct Array {
    jobject array;
    void* elems;
    void* result;
    jsize offset;
    jsize length;
    int mode;
    int stack;
    void (*release)(JNIEnv *env, struct Array *);
} Array;

extern void* jffi_getArray(JNIEnv* env, jobject buf, jint offset, jint length, int type, StackAllocator* alloc, struct Array* array);
extern void* jffi_getArrayCritical(JNIEnv* env, jobject buf, jint offset, jint length, int type, struct Array* array);

#include "com_kenai_jffi_ObjectBuffer.h"

#define OBJ_INDEX_MASK com_kenai_jffi_ObjectBuffer_INDEX_MASK
#define OBJ_INDEX_SHIFT com_kenai_jffi_ObjectBuffer_INDEX_SHIFT

#endif /* jffi_Array_h */

