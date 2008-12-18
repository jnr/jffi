/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jffi;

/**
 *
 * @author wayne
 */
public interface InvocationBuffer {
    public abstract int getInt8Result();
    public abstract int getInt16Result();
    public abstract int getInt32Result();
    public abstract int getInt64Result();
}
