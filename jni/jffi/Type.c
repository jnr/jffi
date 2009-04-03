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
