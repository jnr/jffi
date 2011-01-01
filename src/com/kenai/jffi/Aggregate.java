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

import java.util.logging.Level;
import java.util.logging.Logger;

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

    /** A handle to the foreign interface to keep it alive as long as this object is alive */
    private final Foreign foreign;

    public Aggregate(long handle) {
        this(Foreign.getInstance(), handle);
    }
    
    Aggregate(Foreign foreign, long handle) {
        if (handle == 0L) {
            throw new NullPointerException("Invalid ffi_type handle");
        }
        this.foreign = foreign;
        this.handle = handle;
        this.type = foreign.getTypeType(handle);
        this.size = foreign.getTypeSize(handle);
        this.align = foreign.getTypeAlign(handle);
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
        foreign.freeAggregate(handle);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!disposed) {
                dispose();
            }
        } catch (Throwable t) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, 
                    "Exception when freeing FFI aggregate: %s", t.getLocalizedMessage());
        } finally {
            super.finalize();
        }
    }
}
