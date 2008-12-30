#include <stdlib.h>
#include <pthread.h>
#include <jni.h>
#include "jffi.h"

pthread_key_t jffi_threadDataKey;

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

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved)
{
    pthread_key_create(&jffi_threadDataKey, thread_data_free);
    return JNI_VERSION_1_4;
}

