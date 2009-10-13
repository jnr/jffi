
package com.kenai.jffi;

public interface CallInfo {
    int getParameterCount();
    public Type getReturnType();
    public Type getParameterType(int parameterIndex);
}
