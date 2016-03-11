/* 
 * Copyright (C) 2008, 2009 Wayne Meissner
 * 
 * This file is part of jffi.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 
 * Alternatively, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    int flags;
    long resultMask;
    int (*error_fn)(void);
} CallContext;

extern void jffi_save_errno_ctx(CallContext* ctx);

#define CALL_CTX_SAVE_ERRNO (0x1)
#define CALL_CTX_FAST_INT   (0x2)
#define CALL_CTX_FAST_LONG  (0x4)
#define CALL_CTX_FAULT_PROT (0x8)

#define SAVE_ERRNO(ctx) do { \
    if (unlikely((ctx->flags & CALL_CTX_SAVE_ERRNO) != 0)) { \
        jffi_save_errno_ctx(ctx); \
    } \
} while(0)


#endif /* JFFI_CALLCONTEXT_H */

