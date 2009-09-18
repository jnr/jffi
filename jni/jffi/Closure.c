/*
 * Copyright (C) 2007-2009 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <sys/param.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#if defined(__sun) || defined(_AIX)
#  include <sys/sysmacros.h>
#  include <alloca.h>
#endif
#ifdef _WIN32
#  include <malloc.h>
#endif
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "Type.h"
#include "CallContext.h"
#include "ClosurePool.h"
#include "com_kenai_jffi_Foreign.h"

typedef struct JClosure {
    void* code;
    ffi_closure* ffi_closure;
    ffi_cif ffi_cif;
    jobject javaObject;
    jmethodID javaMethod;
    JavaVM* jvm;
    ffi_type** ffiParamTypes;
    int flags;
} JClosure;

typedef struct JClosurePool {
    ClosurePool* pool;
    CallContext* callContext;
    jmethodID methodID;
    JavaVM* jvm;
} JClosurePool;

#ifndef MAX
#  define MAX(a,b) ((a) > (b) ? (a) : (b))
#endif

static void closure_invoke(ffi_cif* cif, void* retval, void** parameters, void* user_data);
static bool closure_prep(void* ctx, void* code, Closure* closure, char* errbuf, size_t errbufsize);

/*
 * Class:     com_googlecode_jffi_ClosureManager
 * Method:    allocateClosure
 * Signature: (Lcom/googlecode/jffi/ClosureManager$NativeClosure;Ljava/lang/reflect/Method;[II)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_newClosure(JNIEnv* env, jclass clazz,
        jobject closureObject, jobject closureMethod, jlong returnType, jlongArray paramTypeArray, jint flags)
{
    JClosure* closure = NULL;
    int argCount;
    ffi_type* ffiReturnType;
    ffi_status status;
    ffi_abi abi;
    int i;

    argCount = (*env)->GetArrayLength(env, paramTypeArray);

    closure = calloc(1, sizeof(*closure));
    if (closure == NULL) {
        goto cleanup;
    }

    closure->ffi_closure = ffi_closure_alloc(sizeof(*closure->ffi_closure), &closure->code);
    if (closure->ffi_closure == NULL) {
        throwException(env, OutOfMemory, "Could not allocate space for closure");
        goto cleanup;
    }

    closure->javaObject = (*env)->NewGlobalRef(env, closureObject);
    if (closure->javaObject == NULL) {
        throwException(env, IllegalArgument, "Could not obtain reference to Closure");
        goto cleanup;
    }

    closure->javaMethod = (*env)->FromReflectedMethod(env, closureMethod);
    if (closure->javaMethod == NULL) {
        throwException(env, IllegalArgument, "Could not obtain reference to Closure method");
        goto cleanup;
    }

    closure->ffiParamTypes = calloc(MAX(1, argCount), sizeof(ffi_type *));
    if (closure->ffiParamTypes == NULL) {
        throwException(env, OutOfMemory, "Could not allocate space for parameter types");
        goto cleanup;
    }

    if (argCount > 0) {
        jlong* paramTypes = alloca(argCount * sizeof (jlong));
        (*env)->GetLongArrayRegion(env, paramTypeArray, 0, argCount, paramTypes);
        for (i = 0; i < argCount; ++i) {
            closure->ffiParamTypes[i] = (ffi_type *) j2p(paramTypes[i]);
            if (closure->ffiParamTypes[i] == NULL) {
                throwException(env, NullPointer, "parameter type %d is null", i);
                goto cleanup;
            }
        }
    }

    ffiReturnType = (ffi_type *) j2p(returnType);
    if (ffiReturnType == NULL) {
        throwException(env, NullPointer, "return type is null");
        goto cleanup;
    }

#ifdef _WIN32
    abi = (flags & com_kenai_jffi_Foreign_F_STDCALL) ? FFI_STDCALL : FFI_DEFAULT_ABI;
#else
    abi = FFI_DEFAULT_ABI;
#endif

    status = ffi_prep_cif(&closure->ffi_cif, abi, argCount, ffiReturnType, closure->ffiParamTypes);
    switch (status) {
        case FFI_BAD_ABI:
            throwException(env, IllegalArgument, "Invalid ABI specified");
            goto cleanup;
        case FFI_BAD_TYPEDEF:
            throwException(env, IllegalArgument, "Invalid argument type specified");
            goto cleanup;
        case FFI_OK:
            break;
        default:
            throwException(env, IllegalArgument, "Unknown FFI error");
            goto cleanup;
    }

    status = ffi_prep_closure_loc(closure->ffi_closure, &closure->ffi_cif,
            closure_invoke, closure, closure->code);
    switch (status) {
        case FFI_BAD_ABI:
            throwException(env, IllegalArgument, "Invalid ABI specified");
            goto cleanup;
        case FFI_BAD_TYPEDEF:
            throwException(env, IllegalArgument, "Invalid argument type specified");
            goto cleanup;
        case FFI_OK:
            break;
        default:
            throwException(env, IllegalArgument, "Unknown FFI error");
            goto cleanup;
    }

    closure->flags = flags;
    (*env)->GetJavaVM(env, &closure->jvm);
    return p2j(closure);

cleanup:
    if (closure != NULL) {
        if (closure->ffiParamTypes != NULL) {
            free(closure->ffiParamTypes);
        }
        if (closure->ffi_closure != NULL) {
            ffi_closure_free(closure->ffi_closure);
        }
        if (closure->javaObject != NULL) {
            (*env)->DeleteGlobalRef(env, closure->javaObject);
        }
        free(closure);
    }
    return 0;
}


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeClosure
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_kenai_jffi_Foreign_freeClosure(JNIEnv* env, jobject self, jlong address)
{
    JClosure* closure = j2p(address);

    if (closure == NULL) {
        throwException(env, NullPointer, "closure == null");
        return;
    }

    free(closure->ffiParamTypes);
    ffi_closure_free(closure->ffi_closure);
    (*env)->DeleteGlobalRef(env, closure->javaObject);
    //(*env)->DeleteGlobalRef(env, (jobject) closure->javaMethod);
    free(closure);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    newClosurePool
 * Signature: (JLjava/lang/reflect/Method;)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_newClosurePool(JNIEnv* env, jobject self, jlong ctxAddress, jobject closureMethod)
{
    JClosurePool* pool = NULL;

    pool = calloc(1, sizeof(*pool));
    if (pool == NULL) {
        throwException(env, OutOfMemory, "calloc failed");
        return 0L;
    }

    pool->pool = jffi_ClosurePool_New(sizeof(ffi_closure), closure_prep, pool);
    if (pool->pool == NULL) {
        throwException(env, OutOfMemory, "could not allocate closure pool");
        goto error;
    }

    pool->methodID = (*env)->FromReflectedMethod(env, closureMethod);
    if (pool->methodID == NULL) {
        throwException(env, IllegalArgument, "could not obtain reference to closure method");
        goto error;
    }

    if (ctxAddress == 0L) {
        throwException(env, NullPointer, "NULL CallContext");
        goto error;
    }

    (*env)->GetJavaVM(env, &pool->jvm);
    pool->callContext = j2p(ctxAddress);

    return p2j(pool);

error:
    if (pool != NULL) {
        if (pool->pool != NULL) {
            jffi_ClosurePool_Free(pool->pool);
        }
        free(pool);
    }
    return 0L;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeClosurePool
 * Signature: (J)J
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_freeClosurePool(JNIEnv* env, jobject self, jlong address)
{
    JClosurePool* pool = (JClosurePool *) j2p(address);
    if (pool != NULL) {
        jffi_ClosurePool_Free(pool->pool);
        free(pool);
    }
}


static void
closure_begin(JClosure* closure, JNIEnv** penv, bool* detach)
{
    JavaVM* jvm = closure->jvm;
    *detach = (*jvm)->GetEnv(jvm, (void **)penv, JNI_VERSION_1_4) != JNI_OK
        && (*jvm)->AttachCurrentThreadAsDaemon(jvm, (void **)penv, NULL) == JNI_OK;
    if ((**penv)->ExceptionCheck(*penv)) {
        (**penv)->ExceptionClear(*penv);
    }
}

static void
closure_end(JClosure* closure, JNIEnv* env, bool detach)
{
    JavaVM* jvm = closure->jvm;
    if (detach && env != NULL) {
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionClear(env);
        }
        (*jvm)->DetachCurrentThread(jvm);
    }
}

static void
closure_invoke(ffi_cif* cif, void* retval, void** parameters, void* user_data)
{
    JClosure* closure = (JClosure *) user_data;
    JNIEnv* env;
    jvalue javaParams[3];
    bool detach;

    closure_begin(closure, &env, &detach);

    javaParams[0].j = p2j(retval);
    javaParams[1].j = p2j(parameters);

    //
    // Do the actual invoke - the java code will unmarshal the arguments
    //
    (*env)->CallVoidMethodA(env, closure->javaObject, closure->javaMethod, &javaParams[0]);

    closure_end(closure, env, detach);
}

static bool
closure_prep(void* ctx, void* code, Closure* closure, char* errbuf, size_t errbufsize)
{
    JClosurePool* pool = ctx;
    ffi_status status;

    status = ffi_prep_closure(code, &pool->callContext->cif, closure_invoke, closure);
    switch (status) {
        case FFI_OK:
            return true;

        case FFI_BAD_ABI:
            //throwException(env, IllegalArgument, "Invalid ABI specified");
            return false;

        case FFI_BAD_TYPEDEF:
            //throwException(env, IllegalArgument, "Invalid argument type specified");
            return false;

        default:
            //throwException(env, IllegalArgument, "Unknown FFI error");
            return false;
    }
}