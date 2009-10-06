#ifndef JFFI_CALLCONTEXT_H
#define JFFI_CALLCONTEXT_H

#include <stdbool.h>

typedef struct CallContext {
    /** IMPORTANT: keep ffi_cif as the first field */
    ffi_cif cif;
    int rawParameterSize;
    ffi_type** ffiParamTypes;
    int* rawParamOffsets;
    bool saveErrno;
} CallContext;

#endif /* JFFI_CALLCONTEXT_H */

