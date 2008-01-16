package de.unisb.cs.depend.ccs_sem.exporters.bcg;

import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;

public class BCGWriter {

    private static boolean isInitialized = false;
    private static boolean isOpen = false;
    private static int lastStateNr = Integer.MIN_VALUE;

    public static boolean initialize() {
        if (isInitialized)
            return true;

        try {
            System.loadLibrary("BCGWriter");
            return isInitialized = true;
        } catch (final UnsatisfiedLinkError e) {
            System.err.println("BCGwrite library not found. Did you run 'make' in the 'lib' directory?");
            return false;
        }
    }

    public static void open(String filename, int noInitialState, String comment) throws ExportException {
        if (isOpen)
            throw new ExportException("BCG file is already open.");

        isOpen = true;
        lastStateNr = Integer.MIN_VALUE;
        open0(filename, noInitialState, comment);
    }

    private static native void open0(String filename, int noInitialState, String comment);

    public static void writeTransition(int fromState, int toState,
            String label) throws ExportException {
        if (fromState < lastStateNr)
            throw new ExportException("The numbers of the source state must increase monotously.");

        writeTransition0(fromState, toState, label);
    }

    private static native void writeTransition0(int fromState, int toState,
            String label);

    public static void close() throws ExportException {
        if (!isOpen)
            throw new ExportException("BCG file is not open.");

        isOpen = false;
        close0();
    }

    private static native void close0();

    private BCGWriter() {
        // just to forbid instantiation
    }

}
