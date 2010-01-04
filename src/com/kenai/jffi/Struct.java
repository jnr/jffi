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
 * Describes the layout of a C struct
 */
public final class Struct extends Aggregate {
    /* Keep a strong reference to the field types so they do not GCed */
    private final Type[] fields;

    /**
     * Creates a new C struct layout description.
     *
     * @param fields The fields contained in the struct.
     */
    public Struct(Type... fields) {
        super(Foreign.getInstance().newStruct(Type.nativeHandles(fields), false));
        this.fields = (Type[]) fields.clone();
    }
}
