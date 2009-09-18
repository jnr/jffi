/* 
 * Copyright (C) 2007, 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef jffi_jffi_h
#define jffi_jffi_h

#include <sys/param.h>
#include <sys/types.h>
#include <stdint.h>
#include <string.h>
#ifndef _WIN32
#  include <pthread.h>
#endif
#include "endian.h"
#include <jni.h>
#include <ffi.h>

#ifndef roundup
#  define roundup(x, y)   ((((x)+((y)-1))/(y))*(y))
#endif
#ifdef __GNUC__
#  define likely(x) __builtin_expect((x), 1)
#  define unlikely(x) __builtin_expect((x), 0)
#else 
#  define likely(x) (x)
#  define unlikely(x) (x)
#endif

typedef struct StackAllocator {
    size_t used;
    char data[256];
} StackAllocator;

#define initStackAllocator(alloc) ((alloc)->used = 0)

static inline void*
allocStack(StackAllocator* alloc, size_t size) 
{
    if (likely((alloc->used + size + 7) < sizeof(alloc->data))) {
        uintptr_t data = roundup((uintptr_t) &alloc->data[alloc->used], 8);
        alloc->used = (data + size) - (uintptr_t) &alloc->data[0];
        return (void *) data;
    }
    return NULL;
}

/**
 * Convert a C pointer into a java long
 */
static inline jlong 
p2j(void *p) 
{ 
    return (jlong)(uintptr_t) p; 
}

/**
 * Convert a java long into a C pointer
 */
static inline void* 
j2p(jlong j) 
{
    return (void *)(uintptr_t) j;
}

static inline
jboolean loadClass(JNIEnv* env, const char *name, jclass *classp)
{
    jclass tmp = (*env)->FindClass(env, name);
    if (tmp == NULL) {
        return JNI_FALSE;
    }
    *classp = (jclass)(*env)->NewGlobalRef(env, tmp);
    return JNI_TRUE;
}

typedef union FFIValue {
    
    int8_t s8;
    uint8_t u8;
    int16_t s16;
    uint16_t u16;
    int32_t s32;
    uint32_t u32;
    int64_t s64;
    uint64_t u64;
    jint i;
    jlong j;
    long l;
    float f;
    double d;
    void* p;
    ffi_sarg sarg;
    ffi_arg arg;
} FFIValue;

#ifndef _WIN32
typedef struct ThreadData {
    int error;
} ThreadData;

extern pthread_key_t jffi_threadDataKey;
extern ThreadData* jffi_thread_data_init();

static inline ThreadData*
thread_data_get()
{
    ThreadData* td = pthread_getspecific(jffi_threadDataKey);
    return likely(td != NULL) ? td : jffi_thread_data_init();
}
#endif /* !_WIN32 */

#if defined(__i386__)
#  define USE_RAW 1
#endif

#if BYTE_ORDER == LITTLE_ENDIAN
# define return_int(retval) return ((retval).i)
#else
# define return_int(retval) return ((retval).l & 0xFFFFFFFFL)
#endif

# define ffi_call0(ctx, fn, retval) do { \
        FFIValue arg0; \
        void* ffiValues[] = { &arg0 }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

# define ffi_call1(ctx, fn, retval, arg1) do { \
        void* ffiValues[] = {  ARGPTR(&(arg1), (ctx)->cif.arg_types[0]) }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

# define ffi_call2(ctx, fn, retval, arg1, arg2) do {\
        void* ffiValues[] = { \
            ARGPTR(&arg1, (ctx)->cif.arg_types[0]), \
            ARGPTR(&arg2, (ctx)->cif.arg_types[1]) \
        }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

# define ffi_call3(ctx, fn, retval, arg1, arg2, arg3) do { \
        void* ffiValues[] = { \
            ARGPTR(&arg1, (ctx)->cif.arg_types[0]), \
            ARGPTR(&arg2, (ctx)->cif.arg_types[1]), \
            ARGPTR(&arg3, (ctx)->cif.arg_types[2]) \
        }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

# define ffi_call4(ctx, fn, retval, arg1, arg2, arg3, arg4) do { \
        void* ffiValues[] = { \
            ARGPTR(&arg1, (ctx)->cif.arg_types[0]), \
            ARGPTR(&arg2, (ctx)->cif.arg_types[1]), \
            ARGPTR(&arg3, (ctx)->cif.arg_types[2]) \
            ARGPTR(&arg4, (ctx)->cif.arg_types[3]) \
        }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

# define ffi_call5(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5) do { \
        void* ffiValues[] = { \
            ARGPTR(&arg1, (ctx)->cif.arg_types[0]), \
            ARGPTR(&arg2, (ctx)->cif.arg_types[1]), \
            ARGPTR(&arg3, (ctx)->cif.arg_types[2]) \
            ARGPTR(&arg4, (ctx)->cif.arg_types[3]) \
            ARGPTR(&arg5, (ctx)->cif.arg_types[4]) \
        }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

# define ffi_call6(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5, arg6) do { \
        void* ffiValues[] = { \
            ARGPTR(&arg1, (ctx)->cif.arg_types[0]), \
            ARGPTR(&arg2, (ctx)->cif.arg_types[1]), \
            ARGPTR(&arg3, (ctx)->cif.arg_types[2]) \
            ARGPTR(&arg4, (ctx)->cif.arg_types[3]) \
            ARGPTR(&arg5, (ctx)->cif.arg_types[4]) \
            ARGPTR(&arg6, (ctx)->cif.arg_types[5]) \
        }; \
        ffi_call(&(ctx)->cif, FFI_FN((fn)), (retval), ffiValues); \
    } while (0)

#endif /* jffi_jffi_h */


