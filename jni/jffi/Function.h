#ifndef FUNCTION_H
#define FUNCTION_H

typedef struct Function {
    void* function;
    ffi_cif cif;
    ffi_type** ffiParamTypes;
    int* rawParamOffsets;
} Function;

#endif /* FUNCTION_H */