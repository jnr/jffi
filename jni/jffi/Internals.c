#include <stdlib.h>
#include <jni.h>
#include "Exception.h"
#include "com_kenai_jffi_Foreign.h"
#include "LastError.h"
#include "jffi.h"

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    getSaveErrnoFunction
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_getSaveErrnoFunction(JNIEnv *env, jobject self)
{
    return p2j(jffi_save_errno);
}