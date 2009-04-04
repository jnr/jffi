
package com.kenai.jffi;

/**
 * Represents a native closure.
 *
 * A Closure is implemented by java code to act as a C function pointer.
 */
public interface Closure {
    public void invoke(Buffer buffer);

    /**
     * An interface to the native callback parameter buffer.
     */
    public static interface Buffer {

        /**
         * Gets the value of an 8 bit integer parameter.
         *
         * @param index The parameter index
         * @return An 8 bit integer value.
         */
        byte getByte(int index);

        /**
         * Gets the value of a 16 bit integer parameter.
         *
         * @param index The parameter index
         * @return A 16 bit integer value.
         */
        short getShort(int index);

        /**
         * Gets the value of a 32 bit integer parameter.
         *
         * @param index The parameter index
         * @return A 32 bit integer value.
         */
        int getInt(int index);

        /**
         * Gets the value of a 64 bit integer parameter.
         *
         * @param index The parameter index
         * @return A 64 bit integer value.
         */
        long getLong(int index);

        /**
         * Gets the value of a 32 bit floating point parameter.
         *
         * @param index The parameter index
         * @return A 32 bit floating point value.
         */
        float getFloat(int index);

        /**
         * Gets the value of a 64 bit floating point parameter.
         *
         * @param index The parameter index
         * @return A 64 bit floating point value.
         */
        double getDouble(int index);

        /**
         * Gets the value of a native pointer parameter.
         *
         * @param index The parameter index
         * @return A native memory address.
         */
        long getAddress(int index);

        /**
         * Gets the address of a struct parameter that is passed by value.
         *
         * @param index The parameter index
         * @return A native memory address.
         */
        long getStruct(int index);

        /**
         * Sets the closure return value to an 8 bit integer value.
         *
         * @param value The 8 bit integer value to return from the closure.
         */
        void setByteReturn(byte value);

        /**
         * Sets the closure return value to a 16 bit integer value.
         *
         * @param value The 16 bit integer value to return from the closure.
         */
        void setShortReturn(short value);

        /**
         * Sets the closure return value to a 32 bit integer value.
         *
         * @param value The 32 bit integer value to return from the closure.
         */
        void setIntReturn(int value);

        /**
         * Sets the closure return value to a 64 bit integer value.
         *
         * @param value The 64 bit integer value to return from the closure.
         */
        void setLongReturn(long value);

        /**
         * Sets the closure return value to a 32 bit floating point value.
         *
         * @param value The 32 bit floating point value to return from the closure.
         */
        void setFloatReturn(float value);

        /**
         * Sets the closure return value to a 64 bit floating point value.
         *
         * @param value The 64 bit floating point value to return from the closure.
         */
        void setDoubleReturn(double value);

        /**
         * Sets the closure return value to a native pointer value.
         *
         * @param address The native pointer value to return from the closure.
         */
        void setAddressReturn(long address);

        /**
         * Sets the closure return value to the contents of a struct
         *
         * @param address The address of a native struct to return as a struct value from the closure.
         */
        void setStructReturn(long address);

        /**
         * Sets the closure return value to the contents of a struct
         *
         * @param data Struct data packed into a byte array to return as a struct value from the closure.
         * @param offset the offset within the byte array to start copying data
         */
        void setStructReturn(byte[] data, int offset);
    }

    /**
     * A Handle is allocated by the {@link ClosureManager}, as a strong reference
     * to the native closure trampoline.
     */
    public static interface Handle {
        /**
         * Gets the native code address of the closure.
         *
         * This can be passed into a native function that takes a function pointer.
         *
         * @return The native address of the closure code.
         */
        long getAddress();
    }
}
