#include <sys/types.h>
#include <stdlib.h>
#include <errno.h>
#include <ffi.h>
#include <jni.h>
#include "endian.h"
#include "jffi.h"
#include "Exception.h"
#include "Function.h"
#include "LastError.h"
#include "com_kenai_jffi_Foreign.h"


#if BYTE_ORDER == BIG_ENDIAN
#  define ARGPTR(argp, type) (((caddr_t) (argp)) + sizeof(*argp) - (type)->size)
#else
#  define ARGPTR(argp, type) (argp)
#endif
/* for return values <= sizeof(long), need to use an ffi_sarg sized return value */
#define RETVAL(retval, rtype) ((rtype)->size > sizeof(ffi_sarg) ? (retval).j : (retval).sarg)

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrL
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeVrL(JNIEnv* env, jobject self, jlong ctxAddress)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    ffi_cif *cif = &ctx->cif;
    FFIValue retval, arg0;
    void* ffiValues[] = { &arg0 };
    ffi_call(cif, FFI_FN(ctx->function), &retval, ffiValues);
    set_last_error(errno);

    return RETVAL(retval, cif->rtype);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLrL
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeLrL(JNIEnv* env, jobject self, jlong ctxAddress, jlong arg1)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    ffi_cif *cif = &ctx->cif;
    FFIValue retval;
    void* ffiValues[] = { 
        ARGPTR(&arg1, cif->arg_types[0])
    };
    ffi_call(cif, FFI_FN(ctx->function), &retval, ffiValues);
    set_last_error(errno);

    return RETVAL(retval, cif->rtype);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLrL
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeLLrL(JNIEnv* env, jobject self, jlong ctxAddress, jlong arg1, jlong arg2)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    ffi_cif *cif = &ctx->cif;
    FFIValue retval;
    void* ffiValues[] = {
        ARGPTR(&arg1, cif->arg_types[0]),
        ARGPTR(&arg2, cif->arg_types[1])
    };
    ffi_call(cif, FFI_FN(ctx->function), &retval, ffiValues);
    set_last_error(errno);

    return RETVAL(retval, cif->rtype);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLLrL
 * Signature: (JJJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeLLLrL(JNIEnv* env, jobject self, jlong ctxAddress,
        jlong arg1, jlong arg2, jlong arg3)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    ffi_cif *cif = &ctx->cif;
    FFIValue retval;
    void* ffiValues[] = {
        ARGPTR(&arg1, cif->arg_types[0]),
        ARGPTR(&arg2, cif->arg_types[1]),
        ARGPTR(&arg3, cif->arg_types[2])
    };
    ffi_call(cif, FFI_FN(ctx->function), &retval, ffiValues);
    set_last_error(errno);

    return RETVAL(retval, cif->rtype);
}
