package de.unisb.cs.depend.ccs_sem.plugin;

import org.eclipse.jface.preference.IPreferenceStore;

import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;


public class MyPreferenceStore {

    private static IPreferenceStore preferenceStore;
    static {
        initializePreferenceStore();
    }

    private static final String PREFERENCE_DOT_KEY = "dotExecutable";
    private static final String PREFERENCE_DEFAULT_DOT = "dot";

    private static final String PREFERENCE_UNREGULAR_ERROR_TYPE_KEY = "unregularErrorType";
    private static final String PREFERENCE_UNGUARDED_ERROR_TYPE_KEY = "unguardedErrorType";

    private MyPreferenceStore() {
        // this private constructor is never called
        assert false;
    }

    private static void initializePreferenceStore() {
        preferenceStore = Activator.getDefault().getPreferenceStore();
        preferenceStore.setDefault(PREFERENCE_DOT_KEY, PREFERENCE_DEFAULT_DOT);
        preferenceStore.setDefault(PREFERENCE_UNREGULAR_ERROR_TYPE_KEY, ParsingProblem.WARNING);
        preferenceStore.setDefault(PREFERENCE_UNGUARDED_ERROR_TYPE_KEY, ParsingProblem.ERROR);
    }

    public static IPreferenceStore getStore() {
        return preferenceStore;
    }

    public static String getDotKey() {
        return PREFERENCE_DOT_KEY;
    }

    public static String getDot() {
        return getStore().getString(getDotKey());
    }

    public static String getUnregularErrorTypeKey() {
        return PREFERENCE_UNREGULAR_ERROR_TYPE_KEY;
    }

    public static int getUnregularErrorType() {
        return getStore().getInt(getUnregularErrorTypeKey());
    }

    public static String getUnguardedErrorTypeKey() {
        return PREFERENCE_UNGUARDED_ERROR_TYPE_KEY;
    }

    public static int getUnguardedErrorType() {
        return getStore().getInt(getUnguardedErrorTypeKey());
    }

}
