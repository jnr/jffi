#include <sys/types.h>
#include <stdlib.h>
#ifdef __linux__
#  include <endian.h>
#endif
#include <errno.h>
#include <ffi.h>
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "Function.h"
#include "LastError.h"
#include "com_kenai_jffi_Foreign.h"

typedef unsigned int u32;

static inline jint
invokeVrI(ffi_cif* cif, void* function)
{
#if defined(BYPASS_FFI)
    jint retval = ((jint (*)()) function)();
    set_last_error(errno);
    return retval;
#else
    FFIValue retval, arg0;
    void* ffiValues[] = { &arg0 };
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
    set_last_error(errno);
    return_int(retval);
#endif
}


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    callVrI
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invoke32VrI(JNIEnv* env, jclass self, jint ctxAddress)
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
Java_com_kenai_jffi_Foreign_invoke64VrI(JNIEnv* env, jclass self, jlong ctxAddress)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    return invokeVrI(&ctx->cif, ctx->function);
}

#if BYTE_ORDER == BIG_ENDIAN
#  define ARGPTR(argp, type) (((caddr_t) (argp)) + sizeof(*argp) - (type)->size)
#else
#  define ARGPTR(argp, type) (argp)
#endif
static inline jint
invokeIrI(ffi_cif* cif, void* function, jint arg1)
{
#if defined(BYPASS_FFI)
    jint retval = ((jint (*)(jint)) function)(arg1);
    set_last_error(errno);
    return retval;
#else
    FFIValue retval;
# if defined(USE_RAW) && defined(__i386__)
    ffi_raw_call(cif, FFI_FN(function), &retval, (ffi_raw *) &arg1);
# else
    void* ffiValues[] = { ARGPTR(&arg1, cif->arg_types[0]) };
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
# endif
    set_last_error(errno);
    return_int(retval);
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    call32IrI
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invoke32IrI(JNIEnv*env, jobject self, jint ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) (uintptr_t)ctxAddress;
    return invokeIrI(&ctx->cif, ctx->function, arg1);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    call64IrI
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invoke64IrI(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) (uintptr_t)ctxAddress;
    return invokeIrI(&ctx->cif, ctx->function, arg1);
}

static inline jint
invokeIIrI(ffi_cif* cif, void* function, jint arg1, jint arg2)
{
#if defined(BYPASS_FFI)
    jint retval = ((jint (*)(jint, jint)) function)(arg1, arg2);
    set_last_error(errno);
    return retval;
#else
    FFIValue retval;
# if defined(USE_RAW) && defined(__i386__)
    jint raw[] = { arg1, arg2 };
    ffi_raw_call(cif, FFI_FN(function), &retval, (ffi_raw *) raw);
# else
    void* ffiValues[] = {
        ARGPTR(&arg1, cif->arg_types[0]),
        ARGPTR(&arg2, cif->arg_types[1])
    };
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
# endif
    set_last_error(errno);
    return_int(retval);
#endif
}
/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    callIIrI
 * Signature: (JJII)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invoke32IIrI(JNIEnv*env, jobject self, jint ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    return invokeIIrI(&ctx->cif, ctx->function, arg1, arg2);
}
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invoke64IIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    return invokeIIrI(&ctx->cif, ctx->function, arg1, arg2);
}

static inline jint
invokeIIIrI(ffi_cif* cif, void* function, jint arg1, jint arg2, jint arg3)
{
#if defined(BYPASS_FFI)
    jint retval = ((jint (*)(jint, jint, jint)) function)(arg1, arg2, arg3);
    set_last_error(errno);
    return retval;
#else
    FFIValue retval;
# if defined(USE_RAW) && defined(__i386__)
    jint raw[] = { arg1, arg2, arg3 };
    ffi_raw_call(cif, FFI_FN(function), &retval, (ffi_raw *) raw);
# else
    void* ffiValues[] = { 
        ARGPTR(&arg1, cif->arg_types[0]),
        ARGPTR(&arg2, cif->arg_types[1]),
        ARGPTR(&arg3, cif->arg_types[2])
    };
    ffi_call(cif, FFI_FN(function), &retval, ffiValues);
# endif
    set_last_error(errno);
    return_int(retval);
#endif
}
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invoke32IIIrI(JNIEnv*env, jobject self, jint ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    return invokeIIIrI(&ctx->cif, ctx->function, arg1, arg2, arg3);
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invoke64IIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    return invokeIIIrI(&ctx->cif, ctx->function, arg1, arg2, arg3);
}

