#include <stdlib.h>
#include <ffi.h>
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "CallContext.h"

static inline int FFI_ALIGN(int v, int a) {
    return ((((size_t) v) - 1) | (a - 1)) +1;
}

static ffi_type* getFFIType(int type);
/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    newCallContext
 * Signature: (I[II)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_newCallContext(JNIEnv* env, jobject self,
        jint returnType, jintArray paramArray, jint convention)
{
    CallContext* ctx = NULL;
    int* paramTypes;
    int paramCount, i, rawOffset = 0;
    ffi_type* ffiParamTypes;
    int ffiStatus;
    int abi;

    paramCount = (*env)->GetArrayLength(env, paramArray);
    ctx = calloc(1, sizeof(*ctx));
    if (ctx == NULL) {
        throwException(env, OutOfMemory, "Failed to allocate CallContext");
        goto cleanup;
    }
    ctx->ffiParamTypes = calloc(paramCount, sizeof(ffi_type *));
    if (ctx->ffiParamTypes == NULL) {
        throwException(env, OutOfMemory, "Failed to allocate CallContext#ffiParamTypes");
        goto cleanup;
    }
    ctx->rawParamOffsets = calloc(paramCount, sizeof(*ctx->rawParamOffsets));
    if (ctx->rawParamOffsets == NULL) {
        throwException(env, OutOfMemory, "Failed to allocate CallContext#rawParamOffsets");
        goto cleanup;
    }

    paramTypes = alloca(sizeof(int) * paramCount);
    (*env)->GetIntArrayRegion(env, paramArray, 0, paramCount, paramTypes);

    for (i = 0; i < paramCount; ++i) {
        ffi_type* type = getFFIType(paramTypes[i]);
        if (type == NULL) {
            throwException(env, IllegalArgument, "Invalid parameter type: %#x", paramTypes[i]);
            goto cleanup;
        }
        ctx->ffiParamTypes[i] = type;
        ctx->rawParamOffsets[i] = rawOffset;
        rawOffset += FFI_ALIGN(type->size, FFI_SIZEOF_ARG);
    }
#ifdef _WIN32
    abi = convention != 0 ? FFI_STDCALL : FFI_DEFAULT_ABI;
#else
    abi = FFI_DEFAULT_ABI;
#endif
    ffiStatus = ffi_prep_cif(&ctx->cif, abi, paramCount, getFFIType(returnType),
            ctx->ffiParamTypes);
    switch (ffiStatus) {
        case FFI_OK:
            break;
        case FFI_BAD_TYPEDEF:
            throwException(env, IllegalArgument, "Bad typedef");
            goto cleanup;
        case FFI_BAD_ABI:
            throwException(env, Runtime, "Invalid ABI");
            goto cleanup;
        default:
            throwException(env, Runtime, "Unknown FFI error");
    }
    return p2j(ctx);
cleanup:
    if (ctx != NULL) {
        if (ctx->rawParamOffsets != NULL) {
            free(ctx->rawParamOffsets);
        }
        if (ctx->ffiParamTypes != NULL) {
            free(ctx->ffiParamTypes);
        }
       free(ctx);
    }
    return 0LL;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeCallContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_freeCallContext(JNIEnv* env, jobject self, jlong handle)
{
    CallContext* ctx = (CallContext *) j2p(handle);
    if (ctx != NULL) {
        if (ctx->rawParamOffsets != NULL) {
            free(ctx->rawParamOffsets);
        }
        if (ctx->ffiParamTypes != NULL) {
            free(ctx->ffiParamTypes);
        }
        free(ctx);
    }
}

static ffi_type*
getFFIType(int type)
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
