package de.unisb.cs.depend.ccs_sem.plugin;


public class Global {

    private static final String PLUGIN_ID = "de.unisb.cs.depend.ccs_sem.plugin";

    // CCS Perspective
    private static final String GRAPH_VIEW_ID =
        "de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView";
    private static final String STEP_BY_STEP_TRAVERSE_VIEW_ID =
        "de.unisb.cs.depend.ccs_sem.plugin.views.StepByStepTraverseView";
    private static final String LTL_CHECKER_VIEW_ID = 
    	"de.unisb.cs.depend.ccs_sem.plugin.views.LTLCheckerView";
    private static final String COUNTER_EXAMPLE_VIEW_ID = 
    	"de.unisb.cs.depend.ccs_sem.plugin.views.CounterExampleView";
    
    // Simulation Perspective
    private static final String SIMULATION_PERSPECTIVE_ID =
    	"de.unisb.cs.depend.ccs_sem.plugin.perspectives.SimulationPerspective";
    
    private static final String TRACE_VIEW_ID =
    	"de.unisb.cs.depend.ccs_sem.plugin.views.simulation.TraceView";
    private static final String CHOOSE_ACTION_VIEW_ID =
    	"de.unisb.cs.depend.ccs_sem.plugin.views.simulation.ChooseActionView";
    private static final String TOP_LEVEL_GRAPH_VIEW_ID =
    	"de.unisb.cs.depend.ccs_sem.plugin.views.simulation.TopLevelGraphView";
    
    private static final String ACTION_SET_ID = "de.unisb.cs.depend.ccs_sem.plugin.actionSet";

    private static final String NATURE_ID = "de.unisb.cs.depend.ccs_sem.plugin.ccsNature";
    private static final String BUILDER_ID = "de.unisb.cs.depend.ccs_sem.plugin.ccsBuilder";

    private static final String RESOURCE_CREATION_ACTION_SET_ID = "de.unisb.cs.depend.ccs_sem.plugin.ccsRessourceCreationActionSet";

    private static final String NEW_CCS_FILE_WIZARD_ID = "de.unisb.cs.depend.ccs_sem.plugin.wizards.NewCCSFileWizard";
    private static final String NEW_CCS_PROJECT_WIZARD_ID = "de.unisb.cs.depend.ccs_sem.plugin.wizards.NewCCSProjectWizard";
    private static final String EXPORT_LTL_WIZARD_ID = "de.unisb.cs.depend.ccs_sem.plugin.wizards.ExportLTLWizard";

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
    
    public static String getLTLCheckerViewId() {
        return LTL_CHECKER_VIEW_ID;
    }
    
    public static String getCounterExampleViewId() {
        return COUNTER_EXAMPLE_VIEW_ID;
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
    
    public static String getExportLTLWizardId() {
    	return EXPORT_LTL_WIZARD_ID;
    }

    public static boolean isWindows() {
        final String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }
    
    public static String getTraceViewId() {
    	return TRACE_VIEW_ID;
    }
    
    public static String getChooseActionViewId() {
    	return CHOOSE_ACTION_VIEW_ID;
    }

    public static String getTopLevelGraphViewId() {
    	return TOP_LEVEL_GRAPH_VIEW_ID;
    }
    
    public static String getSimulationPerspectiveID() {
    	return SIMULATION_PERSPECTIVE_ID;
    }
}
