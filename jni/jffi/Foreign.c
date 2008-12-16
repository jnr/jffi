#include <stdlib.h>
#include <ffi.h>
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "CallContext.h"
#include "com_kenai_jffi_Foreign.h"

#if defined(__i386__)
#  define USE_RAW 1
#endif

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    callVrI
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_callVrI(JNIEnv*env, jobject self, jlong ctxAddress, jlong functionAddress)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    void* function = j2p(functionAddress);
    long retval;
#ifdef USE_RAW
    int arg0;
    ffi_raw_call(&ctx->cif, FFI_FN(function), &retval, (ffi_raw *) &arg0);
#else
    int arg0;
    void* ffiValues[1] = &arg0;
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#endif
    return retval;
}

typedef union Param {
    int i;
    int8_t s8;
    uint8_t u8;
    int16_t s16;
    uint16_t u16;
    int32_t s32;
    uint32_t u32;
    int64_t s64;
    uint64_t u64;
    float f;
    double d;
    void* p;
} Param;
static inline void
set_int32_param(int type, int32_t arg, Param* v)
{
    switch (type) {
        case FFI_TYPE_INT: v->i = arg; break;
        case FFI_TYPE_SINT8: v->s8 = arg; break;
        case FFI_TYPE_UINT8: v->u8 = arg; break;
        case FFI_TYPE_SINT16: v->s16 = arg; break;
        case FFI_TYPE_UINT16: v->u16 = arg; break;
        case FFI_TYPE_SINT32: v->s32 = arg; break;
        case FFI_TYPE_UINT32: v->u32 = arg; break;
        case FFI_TYPE_SINT64: v->s64 = arg; break;
        case FFI_TYPE_UINT64: v->u64 = arg; break;
        case FFI_TYPE_POINTER: v->p = (void *)(intptr_t)arg; break;
    }
}
/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    callVrI
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_callIrI(JNIEnv*env, jobject self, jlong ctxAddress, jlong functionAddress,
        int arg1)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    void* function = j2p(functionAddress);
    long retval;
#ifdef USE_RAW
    ffi_raw_call(&ctx->cif, FFI_FN(function), &retval, (ffi_raw *) &arg1);
#else
    Param v1;
    void* ffiValues[1] = &v1;
    set_int32_param(ctx->ffiParamTypes[0]->type, arg1, &v1);
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#endif
    return retval;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    callIIrI
 * Signature: (JJII)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_callIIrI(JNIEnv*env, jobject self, jlong ctxAddress, jlong functionAddress,
        int arg1, int arg2)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    void* function = j2p(functionAddress);
    long retval;
#ifdef USE_RAW
    int params[] = { arg1, arg2 };
    ffi_raw_call(&ctx->cif, FFI_FN(function), &retval, (ffi_raw *) params);
#else
    Param v1, v2;
    void* ffiValues[] = { &v1, &v2 };
    set_int32_param(ctx->ffiParamTypes[0]->type, arg1, &v1);
    set_int32_param(ctx->ffiParamTypes[1]->type, arg2, &v2);
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#endif
    return retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_callIIIrI(JNIEnv*env, jobject self, jlong ctxAddress, jlong functionAddress,
        int arg1, int arg2, int arg3)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    void* function = j2p(functionAddress);
    long retval;
#ifdef USE_RAW
    int params[] = { arg1, arg2, arg3 };
    ffi_raw_call(&ctx->cif, FFI_FN(function), &retval, (ffi_raw *) params);
#else
    Param v1, v2, v3;
    void* ffiValues[] = { &v1, &v2, &v3 };
    set_int32_param(ctx->ffiParamTypes[0]->type, arg1, &v1);
    set_int32_param(ctx->ffiParamTypes[1]->type, arg2, &v2);
    set_int32_param(ctx->ffiParamTypes[2]->type, arg3, &v3);
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#endif
    return retval;
}
