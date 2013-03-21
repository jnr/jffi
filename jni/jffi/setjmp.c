#include "FaultProtect.h"

#if FAULT_PROTECT_ENABLED
#include <setjmp.h>
#include <libunwind.h>
#include <stdio.h>
#include <signal.h>
#include <unistd.h>

#include "jffi.h"
#include "Exception.h"

int
jffi_setjmp(FaultData* f)
{
    void **wp = (void **) f->buf;
    wp[JB_SP] = __builtin_frame_address (0);
    wp[JB_RP] = (void *) __builtin_return_address (0);

//    debug("saved state: sp=%p, rp=%p", wp[JB_SP], wp[JB_RP]);
    return 0;
}

#endif
