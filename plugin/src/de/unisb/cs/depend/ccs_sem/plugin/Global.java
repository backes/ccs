package de.unisb.cs.depend.ccs_sem.plugin;

import org.eclipse.jface.preference.IPreferenceStore;


public class Global {

    private static final String PLUGIN_ID = "de.unisb.cs.depend.ccs_sem.plugin";

    private static final String PREFERENCE_KEY_DOT = "dotExecutable";
    private static final String PREFERENCE_DEFAULT_DOT = "dot";

    private static final String GRAPH_VIEW_ID = "de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView";

    private static final String ACTION_SET_ID = "de.unisb.cs.depend.ccs_sem.plugin.actionSet";

    private static IPreferenceStore preferenceStore = null;


    private Global() {
        // this constructor is never called
        assert false;
    }

    public static String getPluginID() {
        return PLUGIN_ID;
    }

    public static String getPreferenceDot() {
        return getPreferenceStore().getString(getPreferenceKeyDot());
    }

    public static String getPreferenceKeyDot() {
        return PREFERENCE_KEY_DOT;
    }

    public static String getGraphViewId() {
        return GRAPH_VIEW_ID;
    }

    public static String getActionSetId() {
        return ACTION_SET_ID;
    }

    public static IPreferenceStore getPreferenceStore() {
        if (preferenceStore == null) {
            preferenceStore = Activator.getDefault().getPreferenceStore();

            preferenceStore.setDefault(PREFERENCE_KEY_DOT, PREFERENCE_DEFAULT_DOT);
        }
        return preferenceStore;
    }

}
