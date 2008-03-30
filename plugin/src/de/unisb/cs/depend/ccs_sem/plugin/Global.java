package de.unisb.cs.depend.ccs_sem.plugin;


public class Global {

    private static final String PLUGIN_ID = "de.unisb.cs.depend.ccs_sem.plugin";

    private static final String GRAPH_VIEW_ID =
        "de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView";
    private static final String STEP_BY_STEP_TRAVERSE_VIEW_ID =
        "de.unisb.cs.depend.ccs_sem.plugin.views.StepByStepTraverseView";

    private static final String ACTION_SET_ID = "de.unisb.cs.depend.ccs_sem.plugin.actionSet";

    private static final String NATURE_ID = "de.unisb.cs.depend.ccs_sem.plugin.ccsNature";
    private static final String BUILDER_ID = "de.unisb.cs.depend.ccs_sem.plugin.ccsBuilder";

    private static final String RESOURCE_CREATION_ACTION_SET_ID = "de.unisb.cs.depend.ccs_sem.plugin.ccsRessourceCreationActionSet";

    private static final String NEW_CCS_FILE_WIZARD_ID = "de.unisb.cs.depend.ccs_sem.plugin.wizards.NewCCSFileWizard";
    private static final String NEW_CCS_PROJECT_WIZARD_ID = "de.unisb.cs.depend.ccs_sem.plugin.wizards.NewCCSProjectWizard";

    private Global() {
        // this constructor is never called
        assert false;
    }

    public static String getPluginID() {
        return PLUGIN_ID;
    }

    public static String getGraphViewId() {
        return GRAPH_VIEW_ID;
    }

    public static String getStepByStepTraverseViewId() {
        return STEP_BY_STEP_TRAVERSE_VIEW_ID;
    }

    public static String getActionSetId() {
        return ACTION_SET_ID;
    }

    public static String getNatureId() {
        return NATURE_ID;
    }

    public static String getBuilderId() {
        return BUILDER_ID;
    }

    public static String getResourceCreationActionSetId() {
        return RESOURCE_CREATION_ACTION_SET_ID;
    }

    public static String getNewCCSFileWizardId() {
        return NEW_CCS_FILE_WIZARD_ID;
    }

    public static String getNewCCSProjectWizardId() {
        return NEW_CCS_PROJECT_WIZARD_ID;
    }

}
