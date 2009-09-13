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
#if defined(__i386__)
# define INT_BYPASS_FFI
# define FLOAT_BYPASS_FFI
#elif defined(__x86_64__)
# define INT_BYPASS_FFI
#endif

# define ffi_call0(ctx, fn, retval) do { \
        FFIValue arg0; \
        void* ffiValues[] = { &arg0 }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

# define ffi_call1(ctx, fn, retval, arg1) do { \
        void* ffiValues[] = {  ARGPTR(&(arg1), (ctx)->cif.arg_types[0]) }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

# define ffi_call2(ctx, fn, retval, arg1, arg2) do {\
        void* ffiValues[] = { \
            ARGPTR(&arg1, (ctx)->cif.arg_types[0]), \
            ARGPTR(&arg2, (ctx)->cif.arg_types[1]) \
        }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

# define ffi_call3(ctx, fn, retval, arg1, arg2, arg3) do { \
        void* ffiValues[] = { \
            ARGPTR(&arg1, (ctx)->cif.arg_types[0]), \
            ARGPTR(&arg2, (ctx)->cif.arg_types[1]), \
            ARGPTR(&arg3, (ctx)->cif.arg_types[2]) \
        }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

#if defined(INT_BYPASS_FFI)
# define invokeVrI(ctx, fn, retval) do { \
            *(int *)(retval) = ((jint (*)()) (fn))(); \
    } while (0)

# define invokeIrI(ctx, fn, retval, arg1) do { \
            *(int *)(retval) = ((jint (*)(jint)) (fn))(arg1); \
    } while (0)

# define invokeIIrI(ctx, fn, retval, arg1, arg2) do { \
            *(int *)(retval) = ((jint (*)(jint, jint)) (fn))((arg1), (arg2)); \
    } while (0)

# define invokeIIIrI(ctx, fn, retval, arg1, arg2, arg3) do { \
            *(int *)(retval) = ((jint (*)(jint, jint, jint)) (fn))(arg1, arg2, arg3); \
    } while (0)

#else /* non-i386, non-x86_64 */

# define invokeVrI ffi_call0
# define invokeIrI ffi_call1
# define invokeIIrI ffi_call2
# define invokeIIIrI ffi_call3

#endif

#if defined(FLOAT_BYPASS_FFI)
# define invokeVrF(ctx, fn, rvp) \
    *(float *) (rvp) = ((float (*)(void)) (fn))()

# define invokeIrF(ctx, fn, rvp, arg1) \
    *(float *) (rvp) = ((float (*)(int)) (fn))(arg1)

# define invokeIIrF(ctx, fn, rvp, arg1, arg2) \
    *(float *) (rvp) = ((float (*)(int, int)) (fn))(arg1, arg2)

# define invokeIIIrF(ctx, fn, rvp, arg1, arg2, arg3) \
    *(float *) (rvp) = ((float (*)(int, int, int)) (fn))(arg1, arg2, arg3)
#else

# define invokeVrF ffi_call0
# define invokeIrF ffi_call1
# define invokeIIrF ffi_call2
# define invokeIIIrF ffi_call3

#endif

#if defined(FLOAT_BYPASS_FFI) && defined(__i386__)
// Doing the test before the call produces slightly better i386 asm for float calls
# define CALL(ctx, stmt) do { \
    if (likely(!ctx->saveErrno)) { \
        stmt; \
    } else { \
        stmt; \
        jffi_save_errno(); \
    } \
   } while (0)
#else
// This version produces smaller code for ffi_call paths
# define CALL(ctx, stmt) do { stmt; SAVE_ERRNO(ctx); } while(0)
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

    CALL(ctx, invokeVrI(ctx, ctx->function, &retval));

    return (jint) retval;
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
    invokeVrI(ctx, ctx->function, &retval);

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIrI(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeIrI(ctx, ctx->function, &retval, arg1));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoIrI(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIrI(ctx, ctx->function, &retval, arg1);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeIIrI(ctx, ctx->function, &retval, arg1, arg2));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIIrI(ctx, ctx->function, &retval, arg1, arg2);

    return (int) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    CALL(ctx, invokeIIIrI(ctx, ctx->function, &retval, arg1, arg2, arg3));

    return (jint) retval;
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_invokeNoErrnoIIIrI(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    ffi_sarg retval;

    invokeIIIrI(ctx, ctx->function, &retval, arg1, arg2, arg3);

    return (int) retval;
}

JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeVrF(JNIEnv* env, jclass self, jlong ctxAddress)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    float retval;

    CALL(ctx, invokeVrF(ctx, ctx->function, &retval));

    return retval;
}

JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeIrF(JNIEnv* env, jclass self, jlong ctxAddress,
        jint arg1)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    float retval;

    CALL(ctx, invokeIrF(ctx, ctx->function, &retval, arg1));

    return retval;
}

JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeIIrF(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    float retval;

    CALL(ctx, invokeIIrF(ctx, ctx->function, &retval, arg1, arg2));

    return retval;
}



JNIEXPORT jfloat JNICALL
Java_com_kenai_jffi_Foreign_invokeIIIrF(JNIEnv*env, jobject self, jlong ctxAddress,
        jint arg1, jint arg2, jint arg3)
{
    Function* ctx = (Function *) j2p(ctxAddress);
    float retval;

    CALL(ctx, invokeIIIrF(ctx, ctx->function, &retval, arg1, arg2, arg3));

    return retval;
}
