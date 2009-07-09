#include <stdlib.h>
#include <unistd.h>
#ifndef _WIN32
#  include <sys/mman.h>
#else
#  include <windows.h>
#  include <winnt.h>
#  include <winbase.h>
#endif
#include <jni.h>
#include "Exception.h"
#include "LastError.h"
#include "com_kenai_jffi_Foreign.h"
#include "jffi.h"

static int PROT(int p);

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    pageSize
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_pageSize(JNIEnv *env, jobject self)
{
#ifndef _WIN32
    return sysconf(_SC_PAGESIZE);
#else
    SYSTEM_INFO si;
    GetSystemInfo(&si);

    return si.dwPageSize;
#endif
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    vmalloc
 * Signature: (JJII)J
 */
JNIEXPORT jlong JNICALL
Java_com_kenai_jffi_Foreign_vmalloc(JNIEnv *env, jobject self, jlong addr, jlong size,
        jint prot, jint flags)
{
    void* ptr;
#ifndef _WIN32
#define ALLOC_ERROR ((caddr_t) -1)
    
    int f = 0;
    f |= ((flags & com_kenai_jffi_Foreign_MEM_FIXED) != 0) ? MAP_FIXED : 0;
#ifdef MAP_TEXT
    f |= ((flags & com_kenai_jffi_Foreign_MEM_TEXT) != 0) ? MAP_TEXT : 0;
#endif

    ptr = mmap(j2p(addr), (size_t) size, PROT(prot), f | MAP_ANON | MAP_PRIVATE,
            -1, 0);
#else
#define ALLOC_ERROR (NULL)
    ptr = VirtualAlloc(j2p(addr), size, MEM_RESERVE | MEM_COMMIT, PROT(prot));
#endif
    if (ptr == ALLOC_ERROR) {
        jffi_save_errno();
        return -1;
    }
    return p2j(ptr);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    vmfree
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_kenai_jffi_Foreign_vmfree(JNIEnv *env, jobject self, jlong addr, jlong size)
{
    jboolean result;
#ifndef _WIN32
    result = munmap(j2p(addr), (size_t) size) == 0;
#else
    result = VirtualFree(j2p(addr), size, MEM_RELEASE);
#endif
    if (!result) {
        jffi_save_errno();
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    vmprotect
 * Signature: (JJI)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_kenai_jffi_Foreign_vmprotect(JNIEnv *env, jobject self, jlong addr, jlong size, jint prot)
{
    jboolean result;
#ifndef _WIN32
    result = mprotect(j2p(addr), (size_t) size, PROT(prot)) == 0;
#else
    DWORD oldprot;
    result = VirtualProtect(j2p(addr), size, PROT(prot), &oldprot);
#endif
    if (!result) {
        jffi_save_errno();
        return JNI_FALSE;
    }
    return JNI_TRUE;
}


#ifdef _WIN32
static int
PROT(int p)
{
    int n = 0;

    if ((p & com_kenai_jffi_Foreign_PROT_EXEC) != 0) {
        n = ((p & com_kenai_jffi_Foreign_PROT_WRITE) != 0)
                ? PAGE_EXECUTE_READWRITE : PAGE_EXECUTE_READ;
    } else if ((p & com_kenai_jffi_Foreign_PROT_WRITE) != 0) {
        n = PAGE_READWRITE;
    } else if ((p & com_kenai_jffi_Foreign_PROT_READ) != 0) {
        n = PAGE_READONLY;
    } else {
        n = PAGE_NOACCESS;
    }

    return n;
}

#else
static int
PROT(int p)
{
    int n = 0;

    n |= ((p & com_kenai_jffi_Foreign_PROT_NONE) != 0) ? PROT_NONE : 0;
    n |= ((p & com_kenai_jffi_Foreign_PROT_READ) != 0) ? PROT_READ : 0;
    n |= ((p & com_kenai_jffi_Foreign_PROT_WRITE) != 0) ? PROT_WRITE : 0;
    n |= ((p & com_kenai_jffi_Foreign_PROT_EXEC) != 0) ? PROT_EXEC : 0;

    return n;
}

#endif
