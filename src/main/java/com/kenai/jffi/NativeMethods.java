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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to register native methods on a class
 */
public final class NativeMethods {

    /**
     * Store a link from the class to the native method holder in a weak
     * hash map, so as long as the class remains alive, the native memory for the
     * structures remains alive.
     *
     * This doesn't seem to be necessary on sun's jvm, but best do it to be safe.
     */
    private static final Map<Class, NativeMethods> registeredMethods
            = new WeakHashMap<Class, NativeMethods>();

    private final ResourceHolder memory;

    private NativeMethods(ResourceHolder memory) {
        this.memory = memory;
    }

    /**
     * Registers the native methods for a class.
     *
     * @param clazz The java class to register the native methods for.
     * @param methods The list of methods to attach to the class.
     */
    public static synchronized final void register(Class clazz, List<NativeMethod> methods) {
        //
        // Calculate how much memory is needed for the function names + signatures
        //
        int stringSize = 0;
        for (NativeMethod m : methods) {
            stringSize += m.name.getBytes().length + 1;
            stringSize += m.signature.getBytes().length + 1;
        }

        final int ptrSize = Platform.getPlatform().addressSize() / 8;
        final MemoryIO mm = MemoryIO.getInstance();

        //
        // Each JNINativeMethod struct is 3 pointers
        // i.e.
        //   typedef struct {
        //      char *name;
        //      char *signature;
        //      void *fnPtr;
        //   } JNINativeMethod;
        int structSize = methods.size() * 3 * ptrSize;
        long memory = mm.allocateMemory(structSize + stringSize, true);
        if (memory == 0L) {
            throw new OutOfMemoryError("could not allocate native memory");
        }

        NativeMethods nm = new NativeMethods(new ResourceHolder(mm, memory));

        int off = 0;
        int stringOff = structSize;
        for (NativeMethod m : methods) {
            byte[] name = m.name.getBytes();
            long nameAddress = memory + stringOff;
            stringOff += name.length + 1;
            mm.putZeroTerminatedByteArray(nameAddress, name, 0, name.length);
            
            byte[] sig = m.signature.getBytes();
            long sigAddress = memory + stringOff;
            stringOff += sig.length + 1;
            mm.putZeroTerminatedByteArray(sigAddress, sig, 0, sig.length);
            
            mm.putAddress(memory + off, nameAddress); off += ptrSize;
            mm.putAddress(memory + off, sigAddress); off += ptrSize;
            mm.putAddress(memory + off, m.function); off += ptrSize;
        }

        if (Foreign.getInstance().registerNatives(clazz, memory, methods.size()) != Foreign.JNI_OK) {
            throw new RuntimeException("failed to register native methods");
        }

        registeredMethods.put(clazz, nm);
    }

    /**
     * Removes all native method attachments for the specified class.
     *
     * @param clazz The class to unregister the native methods on.
     */
    public static synchronized final void unregister(Class clazz) {
        if (!registeredMethods.containsKey(clazz)) {
            throw new IllegalArgumentException("methods were not registered on class via NativeMethods.register");
        }

        if (Foreign.getInstance().unregisterNatives(clazz) != Foreign.JNI_OK) {
            throw new RuntimeException("failed to unregister native methods");
        }
        
        registeredMethods.remove(clazz);
    }
    
    private static final class ResourceHolder {
        private final MemoryIO mm;
        private final long memory;

        public ResourceHolder(MemoryIO mm, long memory) {
            this.mm = mm;
            this.memory = memory;
        }
        
        @Override
        protected void finalize() throws Throwable {
            try {
                mm.freeMemory(memory);
            } catch (Throwable t) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, 
                    "Exception when freeing native method struct array: %s", t.getLocalizedMessage());
            } finally {
                super.finalize();
            }
        }
    }
}
