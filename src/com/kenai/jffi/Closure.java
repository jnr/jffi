
package com.kenai.jffi;

public interface Closure {
    public void invoke(Buffer buffer);

    public static interface Buffer {
        @Deprecated
        int getInt8(int index);
        @Deprecated
        int getInt16(int index);
        @Deprecated
        int getInt32(int index);
        @Deprecated
        long getInt64(int index);
        @Deprecated
        void setInt8Return(int value);
        @Deprecated
        void setInt16Return(int value);
        @Deprecated
        void setInt32Return(int value);
        @Deprecated
        void setInt64Return(long value);

        byte getByte(int index);
        short getShort(int index);
        int getInt(int index);
        long getLong(int index);
        float getFloat(int index);
        double getDouble(int index);
        long getAddress(int index);


        void setByteReturn(byte value);
        void setShortReturn(short value);
        void setIntReturn(int value);
        void setLongReturn(long value);
        void setFloatReturn(float value);
        void setDoubleReturn(double value);
        void setAddressReturn(long address);
    }

    public static interface Handle {
        long getAddress();
    }
}
