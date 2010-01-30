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
    /** The FFI type of this type */
    private final int type;
    /** The size in bytes of this type */
    private final int size;

    /** The minimum alignment of this type */
    private final int align;

    /** The address of this type's ffi_type structure */
    private final long handle;

    private volatile boolean disposed = false;

    public Aggregate(long handle) {
        if (handle == 0L) {
            throw new NullPointerException("Invalid ffi_type handle");
        }
        this.handle = handle;
        this.type = Foreign.getInstance().getTypeType(handle);
        this.size = Foreign.getInstance().getTypeSize(handle);
        this.align = Foreign.getInstance().getTypeAlign(handle);
    }
    
    final long handle() {
        return handle;
    }

    public final int type() {
        return type;
    }

    public final int size() {
        return size;
    }

    public final int alignment() {
        return align;
    }

    public synchronized final void dispose() {
        if (disposed) {
            throw new RuntimeException("native handle already freed");
        }

        disposed = true;
        Foreign.getInstance().freeAggregate(handle);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!disposed) {
                dispose();
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            super.finalize();
        }
    }
}
