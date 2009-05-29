#ifndef JFFI_LASTERRROR_H
#define JFFI_LASTERRROR_H

#include "jffi.h"

#define get_last_error() (thread_data_get()->error)
#define set_last_error(err) (thread_data_get()->error = (err))

#endif /* JFFI_LASTERRROR_H */

