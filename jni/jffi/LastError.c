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
    return get_last_error();
}
