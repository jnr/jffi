#include "FaultProtect.h"

#if FAULT_PROTECT_ENABLED
#include <setjmp.h>
#include <libunwind.h>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <dlfcn.h>

#include <sys/ucontext.h>

#include "jffi.h"
#include "Exception.h"

#ifdef __APPLE__
# define SKIP_FRAME_COUNT (2)
#endif


struct sigaction jffi_sigsegv_chain;
struct sigaction jffi_sigbus_chain;

void
fill_in_backtrace(FaultData* fdp)
{
    unw_context_t uc;
    unw_cursor_t c;
    int i, boff;

    memset(&uc, 0, sizeof(uc));
    memset(&c, 0, sizeof(c));
    if (unw_getcontext(&uc) < 0) {
        abort();
    }

    if (unw_init_local(&c, &uc) < 0) {
        abort();
    }

    // Skip the signal handler, and the signal trampoline
    for (i = 0; i < SKIP_FRAME_COUNT; i++) {
        if (unw_step(&c) <= 0) {
            break;
        }
    }

    memset(fdp->frame, 0, sizeof(fdp->frame));
    fdp->frame_count = 0;
    boff = 0;

    do {
        char fn[256];
        unw_word_t off, ip;
        Dl_info dli;
        unw_proc_info_t pi;

        unw_get_reg (&c, UNW_REG_IP, &ip);
        fdp->frame[fdp->frame_count].addr = (uintptr_t) ip;
        fdp->frame[fdp->frame_count].procname = (uintptr_t) &fdp->backtrace_buf[boff];
        unw_get_proc_name(&c, (char *) fdp->frame[fdp->frame_count].procname, sizeof(fdp->backtrace_buf) - boff, &off);
        unw_get_proc_info(&c, &pi);
        boff += strlen((char *) fdp->frame[fdp->frame_count].procname) + 1;
        fdp->frame[fdp->frame_count].libname = (uintptr_t) &fdp->backtrace_buf[boff];
        dladdr((void *)(uintptr_t) ip, &dli);
        strcpy((char *) (uintptr_t) fdp->frame[fdp->frame_count].libname, dli.dli_fname);
        boff += strlen((char *) fdp->frame[fdp->frame_count].libname) + 1;
        fdp->frame_count++;

    } while (unw_step(&c) > 0);
}

static void
jffi_fault(void)
{
    ThreadData* td = (ThreadData *) pthread_getspecific(jffi_threadDataKey);
    FaultData* fdp = td->fault_data;
    td->fault_data = NULL;

    fill_in_backtrace(fdp);
    jffi_longjmp(fdp->buf, fdp->sig);

    // If we get here, we could not unwind the stack - restore the old signal handler, and let it re-fault
    switch (fdp->sig) {
        case SIGBUS:
            sigaction(fdp->sig, &jffi_sigbus_chain, NULL);
            break;
        case SIGSEGV:
            sigaction(fdp->sig, &jffi_sigsegv_chain, NULL);
            break;
    }
}

static void
jffi_fault_handler(ThreadData* td, int sig, siginfo_t* si, ucontext_t* uctx)
{
    extern int _jffi_fault_trampoline;
    FaultData* fdp = td->fault_data;
    int i, boff;

    fdp->mcontext = *uctx->uc_mcontext;
    fdp->sig = sig;

#ifdef __x86_64__
    uctx->uc_mcontext->__ss.__rax = (uintptr_t) &jffi_fault;
    uctx->uc_mcontext->__ss.__rdx = uctx->uc_mcontext->__ss.__rip;
    uctx->uc_mcontext->__ss.__rip = (uintptr_t) &_jffi_fault_trampoline;
    uctx->uc_mcontext->__ss.__rflags = 0;

#elif defined(__i386__)
    uctx->uc_mcontext->ss.eax = (uintptr_t) &jffi_fault;
    uctx->uc_mcontext->ss.edx = uctx->uc_mcontext->ss.eip;
    uctx->uc_mcontext->ss.eip = &_jffi_fault_trampoline;
    uctx->uc_mcontext->ss.eflags = 0;

#else
# error "architecture not supported"
#endif
}

void
jffi_sigsegv(int sig, siginfo_t *si, void *uctx)
{
    ThreadData* td = (ThreadData *) pthread_getspecific(jffi_threadDataKey);
    if (td == NULL || td->fault_data == NULL) {
        (*jffi_sigsegv_chain.sa_sigaction)(sig, si, uctx);
    } else {
        jffi_fault_handler(td, sig, si, (ucontext_t *) uctx);
    }
}

void
jffi_sigbus(int sig, siginfo_t *si, void *uctx)
{
    ThreadData* td = (ThreadData *) pthread_getspecific(jffi_threadDataKey);
    if (td == NULL || td->fault_data == NULL) {
        (*jffi_sigbus_chain.sa_sigaction)(sig, si, uctx);
    } else {
        jffi_fault_handler(td, sig, si, (ucontext_t *) uctx);
    }
}


void
jffi_faultException(JNIEnv* env, struct FaultData_* f, int val)
{
    jclass exceptionClass = (*env)->FindClass(env, "com/kenai/jffi/FaultException");
    if (exceptionClass != NULL) {
        jmethodID constructor = (*env)->GetMethodID(env, exceptionClass, "<init>", "(I[J[J[J)V");
        if (constructor != NULL) {
            jlongArray addresses = (*env)->NewLongArray(env, f->frame_count);
            jlongArray procnames = (*env)->NewLongArray(env, f->frame_count);
            jlongArray libnames = (*env)->NewLongArray(env, f->frame_count);
            int i;

            for (i = 0; i < f->frame_count; i++) {
                jlong ip = f->frame[i].addr;
                jlong procname = f->frame[i].procname;
                jlong libname = f->frame[i].libname;
                (*env)->SetLongArrayRegion(env, addresses, i, 1, &ip);
                (*env)->SetLongArrayRegion(env, procnames, i, 1, &procname);
                (*env)->SetLongArrayRegion(env, libnames, i, 1, &libname);
            }

            jobject exc = (*env)->NewObject(env, exceptionClass, constructor, val, addresses, procnames, libnames);
            if (exc != NULL) (*env)->Throw(env, (jthrowable) exc);
        }
        (*env)->DeleteLocalRef(env, exceptionClass);
    } else {
        throwException(env, NullPointer, "fault");
    }
}

#endif
