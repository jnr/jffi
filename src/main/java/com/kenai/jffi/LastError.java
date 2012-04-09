/*
 * Copyright (C) 2008, 2009 Wayne Meissner
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
 * Provides access to the value of errno on unix, or GetLastError on windows.
 */
public final class LastError {
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final Foreign foreign = Foreign.getInstance();

    /** Lazy-initialization singleton holder */
    private static final class SingletonHolder {
        static final LastError INSTANCE = new LastError();
    }

    /** Creates a new <tt>LastError</tt> instance */
    private LastError() {}

    /**
     * Gets the singleton instance of the <tt>LastError</tt> object.
     *
     * @return An instance of <tt>LastError</tt>
     */
    public static final LastError getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Gets the errno set by the last C function invoked by the current thread.
     *
     * @return The value of errno/GetLastError()
     */
    @Deprecated
    public final int getError() {
        return Foreign.getLastError();
    }

    /**
     * Gets the errno set by the last C function invoked by the current thread.
     *
     * @return The value of errno/GetLastError()
     */
    public final int get() {
        return Foreign.getLastError();
    }

    /**
     * Sets the system errno value.
     *
     * @param value The value to set errno to.
     */
    public final void set(int value) {
        Foreign.setLastError(value);
    }
}
