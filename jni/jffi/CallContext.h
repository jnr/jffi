#ifndef CALLCONTEXT_H
#define CALLCONTEXT_H

typedef struct CallContext {
    ffi_cif cif;
    ffi_type** ffiParamTypes;
    int* rawParamOffsets;
} CallContext;

#endif /* CALLCONTEXT_H */