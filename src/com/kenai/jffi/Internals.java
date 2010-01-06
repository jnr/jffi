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
 * Retrieves metadata about jffi C internals
 */
public final class Internals {

    private Internals() {
    }

    public static final long getErrnoSaveFunction() {
        try {
            return com.kenai.jffi.Library.getDefault().getSymbolAddress("jffi_save_errno");

        } catch (Throwable t) {
            return 0;
        }
    }
}
