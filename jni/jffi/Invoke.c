#include <sys/types.h>
#include <stdlib.h>
#include <endian.h>
#include <ffi.h>
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "Function.h"
#include "com_kenai_jffi_Foreign.h"

#define PARAM_SIZE (8)
#define MAX_STACK_ARGS (8)
static inline void
invokeArray(JNIEnv* env, jlong ctxAddress, jbyteArray paramBuffer, FFIValue* retval)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    jbyte tmpStackBuffer[MAX_STACK_ARGS * PARAM_SIZE], *tmpBuffer = &tmpStackBuffer[0];
    void* ffiStackArgs[MAX_STACK_ARGS];
    void** ffiArgs = ffiStackArgs;
    unsigned int i;
    if (ctx->cif.nargs > MAX_STACK_ARGS) {
        tmpBuffer = alloca(ctx->cif.nargs * PARAM_SIZE);
        ffiArgs = alloca(ctx->cif.nargs * sizeof(void *));
    }
    for (i = 0; i < ctx->cif.nargs; ++i) {
        ffiArgs[i] = &tmpBuffer[i * 8];
    }
    if (ctx->cif.nargs > 0) {
        (*env)->GetByteArrayRegion(env, paramBuffer, 0, ctx->cif.nargs * PARAM_SIZE, tmpBuffer);
    }
    ffi_call(&ctx->cif, FFI_FN(ctx->function), retval, ffiArgs);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayInt32
 * Signature: (J[B)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayInt32(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArray(env, ctxAddress, paramBuffer, &retval);
#if BYTE_ORDER == LITTLE_ENDIAN
    return retval.s32;
#else
    return retval.l & 0xFFFFFFFFL;
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayInt64
 * Signature: (J[B)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayInt64(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArray(env, ctxAddress, paramBuffer, &retval);
    return retval.s64;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayFloat
 * Signature: (J[B)F
 */
JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayFloat(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArray(env, ctxAddress, paramBuffer, &retval);
    return retval.f;
}
/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeArrayDouble
 * Signature: (J[B)D
 */
JNIEXPORT jdouble JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayDouble(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer)
{
    FFIValue retval;
    invokeArray(env, ctxAddress, paramBuffer, &retval);
    return retval.d;
}

