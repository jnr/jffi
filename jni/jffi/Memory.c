#include <stdlib.h>
#include <jni.h>
#include "jffi.h"

#include "com_kenai_jffi_Foreign.h"

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    allocateMemory
 * Signature: (JZ)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_allocateMemory(JNIEnv* env, jobject self, jlong size, jboolean clear)
{
    void* memory = malloc(size);
    if (memory != NULL && clear != JNI_FALSE) {
        memset(memory, 0, size);
    }
    return p2j(memory);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeMemory
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_freeMemory(JNIEnv* env, jobject self, jlong address)
{
    free(j2p(address));
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    strlen
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_strlen(JNIEnv* env, jobject self, jlong address)
{
    return (jlong) strlen(j2p(address));
}


