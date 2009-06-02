#ifndef FUNCTION_H
#define FUNCTION_H

#include <stdbool.h>

typedef struct Function {
    void* function;
    ffi_cif cif;
    int rawParameterSize;
    ffi_type** ffiParamTypes;
    int* rawParamOffsets;
    bool saveErrno;
} Function;

#define SAVE_ERRNO(ctx) do { \
    if (unlikely(ctx->saveErrno)) { \
        set_last_error(errno); \
    } \
} while(0)

#endif /* FUNCTION_H */

