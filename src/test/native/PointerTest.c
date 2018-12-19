/*
 * Copyright (c) 2007 Wayne Meissner. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the project nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
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

#include <sys/types.h>
#include <sys/param.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
typedef void* ptr;
typedef void* pointer;
#ifdef _WIN32
typedef char* caddr_t;
#endif

#define RET(T) T ptr_ret_##T(void* arg1, int offset) { \
    T tmp; memcpy(&tmp, (caddr_t) arg1 + offset, sizeof(tmp)); return tmp; \
}
#define SET(T) void ptr_set_##T(void* arg1, int offset, T value) { \
    memcpy((caddr_t) arg1 + offset, &value, sizeof(value)); \
}
#define TEST(T) SET(T) RET(T)

TEST(int8_t);
TEST(int16_t);
TEST(int32_t);
TEST(int64_t);
TEST(float);
TEST(double);
TEST(pointer);

void*
ptr_return_array_element(void **ptrArray, int arrayIndex) 
{
    return ptrArray[arrayIndex];
}

void
ptr_set_array_element(void **ptrArray, int arrayIndex, void *value)
{    
    ptrArray[arrayIndex] = value;
}

void*
ptr_malloc(int size) 
{
    return calloc(1, size);
}
void
ptr_free(void* ptr)
{
    free(ptr);
}

void*
ptr_from_address(uintptr_t addr)
{
    return (void *) addr;
}

unsigned long 
invokeO(unsigned long *ptr)
{
    unsigned long ret = *ptr;
    *ptr = 0xdeadbeefUL;
    return ret;
}

unsigned long 
invokeON(unsigned long *ptr, unsigned long val)
{
    unsigned long ret = *ptr;
    *ptr = val;
    return ret;
}

unsigned long 
invokeNO(unsigned long val, unsigned long *ptr)
{
    unsigned long ret = *ptr;
    *ptr = val;
    return ret;
}

unsigned long 
invokeOO(unsigned long *p1, unsigned long *p2)
{
    unsigned long ret = *p1 + *p2;
    unsigned long tmp = *p1;
    *p1 = *p2;
    *p2 = tmp;
    return ret;
}


unsigned long 
invokeONN(unsigned long *ptr, unsigned long n1, unsigned long n2)
{
    unsigned long ret = *ptr;
    ptr[0] = n1;
    ptr[1] = n2;
    return ret;
}

unsigned long
invokeOON(unsigned long *p1, unsigned long *p2, unsigned long n1)
{
    unsigned long ret = *p1;
    *p1 = n1;
    *p2 = n1;
    return ret;
}


unsigned long 
invokeNNO(unsigned long n1, unsigned long n2, unsigned long *ptr)
{
    unsigned long ret = *ptr;
    ptr[0] = n1;
    ptr[1] = n2;
    return ret;
}

unsigned long 
invokeNON(unsigned long n1, unsigned long *ptr, unsigned long n2)
{
    unsigned long ret = *ptr;
    ptr[0] = n1;
    ptr[1] = n2;
    return ret;
}

unsigned long 
invokeNOO(unsigned long n1, unsigned long *p1, unsigned long *p2)
{
    unsigned long ret = *p1;
    *p1 = n1;
    *p2 = n1;
    return ret;
}


unsigned long 
invokeOOO(unsigned long *p1, unsigned long *p2, unsigned long *p3)
{
    unsigned long ret = *p1;
    *p1 = *p3;
    *p2 = *p3;
    *p3 = ret;
    return ret;
}
#define N3O1 p1[0] = n1; p1[1] = n2; p1[2] = n3
#define N3O2 p1[0] = n1; p1[1] = n2; p1[2] = n3
