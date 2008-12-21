
package com.kenai.jffi;

public interface ClosureBuffer {
    int getInt8(int index);
    int getInt16(int index);
    int getInt32(int index);
    long getInt64(int index);
    float getFloat(int index);
    double getDouble(int index);

    void setInt8Return(int value);
    void setInt16Return(int value);
    void setInt32Return(int value);
    void setInt64Return(long value);
    void setFloatReturn(float value);
    void setDoubleReturn(double value);
}
