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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Describes the layout of a C struct
 */
public final class Struct extends Aggregate {
    private static final Map<List<Type>, StructReference> structCache = new ConcurrentHashMap<List<Type>, StructReference>();
    private static final ReferenceQueue<Struct> structReferenceQueue = new ReferenceQueue<Struct>();

    /* Keep a strong reference to the field types so they do not GCed */
    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private final Type[] fields;

    public static Struct newStruct(Type... fields) {
        List<Type> fieldsList = Arrays.asList(fields);
        StructReference ref = structCache.get(fieldsList);
        Struct s = ref != null ? ref.get() : null;
        if (s != null) {
            return s;
        }

        // Cull any dead references
        while ((ref = (StructReference) structReferenceQueue.poll()) != null) {
            structCache.remove(ref.fieldsList);
        }

        structCache.put(fieldsList, new StructReference(s = new Struct(Foreign.getInstance(), fields), structReferenceQueue, fieldsList));

        return s;
    }

    /**
     * Creates a new C struct layout description.
     *
     * @param fields The fields contained in the struct.
     */
    private Struct(Foreign foreign, Type... fields) {
        super(foreign, foreign.newStruct(Type.nativeHandles(fields), false));
        this.fields = fields.clone();
    }

    /**
     * Creates a new C struct layout description.
     *
     * @param fields The fields contained in the struct.
     */
    @Deprecated
    public Struct(Type... fields) {
        super(Foreign.getInstance(), Foreign.getInstance().newStruct(Type.nativeHandles(fields), false));
        this.fields = fields.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        return Arrays.equals(fields, ((Struct) o).fields);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(fields);
        return result;
    }

    private static final class StructReference extends WeakReference<Struct> {
        List<Type> fieldsList;

        private StructReference(Struct struct, ReferenceQueue<? super Struct> referenceQueue, List<Type> fieldsList) {
            super(struct, referenceQueue);
            this.fieldsList = fieldsList;
        }
    }
}
