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
 * Flags to use when adding an array as a pointer parameter
 */
public final class ArrayFlags {

    /* Stop ArrayFlags from being instantiated */
    private ArrayFlags() {}

    /** Copy the array contents to native memory before calling the function */
    public static final int IN = ObjectBuffer.IN;

    /** After calling the function, reload the array contents from native memory */
    public static final int OUT = ObjectBuffer.OUT;

    /** Pin the array memory and pass the JVM memory pointer directly to the function */
    public static final int PINNED = ObjectBuffer.PINNED;

    /** Append a NUL byte to the array contents after copying to native memory */
    public static final int NULTERMINATE = ObjectBuffer.ZERO_TERMINATE;

    /** For OUT arrays, clear the native memory area before passing to the native function */
    public static final int CLEAR = ObjectBuffer.CLEAR;

    /**
     * Tests if the flags indicate data should be copied from native memory.
     *
     * @param flags The array flags.  Any combination of IN | OUT | PINNED | NULTERMINATE.
     * @return <tt>true</tt> If array data should be copied from native memory.
     */
    public static final boolean isOut(int flags) {
        return (flags & (OUT | IN)) != IN;
    }

    /**
     * Tests if the flags indicate data should be copied to native memory.
     *
     * @param flags The array flags.  Any combination of IN | OUT | PINNED | NULTERMINATE.
     * @return <tt>true</tt> If array data should be copied to native memory.
     */
    public static final boolean isIn(int flags) {
        return (flags & (OUT | IN)) != OUT;
    }
}
