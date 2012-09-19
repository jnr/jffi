package com.kenai.jffi;

public final class FaultException extends RuntimeException {
    private final int signal;

    FaultException(int signal, long[] ip, long[] procname, long[] libname) {
        super(String.format("Received signal %d", signal));
        setStackTrace(createStackTrace(ip, procname, libname, fillInStackTrace().getStackTrace()));
        this.signal = signal;
    }

    private static StackTraceElement[] createStackTrace(long[] ip, long[] procname, long[] libname, StackTraceElement[] existingTrace) {
        java.util.List<StackTraceElement> trace = new java.util.ArrayList<StackTraceElement>();

        for (int i = 0; i < ip.length; i++) {
            String procName = new String(Foreign.getZeroTerminatedByteArray(procname[i]));
            String libName = new String(Foreign.getZeroTerminatedByteArray(libname[i]));
            trace.add(new StackTraceElement("native", procName, libName, -1));
        }
        trace.addAll(java.util.Arrays.asList(existingTrace));

        return trace.toArray(new StackTraceElement[trace.size()]);
    }

    public int getSignal() {
        return signal;
    }

}
