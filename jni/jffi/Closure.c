/*
 * Copyright (C) 2007, 2008 Wayne Meissner
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

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#ifdef __sun
#  include <alloca.h>
#endif
#include <jni.h>
#include "jffi.h"
#include "Exception.h"
#include "Type.h"
#include "com_kenai_jffi_Foreign.h"

typedef struct Closure {
    void* code;
    ffi_closure* ffi_closure;
    ffi_cif ffi_cif;
    jobject javaObject;
    jmethodID javaMethod;
    JavaVM* jvm;
    ffi_type** ffiParamTypes;
    int flags;
} Closure;


static void closure_invoke(ffi_cif* cif, void* retval, void** parameters, void* user_data);

/*
 * Class:     com_googlecode_jffi_ClosureManager
 * Method:    allocateClosure
 * Signature: (Lcom/googlecode/jffi/ClosureManager$NativeClosure;Ljava/lang/reflect/Method;[II)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_newClosure(JNIEnv* env, jclass clazz,
        jobject closureObject, jobject closureMethod, jlong returnType, jlongArray paramTypeArray, jint flags)
{
    Closure* closure = NULL;
    int argCount;
    jlong* paramTypes;
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
    closure->ffiParamTypes = calloc(argCount, sizeof(ffi_type *));
    if (closure->ffiParamTypes == NULL) {
        throwException(env, OutOfMemory, "Could not allocate space for parameter types");
        goto cleanup;
    }
    paramTypes = alloca(argCount * sizeof(jlong));
    (*env)->GetLongArrayRegion(env, paramTypeArray, 0, argCount, paramTypes);
    for (i = 0; i < argCount; ++i) {
        closure->ffiParamTypes[i] = (ffi_type *) j2p(paramTypes[i]);
        if (closure->ffiParamTypes[i] == NULL) {
            throwException(env, NullPointer, "parameter type %d is null", i);
            goto cleanup;
        }
    }
    ffiReturnType = (ffi_type *) j2p(returnType);
    if (ffiReturnType == NULL) {
        throwException(env, NullPointer, "return type is null");
        goto cleanup;
    }
#ifdef _WIN32
    abi = (flags & STDCALL) ? FFI_STDCALL : FFI_DEFAULT_ABI;
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
    ffi_prep_closure_loc(closure->ffi_closure, &closure->ffi_cif,
            closure_invoke, closure, closure->code);

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
    }
    return 0;
}

/*
 * Class:     com_googlecode_jffi_ClosureManager
 * Method:    freeClosure
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_googlecode_jffi_lowlevel_Foreign_freeClosure(JNIEnv* env, jclass clazz, jlong address)
{
    Closure* closure = j2p(address);

    (*env)->DeleteGlobalRef(env, closure->javaObject);
    ffi_closure_free(closure->ffi_closure);
    free(closure);
}

static void
closure_begin(Closure* closure, JNIEnv** penv, bool* detach)
{
    JavaVM* jvm = closure->jvm;
    *detach = (*jvm)->GetEnv(jvm, (void **)penv, JNI_VERSION_1_4) != JNI_OK
        && (*jvm)->AttachCurrentThreadAsDaemon(jvm, (void **)penv, NULL) == JNI_OK;
    if ((**penv)->ExceptionCheck(*penv)) {
        (**penv)->ExceptionClear(*penv);
    }
}
static void
closure_end(Closure* closure, JNIEnv* env, bool detach)
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
    Closure* closure = (Closure *) user_data;
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

