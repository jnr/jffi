/*
 * Copyright (C) 2012 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Alternatively, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef jffi_FaultData_h
#define jffi_FaultData_h

#include <signal.h>
#include <jni.h>
#include <stdint.h>

typedef struct FaultData_ FaultData;

#if defined(__APPLE__) && (defined(__x86_64__) || defined(__i386__))
# define FAULT_PROTECT_ENABLED (1)
#endif

extern struct sigaction jffi_sigsegv_chain;
extern struct sigaction jffi_sigbus_chain;

extern void jffi_sigsegv(int sig, siginfo_t *si, void *uctx);
extern void jffi_sigbus(int sig, siginfo_t *si, void *uctx);

#if FAULT_PROTECT_ENABLED

#include <setjmp.h>
#include <sys/ucontext.h>

struct FaultData_ {
    jmp_buf buf;
    int sig;
    _STRUCT_MCONTEXT mcontext;
    struct {
        uintptr_t addr;
        uintptr_t procname;
        uintptr_t libname;
    } frame[128];
    int frame_count;
    char backtrace_buf[1024];
};

extern int jffi_setjmp(struct FaultData_ *);
extern void jffi_longjmp(jmp_buf env, int val);
extern void jffi_faultException(JNIEnv* env, struct FaultData_ *, int val);



#if defined(__amd64) || defined(__x86_64__)
# if defined (__APPLE__)
#  define JB_SP           1
#  define JB_RP           0
#  define UNW_REG_EH UNW_X86_64_RAX
# endif
#else
#  define JB_SP           1
#  define JB_RP           0
#  define UNW_REG_EH UNW_X86_EAX
#endif

#else /* fault protection not enabled */
struct FaultData_ { long dummy; };
# define jffi_setjmp(f) (0)
#endif

#define FAULTPROT_CTX(env, ctx, stmt, fail) do { \
    if (likely((ctx->flags & (CALL_CTX_SAVE_ERRNO | CALL_CTX_FAULT_PROT)) == 0)) { \
        stmt; \
    } else if (likely((ctx->flags & CALL_CTX_FAULT_PROT) == 0)) { \
        stmt; \
        jffi_save_errno_ctx(ctx); \
    } else { \
        JNIEnv* volatile env_ = env; \
        FaultData fd; \
        int val; \
        \
        if (unlikely((val = jffi_setjmp(&fd)) != 0)) { \
            jffi_faultException(env_, &fd, val); \
            fail; \
        } else { \
            ThreadData* td = thread_data_get(); \
            td->fault_data = &fd; \
            stmt; \
            td->fault_data = NULL; \
            jffi_save_errno_td(td, ctx); \
        } \
    } \
} while(0)

#endif /* jffi_FaultData_h */
