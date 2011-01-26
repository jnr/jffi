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

