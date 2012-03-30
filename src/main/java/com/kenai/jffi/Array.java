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

package com.kenai.jffi;

/**
 * Describes the layout of a C array
 */
public final class Array extends Aggregate {
    /* Keep a strong reference to the element types so it is not GCed */
    private final Type elementType;

    private final int length;

    /**
     * Creates a new C array layout description.
     *
     * @param elementType The type of each element of the array
     * @param length The length of the array.
     */
    public static Array newArray(Type elementType, int length) {
        return new Array(elementType, length);
    }

    /**
     * Creates a new C array layout description.
     *
     * @param fields The fields contained in the struct.
     */
    public Array(Type elementType, int length) {
        super(Foreign.getInstance(), Foreign.getInstance().newArray(elementType.handle(), length));
        this.elementType = elementType;
        this.length = length;
    }
    
    /**
     * Returns the type of elements in the array
     *
     * @return The <tt>Type</tt> of the elements in the array
     */
    public final Type getElementType() {
        return elementType;
    }

    /**
     * Returns the number of elements in the array
     *
     * @return The number of elements in the array
     */
    public final int length() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Array array = (Array) o;

        if (length != array.length) return false;
        if (elementType != null ? !elementType.equals(array.elementType) : array.elementType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (elementType != null ? elementType.hashCode() : 0);
        result = 31 * result + length;
        return result;
    }
}
