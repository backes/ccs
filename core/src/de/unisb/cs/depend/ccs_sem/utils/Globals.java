package de.unisb.cs.depend.ccs_sem.utils;

import de.unisb.cs.depend.ccs_sem.exceptions.SystemException;


public final class Globals {

    private static final boolean MINIMIZE_EXPRESSIONS = false;
    private static String newline;

    private Globals() {
        // no instantiation allowed
    }

    public static String getNewline() {
        if (newline == null) {
            newline = System.getProperty("line.separator");
            if (newline == null)
                throw new SystemException("line.separator not defined");
        }
        return newline;
    }

    public static boolean isMinimizeExpressions() {
        return MINIMIZE_EXPRESSIONS;
    }

}
