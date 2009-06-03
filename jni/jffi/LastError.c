#include <stdlib.h>
#include <errno.h>

#include <jni.h>
#include "LastError.h"

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getLastError
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_com_kenai_jffi_Foreign_getLastError(JNIEnv* env, jobject self)
{
    return thread_data_get()->error;
}

void
jffi_save_errno(void)
{
    thread_data_get()->error = errno;
}

