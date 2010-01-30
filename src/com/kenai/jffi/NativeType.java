package com.kenai.jffi;

/**
 *
 */
public enum NativeType {

    VOID(Foreign.TYPE_VOID),
    FLOAT(Foreign.TYPE_FLOAT),
    DOUBLE(Foreign.TYPE_DOUBLE),
    LONGDOUBLE(Foreign.TYPE_LONGDOUBLE),
    UINT8(Foreign.TYPE_UINT8),
    SINT8(Foreign.TYPE_SINT8),
    UINT16(Foreign.TYPE_UINT16),
    SINT16(Foreign.TYPE_SINT16),
    UINT32(Foreign.TYPE_UINT32),
    SINT32(Foreign.TYPE_SINT32),
    UINT64(Foreign.TYPE_UINT64),
    SINT64(Foreign.TYPE_SINT64),
    POINTER(Foreign.TYPE_POINTER),
    UCHAR(Foreign.TYPE_UCHAR),
    SCHAR(Foreign.TYPE_SCHAR),
    USHORT(Foreign.TYPE_USHORT),
    SSHORT(Foreign.TYPE_SSHORT),
    UINT(Foreign.TYPE_UINT),
    SINT(Foreign.TYPE_SINT),
    ULONG(Foreign.TYPE_ULONG),
    SLONG(Foreign.TYPE_SLONG);

    final int ffiType;

    NativeType(int ffiType) {
        this.ffiType = ffiType;
    }
}
