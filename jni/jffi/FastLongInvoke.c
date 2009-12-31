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


/* for return values <= sizeof(long), need to use an ffi_sarg sized return value */
#define RETVAL(retval, rtype) ((rtype)->size > sizeof(ffi_sarg) ? (retval).j : (retval).sarg)

#if defined(__x86_64__)
# define LONG_BYPASS_FFI
#endif

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrL
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeVrL(JNIEnv* env, jobject self, jlong ctxAddress)
{
    Function* ctx = (Function *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(void)) (ctx->function))(); \
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;
    
    ffi_call0(ctx, ctx->function, &retval);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLrL
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeLrL(JNIEnv* env, jobject self, jlong ctxAddress, jlong arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong)) (ctx->function))(arg1); \
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call1(ctx, ctx->function, &retval, arg1);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLrL
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeLLrL(JNIEnv* env, jobject self, jlong ctxAddress, jlong arg1, jlong arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong)) (ctx->function))(arg1, arg2); \
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call2(ctx, ctx->function, &retval, arg1, arg2);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
#endif
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
    Function* ctx = (Function *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong, jlong)) (ctx->function))(arg1, arg2, arg3);
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call3(ctx, ctx->function, &retval, arg1, arg2, arg3);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
#endif
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeLLLLrL(JNIEnv* env, jobject self, jlong ctxAddress,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4)
{
    Function* ctx = (Function *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong, jlong, jlong)) (ctx->function))(arg1, arg2, arg3, arg4);
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call4(ctx, ctx->function, &retval, arg1, arg2, arg3, arg4);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
#endif
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeLLLLLrL(JNIEnv* env, jobject self, jlong ctxAddress,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5)
{
    Function* ctx = (Function *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong, jlong, jlong, jlong)) (ctx->function))(arg1, arg2, arg3, arg4, arg5);
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call5(ctx, ctx->function, &retval, arg1, arg2, arg3, arg4, arg5);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
#endif
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeLLLLLLrL(JNIEnv* env, jobject self, jlong ctxAddress,
        jlong arg1, jlong arg2, jlong arg3, jlong arg4, jlong arg5, jlong arg6)
{
    Function* ctx = (Function *) j2p(ctxAddress);

#ifdef LONG_BYPASS_FFI
    jlong retval = ((jlong (*)(jlong, jlong, jlong, jlong, jlong, jlong)) (ctx->function))(arg1, arg2, arg3, arg4, arg5, arg6);
    SAVE_ERRNO(ctx);
    return retval;
#else
    FFIValue retval;

    ffi_call6(ctx, ctx->function, &retval, arg1, arg2, arg3, arg4, arg5, arg6);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
#endif
}
