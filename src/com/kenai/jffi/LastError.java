/*
 * Copyright (C) 2008, 2009 Wayne Meissner
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
 * Provides access to the value of errno on unix, or GetLastError on windows.
 */
public final class LastError {
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
        return foreign.getLastError();
    }

    /**
     * Gets the errno set by the last C function invoked by the current thread.
     *
     * @return The value of errno/GetLastError()
     */
    public final int get() {
        return foreign.getLastError();
    }

    /**
     * Sets the system errno value.
     *
     * @param value The value to set errno to.
     */
    public final void set(int value) {
        foreign.setLastError(value);
    }
}
