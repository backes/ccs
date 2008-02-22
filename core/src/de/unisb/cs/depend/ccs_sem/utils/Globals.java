package de.unisb.cs.depend.ccs_sem.utils;

import java.util.concurrent.ConcurrentHashMap;

import de.unisb.cs.depend.ccs_sem.exceptions.SystemException;


public final class Globals {

    private static final boolean MINIMIZE_EXPRESSIONS = false;
    private static final int CONCURRENCY_LEVEL = 32;
    private static String newline;

    private Globals() {
        // no instantiation allowed
    }

    /**
     * Just for caching. Yield the same result as
     * <code>System#getProperty("line.separator")</code>.
     *
     * @return the system-wide newline character
     */
    public static String getNewline() {
        if (newline == null) {
            newline = System.getProperty("line.separator");
            if (newline == null)
                throw new SystemException("line.separator not defined");
        }
        return newline;
    }

    /**
     * Minimizing expressions means e.g.
     * <ul>
     * <li>"0 | X" -> "X"</li>
     * <li>"0 + X" -> "X"</li>
     * </ul>
     * @return whether to minimize Expressions
     */
    public static boolean isMinimizeExpressions() {
        return MINIMIZE_EXPRESSIONS;
    }

    /**
     * @return the default concurrency level for {@link ConcurrentHashMap}s or
     * {@link ConcurrentHashSet}.
     */
    public static int getConcurrencyLevel() {
        return CONCURRENCY_LEVEL;
    }

}
