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

typedef struct JClosurePool {
    ClosurePool* pool;
    CallContext* callContext;
    jmethodID methodID;
    JavaVM* jvm;
} JClosurePool;

typedef struct JClosure {
    void* code; /* the code address must be the first member of this struct; used by java */
    jobject javaObject;
    Closure* closure;
    JClosurePool* pool;
} JClosure;

static void closure_invoke(ffi_cif* cif, void* retval, void** parameters, void* user_data);
static bool closure_prep(void* ctx, void* code, Closure* closure, char* errbuf, size_t errbufsize);

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

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    allocateClosure
 * Signature: (JLjava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_allocateClosure(JNIEnv* env, jobject self, jlong closurePool, jobject proxy)
{
    JClosurePool* pool = (JClosurePool *) j2p(closurePool);
    JClosure* jclosure = NULL;
    jobject obj = NULL;
    Closure* closure = NULL;

    jclosure = malloc(sizeof(*jclosure));
    if (jclosure == NULL) {
        throwException(env, OutOfMemory, "failed to allocate closure");
        goto cleanup;
    }

    obj = (*env)->NewGlobalRef(env, proxy);
    if (obj == NULL) {
        throwException(env, IllegalArgument, "could not obtain reference to java object");
        goto cleanup;
    }

    closure = jffi_Closure_Alloc(pool->pool);
    if (closure == NULL) {
        throwException(env, OutOfMemory, "jffi_Closure_Alloc failed");
        goto cleanup;
    }

    jclosure->closure = closure;
    jclosure->javaObject = obj;
    jclosure->closure->info = jclosure;
    jclosure->pool = pool;
    jclosure->code = jclosure->closure->code;

    return p2j(jclosure);

cleanup:
    if (obj != NULL) {
        (*env)->DeleteGlobalRef(env, obj);
    }
    if (closure != NULL) {
        jffi_Closure_Free(closure);
    }
    if (jclosure != NULL) {
        free(jclosure);
    }

    return 0L;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    releaseClosure
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_releaseClosure(JNIEnv* env, jobject self, jlong address)
{
    JClosure* jclosure = j2p(address);
    
    if (jclosure == NULL) {
        throwException(env, NullPointer, "closure == null");
        return;
    }
    
    if (jclosure->closure != NULL) {
        jffi_Closure_Free(jclosure->closure);
    }
    (*env)->DeleteGlobalRef(env, jclosure->javaObject);
    
    free(jclosure);
}

static void
closure_begin(JClosure* closure, JNIEnv** penv, bool* detach)
{
    JavaVM* jvm = closure->pool->jvm;
    *detach = (*jvm)->GetEnv(jvm, (void **)penv, JNI_VERSION_1_4) != JNI_OK
        && (*jvm)->AttachCurrentThreadAsDaemon(jvm, (void **)penv, NULL) == JNI_OK;
    if ((**penv)->ExceptionCheck(*penv)) {
        (**penv)->ExceptionClear(*penv);
    }
}

static void
closure_end(JClosure* closure, JNIEnv* env, bool detach)
{
    JavaVM* jvm = closure->pool->jvm;
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
    Closure* closure = (Closure *) user_data;
    JClosure* jclosure = (JClosure *) closure->info;

    JNIEnv* env;
    jvalue javaParams[3];
    bool detach;

    closure_begin(jclosure, &env, &detach);

    javaParams[0].j = p2j(retval);
    javaParams[1].j = p2j(parameters);

    //
    // Do the actual invoke - the java code will unmarshal the arguments
    //
    (*env)->CallVoidMethodA(env, jclosure->javaObject, jclosure->pool->methodID, &javaParams[0]);

    closure_end(jclosure, env, detach);
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
            snprintf(errbuf, errbufsize, "Invalid ABI specified");
            //throwException(env, IllegalArgument, "Invalid ABI specified");
            return false;

        case FFI_BAD_TYPEDEF:
            snprintf(errbuf, errbufsize, "Invalid argument type specified");
            //throwException(env, IllegalArgument, "Invalid argument type specified");
            return false;

        default:
            snprintf(errbuf, errbufsize, "Unknown FFI error");
            //throwException(env, IllegalArgument, "Unknown FFI error");
            return false;
    }
}