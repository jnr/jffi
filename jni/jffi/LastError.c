#include <stdlib.h>
#include <errno.h>
#ifdef _WIN32
#  include <windows.h>
#endif
#include <jni.h>
#include "LastError.h"

#if defined(_WIN32)
static __thread int last_error = 0;
#endif

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getLastError
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_getLastError(JNIEnv* env, jobject self)
{
#ifdef _WIN32
    // printf("Getting ERRNO: %d on thread %d\n", last_error, (int)GetCurrentThreadId());
    return last_error;
#else
    return thread_data_get()->error;
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    setLastError
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_setLastError(JNIEnv* env, jobject self, jint value)
{
#ifdef _WIN32
    // printf("Setting ERRNO: %d on thread %d\n", value, (int)GetCurrentThreadId());
    SetLastError(value);
    last_error = value;
#else
    thread_data_get()->error = errno = value;
#endif
}

void
jffi_save_errno(void)
{
#ifdef _WIN32
    last_error = GetLastError();
    // printf("JFFI Saving ERRNO: %d on thread %d\n", last_error, (int)GetCurrentThreadId());
#else
    thread_data_get()->error = errno;
#endif
}
