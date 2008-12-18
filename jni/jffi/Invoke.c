#include <sys/types.h>
#include <stdlib.h>
#include <endian.h>
#include <ffi.h>
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "Function.h"
#include "com_kenai_jffi_Foreign.h"

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invoke64Array
 * Signature: (J[B)V
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeArrayInt(JNIEnv* env, jclass self, jlong ctxAddress,
        jbyteArray paramBuffer)
{
    Function* ctx = (Function *) (uintptr_t) ctxAddress;
    jbyte* tmpBuffer = alloca(ctx->cif.nargs * 8);
    void** ffiArgs = alloca(ctx->cif.nargs * sizeof(void *));
    long retval;
    unsigned int i;
    for (i = 0; i < ctx->cif.nargs; ++i) {
        ffiArgs[i] = tmpBuffer + (i * 8);
    }
    (*env)->GetByteArrayRegion(env, paramBuffer, 0, ctx->cif.nargs * 8, tmpBuffer);
    ffi_call(&ctx->cif, FFI_FN(ctx->function), &retval, ffiArgs);
    return retval;
}
