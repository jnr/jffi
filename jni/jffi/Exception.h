/* 
 * Copyright (C) 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef jffi_Exception_h
#define jffi_Exception_h

#ifdef __cplusplus
extern "C" {
#endif

#define throwException(env, name, fmt, a...) \
    jffi_throwExceptionByName((env), jffi_##name##Exception, fmt, ##a)

extern const char* jffi_IllegalArgumentException;
extern const char* jffi_NullPointerException;
extern const char* jffi_OutOfBoundsException;
extern const char* jffi_OutOfMemoryException;
extern const char* jffi_RuntimeException;
extern const char* jffi_UnsatisfiedLinkException;


extern void jffi_throwExceptionByName(JNIEnv* env, const char* exceptionName, const char* fmt, ...);

#ifdef __cplusplus
}
#endif

#endif /* jffi_Exception_h */

