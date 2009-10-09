// AsmJit - Complete JIT Assembler for C++ Language.

// Copyright (c) 2008-2009, Petr Kobalicek <kobalicek.petr@gmail.com>
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
// 
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.

// [Guard]
#ifndef _ASMJIT_MEMORYMANAGER_H
#define _ASMJIT_MEMORYMANAGER_H

// [Dependencies]
#include "Build.h"

// [Warnings-Push]
#include "WarningsPush.h"

namespace AsmJit {

//! @addtogroup AsmJit_MemoryManagement
//! @{

// ============================================================================
// [AsmJit::MemoryManager]
// ============================================================================

//! @brief Types of allocation used by @c AsmJit::MemoryManager::alloc() method.
enum MEMORY_ALLOC_TYPE
{
  //! @brief Allocate pernament memory that will be never freed.
  MEMORY_ALLOC_FREEABLE,
  //! @brief Allocate memory that can be freed by @c AsmJit::MemoryManager::free()
  //! method.
  MEMORY_ALLOC_PERNAMENT
};

//! @brief Virtual memory manager.
struct ASMJIT_API MemoryManager
{
  //! @brief Create memory manager instance.
  MemoryManager() ASMJIT_NOTHROW;
  //! @brief Destroy memory manager instance, this means also to free all blocks.
  ~MemoryManager() ASMJIT_NOTHROW;

  //! @brief Allocate a @a size bytes of virtual memory.
  void* alloc(SysUInt size, UInt32 type = MEMORY_ALLOC_FREEABLE) ASMJIT_NOTHROW;
  //! @brief Free previously allocated memory at a given @a address.
  bool free(void* address) ASMJIT_NOTHROW;

  //! @brief Tell how many bytes are currently used.
  SysUInt used() ASMJIT_NOTHROW;
  //! @brief Tell how many bytes are currently allocated.
  SysUInt allocated() ASMJIT_NOTHROW;

  //! @brief Get global instance of memory manager.
  static MemoryManager* global() ASMJIT_NOTHROW;

private:
  void* _d;
};

//! @}

} // AsmJit namespace

// [Warnings-Pop]
#include "WarningsPop.h"

// [Guard]
#endif // _ASMJIT_MEMORYMANAGER_H
