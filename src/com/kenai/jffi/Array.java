/*
 * Copyright (C) 2009 Wayne Meissner
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
     * @param fields The fields contained in the struct.
     */
    public Array(Type elementType, int length) {
        super(Foreign.getInstance().newArray(elementType.handle(), length));
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
}
