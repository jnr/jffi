#ifndef JFFI_LASTERRROR_H
#define JFFI_LASTERRROR_H

#include "jffi.h"

static inline int
get_last_error()
{
    return thread_data_get()->error;
}

static inline void
set_last_error(int error)
{
    thread_data_get()->error = error;
}

#endif /* JFFI_LASTERRROR_H */

