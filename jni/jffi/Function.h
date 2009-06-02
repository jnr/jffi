#ifndef FUNCTION_H
#define FUNCTION_H

#include <stdbool.h>

typedef struct Function {
    ffi_cif cif;
    void* function;
    int rawParameterSize;
    ffi_type** ffiParamTypes;
    int* rawParamOffsets;
    bool saveErrno;
} Function;

#define SAVE_ERRNO(ctx) do { \
    if (unlikely(ctx->saveErrno)) { \
        jffi_save_errno(); \
    } \
} while(0)

#endif /* FUNCTION_H */

