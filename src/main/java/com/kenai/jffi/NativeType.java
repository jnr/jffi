/*
 * Copyright (C) 2008-2010 Wayne Meissner
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

package com.kenai.jffi;

/**
 *
 */
public enum NativeType {

    VOID(Foreign.TYPE_VOID),
    FLOAT(Foreign.TYPE_FLOAT),
    DOUBLE(Foreign.TYPE_DOUBLE),
    LONGDOUBLE(Foreign.TYPE_LONGDOUBLE),
    UINT8(Foreign.TYPE_UINT8),
    SINT8(Foreign.TYPE_SINT8),
    UINT16(Foreign.TYPE_UINT16),
    SINT16(Foreign.TYPE_SINT16),
    UINT32(Foreign.TYPE_UINT32),
    SINT32(Foreign.TYPE_SINT32),
    UINT64(Foreign.TYPE_UINT64),
    SINT64(Foreign.TYPE_SINT64),
    POINTER(Foreign.TYPE_POINTER),
    UCHAR(Foreign.TYPE_UCHAR),
    SCHAR(Foreign.TYPE_SCHAR),
    USHORT(Foreign.TYPE_USHORT),
    SSHORT(Foreign.TYPE_SSHORT),
    UINT(Foreign.TYPE_UINT),
    SINT(Foreign.TYPE_SINT),
    ULONG(Foreign.TYPE_ULONG),
    SLONG(Foreign.TYPE_SLONG),
    STRUCT(Foreign.TYPE_STRUCT);

    final int ffiType;

    NativeType(int ffiType) {
        this.ffiType = ffiType;
    }
}
