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

#ifndef jffi_Array_h
#define jffi_Array_h

//
// WARNING! Do not change the layout of this struct, it may be used from assembler
//
typedef struct Array {
    void (JNICALL *copyin)(JNIEnv* env, jobject array, jsize start, jsize len, void *buf);
    void (JNICALL *copyout)(JNIEnv* env, jobject array, jsize start, jsize len, const void *buf);
    void (*release)(JNIEnv *env, struct Array *);
    jobject array;
    void* elems;    
    int offset;
    int length;
    int type;
    int bytesize; // total size of array in bytes
} Array;

extern bool jffi_getInitArray(JNIEnv* env, jobject buf, jint offset, jint length, int type, struct Array* array);
extern void* jffi_getArrayBuffer(JNIEnv* env, jobject buf, jint offset, jint length, int type, struct Array* array, void* buffer);
extern void* jffi_getArrayHeap(JNIEnv* env, jobject buf, jint offset, jint length, int type, struct Array* array);
extern void* jffi_getArrayCritical(JNIEnv* env, jobject buf, jint offset, jint length, int type, struct Array* array);
extern int jffi_arraySize(int length, int type);
extern void jffi_releaseArrays(JNIEnv* env, Array* arrays, int arrayCount);

#include "com_kenai_jffi_ObjectBuffer.h"

#define OBJ_INDEX_MASK com_kenai_jffi_ObjectBuffer_INDEX_MASK
#define OBJ_INDEX_SHIFT com_kenai_jffi_ObjectBuffer_INDEX_SHIFT
#define ARRAY_NULTERMINATE com_kenai_jffi_ObjectBuffer_ZERO_TERMINATE
#define ARRAY_IN com_kenai_jffi_ObjectBuffer_IN
#define ARRAY_OUT com_kenai_jffi_ObjectBuffer_OUT
#define ARRAY_PINNED com_kenai_jffi_ObjectBuffer_PINNED
#define ARRAY_CLEAR com_kenai_jffi_ObjectBuffer_CLEAR

#define IS_PINNED_ARRAY(flags) \
        (((flags) & (com_kenai_jffi_ObjectBuffer_ARRAY | com_kenai_jffi_ObjectBuffer_PINNED)) == (com_kenai_jffi_ObjectBuffer_ARRAY | com_kenai_jffi_ObjectBuffer_PINNED))

#define IS_UNPINNED_ARRAY(flags) \
        (((flags) & (com_kenai_jffi_ObjectBuffer_ARRAY | com_kenai_jffi_ObjectBuffer_PINNED)) == com_kenai_jffi_ObjectBuffer_ARRAY)

#define IS_OUT_ARRAY(flags) (((flags) & (com_kenai_jffi_ObjectBuffer_ARRAY | ARRAY_IN | ARRAY_OUT)) != (com_kenai_jffi_ObjectBuffer_ARRAY | ARRAY_IN))
#define IS_IN_ARRAY(flags) (((flags) & (com_kenai_jffi_ObjectBuffer_ARRAY | ARRAY_IN | ARRAY_OUT)) != (com_kenai_jffi_ObjectBuffer_ARRAY | ARRAY_OUT))

#define RELEASE_ARRAYS(env, arrays, arrayCount) do { if (unlikely(arrayCount > 0)) jffi_releaseArrays(env, arrays, arrayCount); } while (0)

#endif /* jffi_Array_h */

