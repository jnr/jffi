/*
 * Copyright (C) 2007, 2008 Wayne Meissner
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to load the jffi stub library
 */
final class Init {
    private static volatile boolean loaded = false;

    static final String stubLoaderClassName = Init.class.getPackage().getName() + ".internal.StubLoader";
    
    // prevent instantiation
    private Init() {}
    
    /**
     * Loads the stub library
     */
    static void load() {
        if (loaded) {
            return;
        }
        List<Throwable> failureCauses = new ArrayList<Throwable>();
        List<ClassLoader> loaders = getClassLoaders();
        
        for (ClassLoader cl : loaders) {
            try {
                Class<?> c = Class.forName(stubLoaderClassName, true, cl);
                Method isLoaded = c.getDeclaredMethod("isLoaded", new Class[0]);
                loaded |= Boolean.class.cast(isLoaded.invoke(c, new Object[0]));
                if (!loaded) {
                    Method getFailureCause = c.getDeclaredMethod("getFailureCause", new Class[0]);
                    throw Throwable.class.cast(getFailureCause.invoke(c, new Object[0]));
                }

            } catch (IllegalAccessException ex) {
                failureCauses.add(ex);
            
            } catch (InvocationTargetException ex) {
                failureCauses.add(ex);
            
            } catch (ClassNotFoundException ex) {
                failureCauses.add(ex);
            
            } catch (Throwable throwable) {
                if (throwable instanceof UnsatisfiedLinkError) {
                    throw (UnsatisfiedLinkError) throwable;
                }
                throw newLoadError(throwable);
            }
        }

        if (!loaded && !failureCauses.isEmpty()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            for (Throwable t : failureCauses) {
                t.printStackTrace(pw);
            }

            throw new UnsatisfiedLinkError(sw.toString());
        }
    }

    private static List<ClassLoader> getClassLoaders() {
        List<ClassLoader> loaders = new ArrayList<ClassLoader>();
        
        try {
            loaders.add(ClassLoader.getSystemClassLoader());
        } catch (SecurityException ex) {
        }
        
        try {
            loaders.add(Thread.currentThread().getContextClassLoader());
        } catch (SecurityException ex) {
        }
        
        loaders.add(Init.class.getClassLoader());
        
        // Remove all the nulls except one - in the case where this is loaded 
        // from the bootstrap classloader
        int nullCount = 0;
        for (Iterator<ClassLoader> it = loaders.iterator(); it.hasNext(); ) {
            if (it.next() == null && ++nullCount > 1) {
                it.remove();
            }
        }
        
        return Collections.unmodifiableList(loaders);
    }

    private static UnsatisfiedLinkError newLoadError(Throwable cause) {
        UnsatisfiedLinkError error = new UnsatisfiedLinkError();
        error.initCause(cause);

        return error;
    }
}
