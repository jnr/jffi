#include <stdlib.h>
#include <errno.h>
#ifdef _WIN32
#  include <windows.h>
#endif
#include <jni.h>
#include "LastError.h"

#if defined(_WIN32) && defined(notyet)
static int __thread last_error = 0;
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
#ifdef notyet
    return last_error;
#else
    return GetLastError();
#endif
#else
    return thread_data_get()->error;
#endif
}

void
jffi_save_errno(void)
{
#ifdef _WIN32
    //last_error = GetLastError();
#else
    thread_data_get()->error = errno;
#endif
}

