/*
 * Copyright (C) 2007-2009 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 
 * Alternatively, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <sys/param.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <errno.h>
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
#include "MemoryUtil.h"
#include "FaultProtect.h"
#include "com_kenai_jffi_Foreign.h"

#define THREAD_ATTACH_THRESHOLD (1000)

struct Closure;

typedef struct ClosureMagazine {
    CallContext* callContext;
    jmethodID methodID;
    JavaVM* jvm;
    void* code;
    struct Closure* closures;
    int nclosures;
    int nextclosure;
    int callWithPrimitiveParameters;
} Magazine;

typedef struct Closure {
    void* code; /* the code address must be the first member of this struct; used by java */
    jobject javaObject;
    Magazine* magazine;
} Closure;

static bool closure_prep(ffi_cif* cif, void* code, Closure* closure, char* errbuf, size_t errbufsize);

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_newClosureMagazine(JNIEnv *env, jobject self, jlong ctxAddress, jobject closureMethod,
    jboolean callWithPrimitiveParameters)
{
    CallContext* ctx = (CallContext *) j2p(ctxAddress);
    Closure* list = NULL;
    Magazine* magazine = NULL;
    caddr_t code = NULL;
    char errmsg[256];
    int i;
    int trampolineSize, pageSize, nclosures;

    trampolineSize = roundup(sizeof(ffi_closure), 8);
    pageSize = jffi_getPageSize();
    nclosures = pageSize / trampolineSize;

    magazine = calloc(1, sizeof(*magazine));
    list = calloc(nclosures, sizeof(*list));
    code = jffi_allocatePages(1);

    if (magazine == NULL || list == NULL || code == NULL) {
        snprintf(errmsg, sizeof(errmsg), "failed to allocate a page. errno=%d (%s)", errno, strerror(errno));
        goto error;
    }
    
    // Thread all the closure handles onto a list, and init each one
    for (i = 0; i < nclosures; ++i) {
        Closure* closure = &list[i];
        closure->magazine = magazine;
        closure->code = (code + (i * trampolineSize));

        if (!closure_prep(&ctx->cif, closure->code, closure, errmsg, sizeof(errmsg))) {
            goto error;
        }
    }
    

    if (!jffi_makePagesExecutable(code, 1)) {
        snprintf(errmsg, sizeof(errmsg), "failed to make page executable. errno=%d (%s)", errno, strerror(errno));
        goto error;
    }

    magazine->methodID = (*env)->FromReflectedMethod(env, closureMethod);
    if (magazine->methodID == NULL) {
        throwException(env, IllegalArgument, "could not obtain reference to closure method");
        goto error;
    }

    /* Track the allocated page + Closure memory area */
    magazine->closures = list;
    magazine->nextclosure = 0;
    magazine->nclosures = nclosures;
    magazine->code = code;
    magazine->callWithPrimitiveParameters = callWithPrimitiveParameters;
    (*env)->GetJavaVM(env, &magazine->jvm);

    return p2j(magazine);

error:
    free(list);
    free(magazine);
    if (code != NULL) {
        jffi_freePages(code, 1);
    }
    throwException(env, Runtime, errmsg);
    return 0L;
}


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeClosureMagazine
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_freeClosureMagazine(JNIEnv *env, jobject self, jlong magAddress)
{
    Magazine* magazine = (Magazine *) j2p(magAddress);
    Closure* closure;
    int i;

    for (i = 0; i < magazine->nextclosure; ++i) {
        (*env)->DeleteGlobalRef(env, magazine->closures[i].javaObject);
    }

    free(magazine->closures);
    jffi_freePages(magazine->code, 1);
    free(magazine);
}


/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    closureMagazineGet
 * Signature: (JLjava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_closureMagazineGet(JNIEnv *env, jobject self, jlong magAddress, jobject closureProxy)
{
    Magazine* magazine = (Magazine *) j2p(magAddress);
    if (magazine->nextclosure < magazine->nclosures) {
        Closure* closure = &magazine->closures[magazine->nextclosure];
        closure->javaObject = (*env)->NewGlobalRef(env, closureProxy);
        if (closure->javaObject == NULL) {
            throwException(env, IllegalArgument, "could not obtain reference to java object");
            return 0L;
        }
        
        magazine->nextclosure++;
        return p2j(closure);
    }

    return 0L;
}



static void
closure_begin(Closure* closure, JNIEnv** penv, bool* detach)
{
    JavaVM* jvm = closure->magazine->jvm;

    *detach = (*jvm)->GetEnv(jvm, (void **)penv, JNI_VERSION_1_4) != JNI_OK
        && (*jvm)->AttachCurrentThreadAsDaemon(jvm, (void **)penv, NULL) == JNI_OK;

#ifndef _WIN32
    if (*detach && thread_data_get()->attach_count++ >= THREAD_ATTACH_THRESHOLD) {
        thread_data_get()->attached_vm = jvm;
        *detach = false;
    }
#endif
    
    if ((**penv)->ExceptionCheck(*penv)) {
        (**penv)->ExceptionClear(*penv);
    }
}

static void
closure_end(Closure* closure, JNIEnv* env, bool detach)
{
    JavaVM* jvm = closure->magazine->jvm;
    bool clearException = detach;

#ifndef _WIN32
    if (thread_data_get()->attached_vm != NULL) {
        clearException = true;
    }
#endif

    if (env != NULL && clearException) {
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionClear(env);
        }
    }

    if (detach) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}

static void
closure_invoke(ffi_cif* cif, void* retval, void** parameters, void* user_data)
{
    Closure* closure = (Closure *) user_data;
    
    JNIEnv* env;
    int i;
    bool detach;
#if FAULT_PROTECT_ENABLED
    ThreadData* td = thread_data_get();
    FaultData* fdp;
#endif

    closure_begin(closure, &env, &detach);
#if FAULT_PROTECT_ENABLED
    fdp = td->fault_data;
    td->fault_data = NULL;
#endif

    if (closure->magazine->callWithPrimitiveParameters) {
        // allocate one more than the parameter count (for the struct return value)
        jvalue* jparams = alloca((cif->nargs + 1) * sizeof(jvalue));

        for (i = 0; i < (int) cif->nargs; i++) {
            jvalue* vp = &jparams[i];

            vp->j = 0LL; // zero out any bits not filled below
            switch (cif->arg_types[i]->type) {
                case FFI_TYPE_SINT8:
                case FFI_TYPE_UINT8:
                    vp->b = *(jbyte *) parameters[i];
                    break;

                case FFI_TYPE_SINT16:
                case FFI_TYPE_UINT16:
                    vp->s = *(jshort *) parameters[i];
                    break;

                case FFI_TYPE_SINT32:
                case FFI_TYPE_UINT32:
                case FFI_TYPE_INT:
                    vp->i = *(jint *) parameters[i];
                    break;

                case FFI_TYPE_SINT64:
                case FFI_TYPE_UINT64:
                    vp->j = *(jlong *) parameters[i];
                    break;

                case FFI_TYPE_FLOAT:
                    vp->i = *(jfloat *) parameters[i];
                    break;

                case FFI_TYPE_DOUBLE:
                    vp->i = *(jdouble *) parameters[i];
                    break;

                case FFI_TYPE_POINTER:
                    if (cif->arg_types[i]->size == 4) {
                        vp->i = (uintptr_t) *(void **) parameters[i];
                    } else {
                        vp->j = p2j(*(void **) parameters[i]);
                    }
                    break;

                case FFI_TYPE_STRUCT:
#if FFI_TYPE_LONGDOUBLE != FFI_TYPE_DOUBLE
                case FFI_TYPE_LONGDOUBLE:
#endif
                    vp->j = p2j(parameters[i]);
                    break;

                default:
                    memset(vp, 0, sizeof(*vp));
                    break;

            }
        }

        switch (cif->rtype->type) {
            case FFI_TYPE_VOID:
                (*env)->CallVoidMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                *((ffi_sarg *) retval) = 0;
                break;

            case FFI_TYPE_SINT8:
                *((ffi_sarg *) retval) = (*env)->CallByteMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_SINT16:
                *((ffi_sarg *) retval) = (*env)->CallShortMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_SINT32:
            case FFI_TYPE_INT:
                *((ffi_sarg *) retval) = (*env)->CallIntMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_UINT8:
                *((ffi_arg *) retval) = (*env)->CallByteMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_UINT16:
                *((ffi_arg *) retval) = (*env)->CallShortMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_UINT32:
                *((ffi_arg *) retval) = (*env)->CallIntMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_SINT64:
                *((int64_t *) retval) = (*env)->CallLongMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_UINT64:
                *((uint64_t *) retval) = (*env)->CallLongMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_POINTER:
                if (cif->rtype->size == 4) {
                    *((ffi_arg *) retval) = (*env)->CallIntMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                } else {
                    *((ffi_arg *) retval) = (*env)->CallLongMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                }
                break;

            case FFI_TYPE_FLOAT:
                *((float *) retval) = (*env)->CallFloatMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_DOUBLE:
                *((double *) retval) = (*env)->CallDoubleMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            case FFI_TYPE_STRUCT:
#if FFI_TYPE_LONGDOUBLE != FFI_TYPE_DOUBLE
                case FFI_TYPE_LONGDOUBLE:
#endif
                // stuff the retval in as the last parameter passed to the java method
                jparams[cif->nargs].j = p2j(retval);
                (*env)->CallVoidMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
                break;

            default:
                memset(retval, 0, cif->rtype->size);
        }

    } else {
        jvalue jparams[2];
        jparams[0].j = p2j(retval);
        jparams[1].j = p2j(parameters);
        //
        // Do the actual invoke - the java code will unmarshal the arguments
        //
        (*env)->CallVoidMethodA(env, closure->javaObject, closure->magazine->methodID, jparams);
    }


    if ((*env)->ExceptionCheck(env)) {
        memset(retval, 0, cif->rtype->size);
    }

#if FAULT_PROTECT_ENABLED
    td->fault_data = fdp;
#endif
    closure_end(closure, env, detach);
}

static bool
closure_prep(ffi_cif* cif, void* code, Closure* closure, char* errbuf, size_t errbufsize)
{
    ffi_status status;

    status = ffi_prep_closure(code, cif, closure_invoke, closure);
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

