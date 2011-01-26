/* 
 * Copyright (C) 2009 Wayne Meissner
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

#ifndef JFFI_ENDIAN_H
#define JFFI_ENDIAN_H

#include <sys/param.h>
#include <sys/types.h>

#ifdef __linux__
#  include_next <endian.h>
#endif

#ifdef __sun
# include <sys/byteorder.h>
# define LITTLE_ENDIAN 1234
# define BIG_ENDIAN 4321
# if defined(_BIG_ENDIAN)
#  define BYTE_ORDER BIG_ENDIAN
# elif defined(_LITTLE_ENDIAN)
#  define BYTE_ORDER LITTLE_ENDIAN
# else
#  error "Cannot determine endian-ness"
# endif
#endif

#if defined(_AIX) && !defined(BYTE_ORDER)
# define LITTLE_ENDIAN 1234
# define BIG_ENDIAN 4321
# if defined(__BIG_ENDIAN__)
#  define BYTE_ORDER BIG_ENDIAN
# elif defined(__LITTLE_ENDIAN__)
#  define BYTE_ORDER LITTLE_ENDIAN
# else
#  error "Cannot determine endian-ness"
# endif
#endif

#if !defined(BYTE_ORDER) || !defined(LITTLE_ENDIAN) || !defined(BIG_ENDIAN)
#  error "Cannot determine the endian-ness of this platform"
#endif

#endif /* JFFI_ENDIAN_H */
