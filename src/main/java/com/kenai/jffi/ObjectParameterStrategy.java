/*
 * Copyright (C) 2011 Wayne Meissner
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
abstract public class ObjectParameterStrategy {
    private final boolean isDirect;
    final int typeInfo;
    protected static enum StrategyType { DIRECT, HEAP }
    protected static final StrategyType DIRECT = StrategyType.DIRECT;
    protected static final StrategyType HEAP = StrategyType.HEAP;

    public ObjectParameterStrategy(boolean isDirect) {
        this(isDirect, ObjectParameterType.INVALID);
    }

    public ObjectParameterStrategy(boolean isDirect, ObjectParameterType type) {
        this.isDirect = isDirect;
        this.typeInfo = type.typeInfo;
    }

    public ObjectParameterStrategy(StrategyType type) {
        this(type, ObjectParameterType.INVALID);
    }

    public ObjectParameterStrategy(StrategyType strategyType, ObjectParameterType parameterType) {
        this.isDirect = strategyType == DIRECT;
        this.typeInfo = parameterType.typeInfo;
    }

    public final boolean isDirect() {
        return isDirect;
    }

    final int objectInfo(ObjectParameterInfo info) {
        int objectInfo = info.asObjectInfo();
        // Over-ride the type info contained in the parameter info
        if (typeInfo != 0) {
            return (objectInfo & ~ObjectBuffer.TYPE_MASK) | typeInfo;
        } else {
            return objectInfo;
        }
    }

    abstract public long address(Object parameter);

    abstract public Object object(Object parameter);
    abstract public int offset(Object parameter);
    abstract public int length(Object parameter);
}
