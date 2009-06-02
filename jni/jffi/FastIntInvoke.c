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

typedef unsigned int u32;

#ifndef BYTE_ORDER
# error "BYTE_ORDER not defined"
#endif

#if BYTE_ORDER == LITTLE_ENDIAN
#  define ARGPTR(argp, type) (argp)
#elif BYTE_ORDER == BIG_ENDIAN
#  define ARGPTR(argp, type) (((caddr_t) (argp)) + sizeof(*argp) - (type)->size)
#else
#  error "Unsupported BYTE_ORDER"
#endif

#if defined(BYPASS_FFI)
# define invokeVrI(ctx, retval) do { \
        *(int *)(retval) = ((jint (*)()) (ctx)->function)(); \
    } while (0)

# define invokeIrI(ctx, retval, arg1) do { \
        *(int *)(retval) = ((jint (*)(jint)) (ctx)->function)(arg1); \
    } while (0)

# define invokeIIrI(ctx, retval, arg1, arg2) do { \
        *(int *)(retval) = ((jint (*)(jint, jint)) (ctx)->function)((arg1), (arg2)); \
    } while (0)

# define invokeIIIrI(ctx, retval, arg1, arg2, arg3) do { \
        *(int *)(retval) = ((jint (*)(jint, jint, jint)) (ctx)->function)(arg1, arg2, arg3); \
    } while (0)

#elif defined(USE_RAW) && defined(__i386__)

# define invokeVrI(ctx, retval) do { \
        FFIValue arg0; \
        void* ffiValues[] = { &arg0 }; \
        ffi_call(&(ctx)->cif, FFI_FN((ctx)->function), (retval), ffiValues); \
    } while (0)

# define invokeIrI(ctx, retval, arg1) do { \
        ffi_raw_call(&(ctx)->cif, FFI_FN((ctx)->function), (retval), (ffi_raw *) &arg1); \
    } while (0)

# define invokeIIrI(ctx, retval, arg1, arg2) do { \
        ffi_raw_call(&(ctx)->cif, FFI_FN((ctx)->function), (retval), (ffi_raw *) &arg1); \
    } while (0)

# define invokeIIIrI(ctx, retval, arg1, arg2, arg3) do { \
        void* ffiValues[] = { &arg1, &arg2, &arg3 }; \
        ffi_call(&ctx->cif, FFI_FN(ctx->function), (retval), ffiValues); \
    } while (0)

#else /* Anything that is BIG endian or non-i386 little endian */

# define invokeVrI(ctx, retval) do { \
        FFIValue arg0; \
        void* ffiValues[] = { &arg0 }; \
        ffi_call(&(ctx)->cif, FFI_FN((ctx)->function), (retval), ffiValues); \
    } while (0)

# define invokeIrI(ctx, retval, arg1) do { \
        void* ffiValues[] = {  ARGPTR(&(arg1), (ctx)->cif.arg_types[0]) }; \
        ffi_call(&(ctx)->cif, FFI_FN((ctx)->function), (retval), ffiValues); \
    } while (0)

# define invokeIIrI(ctx, retval, arg1, arg2) do {\
        void* ffiValues[] = { \
            ARGPTR(&arg1, (ctx)->cif.arg_types[0]), \
            ARGPTR(&arg2, (ctx)->cif.arg_types[1]) \
        }; \
        ffi_call(&(ctx)->cif, FFI_FN((ctx)->function), (retval), ffiValues); \
    } while (0)

# define invokeIIIrI(ctx, retval, arg1, arg2, arg3) do { \
        void* ffiValues[] = { \
            ARGPTR(&arg1, (ctx)->cif.arg_types[0]), \
            ARGPTR(&arg2, (ctx)->cif.arg_types[1]), \
            ARGPTR(&arg3, (ctx)->cif.arg_types[2]) \
        }; \
        ffi_call(&(ctx)->cif, FFI_FN((ctx)->function), (retval), ffiValues); \
    } while (0)
#endif


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrI
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeVrI(JNIEnv* env, jclass self, jlong ctxAddress)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;
    invokeVrI(ctx, &retval);
    SAVE_ERRNO(ctx);

    return (int) retval;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    invokeVrI
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoVrI(JNIEnv* env, jclass self, jlong ctxAddress)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;
    invokeVrI(ctx, &retval);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIrI(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIrI(ctx, &retval, arg1);
    SAVE_ERRNO(ctx);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoIrI(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIrI(ctx, &retval, arg1);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIIrI(ctx, &retval, arg1, arg2);
    SAVE_ERRNO(ctx);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIIrI(ctx, &retval, arg1, arg2);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIIIrI(ctx, &retval, arg1, arg2, arg3);
    SAVE_ERRNO(ctx);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIIIrI(ctx, &retval, arg1, arg2, arg3);

    return (int) retval;
}

