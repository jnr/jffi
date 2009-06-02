#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <stdlib.h>
#include <jni.h>
#include "jffi.h"
#include "com_kenai_jffi_Foreign.h"

/*
 * This version of getZeroTerminatedByteArray is deprecated and only here for
 * binary backwards compatibility.
 */
JNIEXPORT jbyteArray JNICALL
Java_com_kenai_jffi_Foreign_getZeroTerminatedByteArray__JJ(JNIEnv* env, jobject self, jlong address, jlong maxlen)
{
    return Java_com_kenai_jffi_Foreign_getZeroTerminatedByteArray__JI(env, self, address, (jint) maxlen);
}
