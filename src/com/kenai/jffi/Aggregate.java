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

public abstract class Aggregate extends Type {
    private volatile boolean disposed;

    public Aggregate(long handle) {
        super(handle);
    }

    public synchronized final void dispose() {
        if (disposed) {
            throw new RuntimeException("native handle already freed");
        }

        disposed = true;
        Foreign.getInstance().freeStruct(handle);        
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!disposed) {
                Foreign.getInstance().freeStruct(handle);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            super.finalize();
        }
    }
}
