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
abstract public class DirectObjectParameterStrategy extends ObjectParameterStrategy {

    public DirectObjectParameterStrategy(boolean isDirect, ObjectParameterType parameterType) {
        super(isDirect, parameterType);
    }
    
    abstract public long getAddress(Object parameter);

    public final Object object(Object parameter) {
        throw new RuntimeException("direct object " + (parameter != null ? parameter.getClass() : "null") + " has no array");
    }
    
    public final int offset(Object parameter) {
        throw new RuntimeException("direct object " + (parameter != null ? parameter.getClass() : "null") + "has no offset");
    }
    
    public final int length(Object parameter) {
        throw new RuntimeException("direct object " + (parameter != null ? parameter.getClass() : "null") + "has no length");
    }
}
