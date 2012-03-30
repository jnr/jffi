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

import java.util.Arrays;

/**
 * Describes the layout of a C union
 */
public final class Union extends Aggregate {
    /* Keep a strong reference to the field types so they do not GCed */
    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private final Type[] fields;


    public static Union newUnion(Type... fields) {
        return new Union(fields);
    }

    /**
     * Creates a new C union layout description.
     *
     * @param fields The fields contained in the union.
     */
    public Union(Type... fields) {
        super(Foreign.getInstance(), Foreign.getInstance().newStruct(Type.nativeHandles(fields), true));
        this.fields = fields.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Union union = (Union) o;

        return Arrays.equals(fields, union.fields);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (fields != null ? Arrays.hashCode(fields) : 0);
        return result;
    }
}
