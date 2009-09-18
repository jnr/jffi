#ifndef JFFI_CLOSUREPOOL_H
#define JFFI_CLOSUREPOOL_H

typedef struct ClosurePool_ ClosurePool;
typedef struct Closure_ Closure;

struct Closure_ {
    void* info;      /* opaque handle for storing closure-instance specific data */
    void* function;  /* closure-instance specific function, called by custom trampoline */
    void* code;      /* The native trampoline code location */
    struct ClosurePool_* pool;
    Closure* next;
};

void jffi_ClosurePool_Init(void);

ClosurePool* jffi_ClosurePool_New(int closureSize,
        bool (*prep)(void* ctx, void *code, Closure* closure, char* errbuf, size_t errbufsize),
        void* ctx);

void jffi_ClosurePool_Free(ClosurePool *);

Closure* jffi_Closure_Alloc(ClosurePool *);
void jffi_Closure_Free(Closure *);

void* jffi_Closure_CodeAddress(Closure *);

#endif /* JFFI_CLOSUREPOOL_H */