/* libunwind - a platform-independent unwind library
   Copyright (C) 2003-2004 Hewlett-Packard Co
	Contributed by David Mosberger-Tang <davidm@hpl.hp.com>

This file is part of libunwind.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.  */

#undef _FORTIFY_SOURCE
#include <assert.h>
#include <setjmp.h>
#include <signal.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include "jffi.h"
#include "FaultProtect.h"

#if FAULT_PROTECT_ENABLED
#include <libunwind.h>

#ifdef __APPLE__
#if defined(__x86_64__)
# define SP_OFF 16
#else
# define SP_OFF 12
#endif
#elif defined __FreeBSD__
# define SP_OFF (sizeof(unw_word_t))
#else
# define SP_OFF 0
#endif

void
jffi_longjmp (jmp_buf env, int val)
{
    extern int _jffi_longjmp_cont;
    unw_context_t uc;
    unw_cursor_t c;
    unw_word_t sp, ip, bp = 0;
    uintptr_t *wp = (uintptr_t *) env;
    int i, setjmp_frame;

    if (unw_getcontext (&uc) < 0 || unw_init_local (&c, &uc) < 0) {
        debug("failed to get context");
        abort ();
    }
#ifdef __x86_86__
# define UNW_REG_BP UNW_X86_64_RBP
#else
# define UNW_REG_BP UNW_X86_EBP
#endif
    setjmp_frame = 0;

    do {
        char name[256];
        unw_proc_info_t pi;
        unw_word_t off;
        if (unw_get_reg (&c, UNW_REG_BP, &bp) < 0) {
            abort();
        }
        if (unw_get_reg (&c, UNW_REG_SP, &sp) < 0) {
            abort();
        }
        if (unw_get_reg (&c, UNW_REG_IP, &ip) < 0) {
            abort();
        }
        unw_get_proc_name(&c, name, sizeof(name), &off);
        unw_get_proc_info(&c, &pi);
//        debug("frame %s ip=%llx sp=%llx bp=%llx wp[RP]=%p wp[SP]=%p, pi.start_ip=%llx, pi.end_ip=%llx",
//            name, (long long) ip, (long long) sp, (long long) bp, (void *) wp[JB_RP], (void *) wp[JB_SP],
//            pi.start_ip, pi.end_ip);

        if (wp[JB_SP] > sp || wp[JB_RP] < pi.start_ip || wp[JB_RP] > pi.end_ip) continue;

        /* found the right frame: */
//        debug("found frame to jump back to");
        assert (UNW_NUM_EH_REGS >= 2);
        if (unw_set_reg (&c, UNW_REG_EH + 0, wp[JB_RP]) < 0
            || unw_set_reg (&c, UNW_REG_EH + 1, val) < 0
            || unw_set_reg (&c, UNW_REG_IP, (unw_word_t) (uintptr_t) &_jffi_longjmp_cont))
            abort ();

        unw_resume (&c);
        // should not reach here
        abort ();
    } while (unw_step (&c) > 0);
//    debug("failed to find correct frame to jmp to");
}

#endif /* FAULT_PROTECT_ENABLED */
