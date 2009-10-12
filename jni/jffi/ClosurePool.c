/*
 * Copyright (c) 2009, Wayne Meissner
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * The name of the author or authors may not be used to endorse or promote
 *   products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <sys/param.h>
#include <sys/types.h>
#ifndef _WIN32
#  include <sys/mman.h>
#endif
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <stdbool.h>
#ifndef _WIN32
#  include <unistd.h>
#else
#  include <windows.h>
#endif
#include <errno.h>

#include <ffi.h>

#include "queue.h"
#include "jffi.h"
#include "memory.h"
#include "ClosurePool.h"

typedef struct ClosureSlab_ {
    void* code;
    void* data;
    long refcnt;
    Closure* list;
    ClosurePool* pool;
    TAILQ_ENTRY(ClosureSlab_) entry;
} Slab;

struct ClosurePool_ {
    void* ctx;
    int closureSize;
    int trampolineSize;
    int pageSize;
    int closuresPerPage;
    bool (*prep)(void* ctx, void *code, Closure* closure, char* errbuf, size_t errbufsize);
#if defined (HAVE_NATIVETHREAD) && !defined(_WIN32)
    pthread_mutex_t mutex;
#endif
    TAILQ_HEAD(,ClosureSlab_) partial;
    TAILQ_HEAD(,ClosureSlab_) full;
    TAILQ_HEAD(,ClosureSlab_) empty;
    long refcnt;
};

#if defined(HAVE_NATIVETHREAD) && !defined(_WIN32)
#  define pool_lock(p) pthread_mutex_lock(&(p)->mutex)
#  define pool_unlock(p)  pthread_mutex_unlock(&(p)->mutex)
#else
#  define pool_lock(p)
#  define pool_unlock(p)
#endif

static Slab* new_slab(ClosurePool* pool);

ClosurePool*
jffi_ClosurePool_New(int closureSize,
        bool (*prep)(void* ctx, void *code, Closure* closure, char* errbuf, size_t errbufsize),
        void* ctx)
{
    ClosurePool* pool;

    pool = calloc(1, sizeof(*pool));
    if (pool == NULL) {
        return NULL;
    }

    pool->closureSize = closureSize;
    pool->ctx = ctx;
    pool->prep = prep;
    pool->refcnt = 1;
    pool->trampolineSize = roundup(pool->closureSize, 8);
    pool->closuresPerPage = jffi_getPageSize() / pool->trampolineSize;
    TAILQ_INIT(&pool->partial);
    TAILQ_INIT(&pool->full);
    TAILQ_INIT(&pool->empty);
    
#if defined(HAVE_NATIVETHREAD) && !defined(_WIN32) && !defined(__WIN32__)
    pthread_mutex_init(&pool->mutex, NULL);
#endif

    return pool;
}

void
jffi_ClosurePool_Free(ClosurePool* pool)
{
    if (pool != NULL) {
        bool empty = false;
        pool_lock(pool);
        pool->refcnt = 0;
        empty = TAILQ_EMPTY(&pool->full) && TAILQ_EMPTY(&pool->partial);
        pool_unlock(pool);

        jffi_ClosurePool_Drain(pool);
        if (empty) {
            free(pool);
        }
    }
}

void
jffi_ClosurePool_Drain(ClosurePool* pool)
{
    Slab* slab;

    pool_lock(pool);
    while ((slab = TAILQ_FIRST(&pool->empty))) {
        jffi_freePages(slab->code, 1);
        free(slab->data);
        TAILQ_REMOVE(&pool->empty, slab, entry);
        free(slab);
    }
    pool_unlock(pool);
}

Closure*
jffi_Closure_Alloc(ClosurePool* pool)
{
    Slab* slab = NULL;
    Closure* closure;

    pool_lock(pool);

    if (unlikely(TAILQ_EMPTY(&pool->partial))) {
        if (TAILQ_EMPTY(&pool->empty)) {
            slab = new_slab(pool);
            if (slab == NULL) {
                pool_unlock(pool);
                return NULL;
            }
        } else {
            slab = TAILQ_FIRST(&pool->empty);
            TAILQ_REMOVE(&pool->empty, slab, entry);
        }
        TAILQ_INSERT_TAIL(&pool->partial, slab, entry);
    } else {
        slab = TAILQ_FIRST(&pool->partial);
    }

    closure = slab->list;
    slab->list = closure->next;
    slab->refcnt++;

    // If this slab is completely used up, put it on the full list until a closure is freed
    if (unlikely(slab->list == NULL)) {
        TAILQ_REMOVE(&pool->partial, slab, entry);
        TAILQ_INSERT_TAIL(&pool->full, slab, entry);
    }

    pool_unlock(pool);

    return closure;
}

void
jffi_Closure_Free(Closure* closure)
{
    if (likely(closure != NULL)) {
        bool cleanup = false;
        Slab* slab = closure->slab;
        ClosurePool* pool = slab->pool;
        pool_lock(pool);

        // If this slab was previously fully used, move it to end of the partial list
        if (unlikely(slab->list == NULL)) {
            TAILQ_REMOVE(&pool->full, slab, entry);
            TAILQ_INSERT_TAIL(&pool->partial, slab, entry);
        }

        closure->next = slab->list;
        slab->list = closure;
        slab->refcnt--;
        
        // This slab is now unused, put it on the empty list, ready for draining
        if (unlikely(slab->refcnt < 1)) {
            TAILQ_REMOVE(&pool->partial, slab, entry);
            TAILQ_INSERT_TAIL(&pool->empty, slab, entry);
        }

        cleanup = pool->refcnt < 1 && TAILQ_EMPTY(&pool->full) && TAILQ_EMPTY(&pool->partial);
        pool_unlock(pool);
        if (cleanup) {
            jffi_ClosurePool_Drain(pool);
            free(pool);
        }
    }
}

void*
jffi_Closure_CodeAddress(Closure* handle)
{
    return handle->code;
}


static Slab*
new_slab(ClosurePool* pool)
{
    Closure* list = NULL;
    Slab* slab = NULL;
    caddr_t code = NULL;
    char errmsg[256];
    int i;

    slab = calloc(1, sizeof(*slab));
    list = calloc(pool->closuresPerPage, sizeof(*list));
    code = jffi_allocatePages(1);

    if (slab == NULL || list == NULL || code == NULL) {
        snprintf(errmsg, sizeof(errmsg), "failed to allocate a page. errno=%d (%s)", errno, strerror(errno));
        goto error;
    }

    // Thread all the closure handles onto a list, and init each one
    for (i = 0; i < pool->closuresPerPage; ++i) {
        Closure* closure = &list[i];
        closure->next = &list[i + 1];
        closure->slab = slab;
        closure->code = (code + (i * pool->trampolineSize));

        if (!(*pool->prep)(pool->ctx, closure->code, closure, errmsg, sizeof(errmsg))) {
            goto error;
        }
    }
    list[pool->closuresPerPage - 1].next = NULL;

    if (!jffi_makePagesExecutable(code, 1)) {
        goto error;
    }

    /* Track the allocated page + Closure memory area */
    slab->data = list;
    slab->code = code;
    slab->list = list;
    slab->pool = pool;

    return slab;

error:
    free(list);
    free(slab);
    if (code != NULL) {
        jffi_freePages(code, 1);
    }

    return NULL;
}
