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

//
// WARNING! Do not change the layout of this struct, it may be used from assembler
//
typedef struct Array {
    void (*release)(JNIEnv *env, struct Array *);
    jobject array;
    void* elems;    
    int offset;
    int length;
} Array;

extern void* jffi_getArrayBuffer(JNIEnv* env, jobject buf, jint offset, jint length, int type, struct Array* array, void* buffer);
extern void* jffi_getArrayHeap(JNIEnv* env, jobject buf, jint offset, jint length, int type, struct Array* array);
extern void* jffi_getArrayCritical(JNIEnv* env, jobject buf, jint offset, jint length, int type, struct Array* array);
extern int jffi_arraySize(int length, int type);

#include "com_kenai_jffi_ObjectBuffer.h"

#define OBJ_INDEX_MASK com_kenai_jffi_ObjectBuffer_INDEX_MASK
#define OBJ_INDEX_SHIFT com_kenai_jffi_ObjectBuffer_INDEX_SHIFT

#endif /* jffi_Array_h */

