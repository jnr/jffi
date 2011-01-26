/* 
 * Copyright (C) 2008, 2009 Wayne Meissner
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

#include <stdlib.h>
#include <ffi.h>
#include <jni.h>
#include "com_kenai_jffi_Foreign.h"
#include "jffi.h"

static ffi_type*
typeToFFI(int type)
{
    switch (type) {
        case com_kenai_jffi_Foreign_TYPE_VOID: return &ffi_type_void;
        case com_kenai_jffi_Foreign_TYPE_FLOAT:return &ffi_type_float;
        case com_kenai_jffi_Foreign_TYPE_DOUBLE: return &ffi_type_double;
        case com_kenai_jffi_Foreign_TYPE_LONGDOUBLE: return &ffi_type_longdouble;
        case com_kenai_jffi_Foreign_TYPE_UINT8: return &ffi_type_uint8;
        case com_kenai_jffi_Foreign_TYPE_SINT8: return &ffi_type_sint8;
        case com_kenai_jffi_Foreign_TYPE_UINT16: return &ffi_type_uint16;
        case com_kenai_jffi_Foreign_TYPE_SINT16: return &ffi_type_sint16;
        case com_kenai_jffi_Foreign_TYPE_UINT32: return &ffi_type_uint32;
        case com_kenai_jffi_Foreign_TYPE_SINT32: return &ffi_type_sint32;
        case com_kenai_jffi_Foreign_TYPE_UINT64: return &ffi_type_uint64;
        case com_kenai_jffi_Foreign_TYPE_SINT64: return &ffi_type_sint64;
        case com_kenai_jffi_Foreign_TYPE_POINTER: return &ffi_type_pointer;
        case com_kenai_jffi_Foreign_TYPE_UCHAR: return &ffi_type_uchar;
        case com_kenai_jffi_Foreign_TYPE_SCHAR: return &ffi_type_schar;
        case com_kenai_jffi_Foreign_TYPE_USHORT: return &ffi_type_ushort;
        case com_kenai_jffi_Foreign_TYPE_SSHORT: return &ffi_type_sshort;
        case com_kenai_jffi_Foreign_TYPE_UINT: return &ffi_type_uint;
        case com_kenai_jffi_Foreign_TYPE_SINT: return &ffi_type_sint;
        case com_kenai_jffi_Foreign_TYPE_ULONG: return &ffi_type_ulong;
        case com_kenai_jffi_Foreign_TYPE_SLONG: return &ffi_type_slong;
    }
    return NULL;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    lookupType
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_lookupBuiltinType(JNIEnv* env, jobject self, jint type)
{
    return p2j(typeToFFI(type));
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getTypeSize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_getTypeSize(JNIEnv* env, jobject self, jlong handle)
{
    return ((ffi_type *) j2p(handle))->size;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getTypeAlign
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_kenai_jffi_Foreign_getTypeAlign(JNIEnv* env, jobject self, jlong handle)
{
    return ((ffi_type *) j2p(handle))->alignment;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getTypeType
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_getTypeType(JNIEnv* env, jobject self, jlong handle)
{
    return ((ffi_type *) j2p(handle))->type;
}
