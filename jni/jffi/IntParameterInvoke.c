#include <sys/types.h>
#include <stdlib.h>
#include <endian.h>
#include <ffi.h>
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "Function.h"
#include "com_kenai_jffi_Foreign.h"

#if defined(__i386__) && 0
#  define USE_RAW 1
#endif

typedef union FFIValue {
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
} FFIValue;

static inline int
invokeVrI(ffi_cif* cif, void* function)
{
    long retval;
#if defined(__i386__) && 1
    retval = ((int (*)()) function)();
#elif defined(USE_RAW) && 0 /* for zero args, non-raw is marginally faster */
    int arg0;
    ffi_raw_call(cif, FFI_FN(function), &retval, (ffi_raw *) &arg0);
#else
    int arg0;
    void* ffiValues[] = { &arg0 };
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#endif
    return retval;
}


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    callVrI
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_call32VrI(JNIEnv* env, jclass self, jint ctxAddress)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    return invokeVrI(&ctx->cif, ctx->function);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    call64VrI
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_call64VrI(JNIEnv* env, jclass self, jlong ctxAddress)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    return invokeVrI(&ctx->cif, ctx->function);
}

static inline void
set_int32_param(int type, int32_t arg, FFIValue* v)
{
    switch (type) {
        case FFI_TYPE_INT: v->i = arg; break;
        case FFI_TYPE_SINT8: v->s8 = arg; break;
        case FFI_TYPE_UINT8: v->u8 = arg; break;
        case FFI_TYPE_SINT16: v->s16 = arg; break;
        case FFI_TYPE_UINT16: v->u16 = arg; break;
        case FFI_TYPE_SINT32: v->s32 = arg; break;
        case FFI_TYPE_UINT32: v->u32 = arg; break;
        case FFI_TYPE_POINTER: v->p = (void *)(intptr_t)arg; break;
    }
}

static inline int
invokeIrI(ffi_cif* cif, void* function, ffi_type** ffiParamTypes, int arg1)
{
    long retval;
#if defined(__i386__)
    retval = ((int (*)(int)) function)(arg1);
#elif defined(USE_RAW) && defined(__i386__)
    ffi_raw_call(cif, FFI_FN(function), &retval, (ffi_raw *) &arg1);
#elif BYTE_ORDER == LITTLE_ENDIAN
    void* ffiValues[] = { &arg1 };
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#else
    FFIValue v1;
    void* ffiValues[] = { &v1 };
    set_int32_param(ffiParamTypes[0]->type, arg1, &v1);
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#endif
    return retval;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    call32IrI
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_call32IrI(JNIEnv*env, jobject self, jint ctxAddress,
        int arg1)
{
    Function* ctx = (Function *) (uintptr_t)ctxAddress;
    return invokeIrI(&ctx->cif, ctx->function, ctx->ffiParamTypes, arg1);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    call64IrI
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_call64IrI(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) (uintptr_t)ctxAddress;
    return invokeIrI(&ctx->cif, ctx->function, ctx->ffiParamTypes, arg1);
}

static inline int
invokeIIrI(ffi_cif* cif, void* function, ffi_type** ffiParamTypes, int arg1, int arg2)
{
    long retval;
#if defined(__i386__)
    retval = ((int (*)(int, int)) function)(arg1, arg2);
#elif defined(USE_RAW) && defined(__i386__)
    ffi_raw_call(cif, FFI_FN(function), &retval, (ffi_raw *) &arg1);
#elif BYTE_ORDER == LITTLE_ENDIAN
    void* ffiValues[] = { &arg1, &arg2 };
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#else
    FFIValue v1, v2;
    void* ffiValues[] = { &v1, &v2 };
    set_int32_param(ffiParamTypes[0]->type, arg1, &v1);
    set_int32_param(ffiParamTypes[1]->type, arg2, &v2);
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
Java_com_kenai_jffi_Foreign_callIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        int arg1, int arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    return invokeIIrI(&ctx->cif, ctx->function, ctx->ffiParamTypes, arg1, arg2);
}

static inline int
invokeIIIrI(ffi_cif* cif, void* function, ffi_type** ffiParamTypes, int arg1, int arg2, int arg3)
{
    long retval;
#if defined(__i386__)
    retval = ((int (*)(int, int, int)) function)(arg1, arg2, arg3);
#elif defined(USE_RAW) && defined(__i386__)
    ffi_raw_call(cif, FFI_FN(function), &retval, (ffi_raw *) &arg1);
#elif BYTE_ORDER == LITTLE_ENDIAN
    void* ffiValues[] = { &arg1, &arg2, &arg3 };
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#else
    FFIValue v1, v2, v3;
    void* ffiValues[] = { &v1, &v2, &v3 };
    set_int32_param(ffiParamTypes[0]->type, arg1, &v1);
    set_int32_param(ffiParamTypes[1]->type, arg2, &v2);
    set_int32_param(ffiParamTypes[2]->type, arg3, &v3);
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
#endif
    return retval;
}
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_callIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        int arg1, int arg2, int arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    return invokeIIIrI(&ctx->cif, ctx->function, ctx->ffiParamTypes, arg1, arg2, arg3);
}
