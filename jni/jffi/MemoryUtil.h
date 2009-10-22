#ifndef JFFI_MEMORY_H
#define JFFI_MEMORY_H

#ifdef __cplusplus
extern "C" {
#endif

int jffi_getPageSize(void);
void* jffi_allocatePages(int npages);
bool jffi_freePages(void *addr, int npages);
bool jffi_makePagesExecutable(void* memory, int npages);

#ifdef __cplusplus
}
#endif

#endif

