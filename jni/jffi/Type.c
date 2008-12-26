#include <stdlib.h>
#include <ffi.h>
#include <jni.h>
#include "jffi.h"

ffi_type*
jffi_TypeToFFI(int type)
{
    switch (type) {
        case FFI_TYPE_VOID: return &ffi_type_void;
        case FFI_TYPE_INT: return &ffi_type_sint;
        case FFI_TYPE_FLOAT:return &ffi_type_float;
        case FFI_TYPE_DOUBLE: return &ffi_type_double;
        case FFI_TYPE_UINT8: return &ffi_type_uint8;
        case FFI_TYPE_SINT8: return &ffi_type_sint8;
        case FFI_TYPE_UINT16: return &ffi_type_uint16;
        case FFI_TYPE_SINT16: return &ffi_type_sint16;
        case FFI_TYPE_UINT32: return &ffi_type_uint32;
        case FFI_TYPE_SINT32: return &ffi_type_sint32;
        case FFI_TYPE_UINT64: return &ffi_type_uint64;
        case FFI_TYPE_SINT64: return &ffi_type_sint64;
        case FFI_TYPE_POINTER: return &ffi_type_pointer;
    }
    return NULL;
}
