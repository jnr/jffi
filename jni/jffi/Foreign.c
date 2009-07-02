#include <stdlib.h>
#ifndef _WIN32
#  include <pthread.h>
#endif
#include <jni.h>
#include "Exception.h"
#include "com_kenai_jffi_Foreign.h"
#include "jffi.h"

#ifndef _WIN32
pthread_key_t jffi_threadDataKey;
static void thread_data_free(void *ptr);
#endif

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved)
{
#ifndef _WIN32
    pthread_key_create(&jffi_threadDataKey, thread_data_free);
#endif
    return JNI_VERSION_1_4;
}

#ifndef _WIN32
ThreadData*
jffi_thread_data_init()
{
    ThreadData* td = calloc(1, sizeof(*td));
    pthread_setspecific(jffi_threadDataKey, td);
    return td;
}

static void
thread_data_free(void *ptr)
{
    free(ptr);
}
#endif /* !_WIN32 */

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_getVersion(JNIEnv* env, jobject self)
{
    return (com_kenai_jffi_Foreign_VERSION_MAJOR << 16)
        | (com_kenai_jffi_Foreign_VERSION_MINOR << 8)
        | (com_kenai_jffi_Foreign_VERSION_MICRO);
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_getJNIVersion(JNIEnv* env, jobject self)
{
    return (*env)->GetVersion(env);
}

JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_getJavaVM(JNIEnv *env, jobject self)
{
    JavaVM* vm;
    (*env)->GetJavaVM(env, &vm);
    return p2j(vm);
}

JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_fatalError(JNIEnv * env, jobject self, jstring msg)
{
    const char* str = (*env)->GetStringUTFChars(env, msg, NULL);
    (*env)->FatalError(env, str);
    (*env)->ReleaseStringUTFChars(env, msg, str);
}

JNIEXPORT jclass JNICALL
Java_com_kenai_jffi_Foreign_defineClass__Ljava_lang_String_2Ljava_lang_Object_2_3BII(JNIEnv *env,
        jobject self, jstring jname, jobject loader, jbyteArray jbuf, jint off, jint len)
{
    const char* name = NULL;
    jbyte* buf = NULL;
    jclass retval = NULL;

    name = (*env)->GetStringUTFChars(env, jname, NULL);
    if (name == NULL) {
        throwException(env, NullPointer, "Invalid name parameter");
        goto cleanup;
    }
    buf = (*env)->GetByteArrayElements(env, jbuf, NULL);
    if (buf == NULL) {
        throwException(env, NullPointer, "Invalid buffer parameter");
        goto cleanup;
    }

    retval = (*env)->DefineClass(env, name, loader, buf + off, len);

cleanup:
    if (buf != NULL) {
        (*env)->ReleaseByteArrayElements(env, jbuf, buf, JNI_ABORT);
    }
    if (name != NULL) {
        (*env)->ReleaseStringUTFChars(env, jname, name);
    }

    return retval;
}

JNIEXPORT jclass JNICALL
Java_com_kenai_jffi_Foreign_defineClass__Ljava_lang_String_2Ljava_lang_Object_2Ljava_nio_ByteBuffer_2(JNIEnv *env,
        jobject self, jstring jname, jobject loader, jobject jbuf)
{
    const char* name = NULL;
    jclass retval = NULL;

    name = (*env)->GetStringUTFChars(env, jname, NULL);
    if (name == NULL) {
        throwException(env, NullPointer, "Invalid name parameter");
        goto cleanup;
    }

    if (jbuf == NULL) {
        throwException(env, NullPointer, "Invalid buffer parameter");
        goto cleanup;
    }

    retval = (*env)->DefineClass(env, name, loader,
            (*env)->GetDirectBufferAddress(env, jbuf),
            (*env)->GetDirectBufferCapacity(env, jbuf));

cleanup:
    if (name != NULL) {
        (*env)->ReleaseStringUTFChars(env, jname, name);
    }

    return retval;
}

JNIEXPORT jobject JNICALL
Java_com_kenai_jffi_Foreign_allocObject(JNIEnv *env, jobject self, jclass klass)
{
    return (*env)->AllocObject(env, klass);
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_registerNatives(JNIEnv *env, jobject self, jclass clazz,
        jlong methods, jint nmethods)
{
    return (*env)->RegisterNatives(env, clazz, j2p(methods), nmethods);
}

JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_unregisterNatives(JNIEnv *env, jobject self, jclass clazz)
{
    return (*env)->UnregisterNatives(env, clazz);
}
