#include <sys/param.h>
#include <sys/types.h>

#include <stdlib.h>
#ifdef __sun
#  include <alloca.h>
#endif
#include <ffi.h>
#include <jni.h>
#include "com_kenai_jffi_Foreign.h"
#include "jffi.h"
#include "Exception.h"

#ifndef MAX
#  define MAX(x,y)  ((x) > (y) ? (x) : (y))
#endif
#define ALIGN(v, a)  (((((size_t) (v))-1) | ((a)-1))+1)

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    newStruct
 * Signature: ([J)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_newStruct(JNIEnv* env, jobject self, jlongArray typeArray)
{
    ffi_type* s = NULL;
    int fieldCount;
    jlong* fieldTypes;
    int i;

    if (typeArray == NULL) {
        throwException(env, NullPointer, "types array cannot be null");
        return 0L;
    }

    fieldCount = (*env)->GetArrayLength(env, typeArray);
    if (fieldCount < 1) {
        throwException(env, IllegalArgument, "No fields specified");
        return 0L;
    }

    s = calloc(1, sizeof(*s));
    if (s == NULL) {
        return 0L;
    }

    //
    // Need to terminate the list of field types with a NULL, so allocate 1 extra
    //
    s->elements = calloc(fieldCount + 1, sizeof(ffi_type *));
    if (s->elements == NULL) {
        goto error;
    }

    // Copy out all the field descriptors
    fieldTypes = alloca(fieldCount * sizeof(long));
    (*env)->GetLongArrayRegion(env, typeArray, 0, fieldCount, fieldTypes);
    
    s->type = FFI_TYPE_STRUCT;
    s->size = 0;
    s->alignment = 0;

    for (i = 0; i < fieldCount; ++i) {
        ffi_type* elem = (ffi_type *) j2p(fieldTypes[i]);

        if (elem == NULL) {
            throwException(env, IllegalArgument, "Type for field %d is NULL", i);
            goto error;
        }
        if (elem->size == 0) {
            throwException(env, IllegalArgument, "Type for field %d has size 0", i);
            goto error;
        }

        s->elements[i] = elem;
        s->size = ALIGN(s->size, elem->alignment);
        s->size += elem->size;
        s->alignment = MAX(s->alignment, elem->alignment);
        //printf("s->size=%d s->alignment=%d\n", s->size, s->alignment);
    }
    if (s->size == 0) {
        throwException(env, Runtime, "Struct size is zero");
        goto error;
    }
    
    // Include tail padding
    s->size = ALIGN(s->size, s->alignment);
    return p2j(s);
    
error:
    if (s != NULL) {
        if (s->elements != NULL) {
            free(s->elements);
        }
        free(s);
    }
    return 0L;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    freeStruct
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_kenai_jffi_Foreign_freeStruct(JNIEnv* env, jobject self, jlong handle)
{
    ffi_type* s = (ffi_type *) j2p(handle);
    
    if (s != NULL) {
        if (s->elements != NULL) {
            free(s->elements);
        }
        free(s);
    }
}
