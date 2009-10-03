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
Java_com_kenai_jffi_Foreign_invokeVrN(JNIEnv* env, jobject self, jlong ctxAddress)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    ffi_call0(ctx, ctx->function, &retval);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLrL
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeNrN(JNIEnv* env, jobject self, jlong ctxAddress, jlong arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    ffi_call1(ctx, ctx->function, &retval, arg1);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLrL
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeNNrN(JNIEnv* env, jobject self, jlong ctxAddress, jlong arg1, jlong arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    ffi_call2(ctx, ctx->function, &retval, arg1, arg2);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeLLLrL
 * Signature: (JJJJ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeNNNrN(JNIEnv* env, jobject self, jlong ctxAddress,
        jlong arg1, jlong arg2, jlong arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    FFIValue retval;

    ffi_call3(ctx, ctx->function, &retval, arg1, arg2, arg3);
    SAVE_ERRNO(ctx);

    return RETVAL(retval, ctx->cif.rtype);
}
