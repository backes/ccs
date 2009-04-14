package de.unisb.cs.depend.ccs_sem.plugin.preferencePage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.MyPreferenceStore;

public class CCSPreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {


    public class RebuildAllCCSProjectsJob extends Job {

        public RebuildAllCCSProjectsJob() {
            super("RebuildAllCCSProjects");
            setPriority(BUILD);
            setUser(true);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for (final IProject project: projects) {
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                    if (project.hasNature(Global.getNatureId())) {
                        project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                    }
                }
                return Status.OK_STATUS;
            } catch (final CoreException e) {
                return new Status(IStatus.ERROR, Global.getPluginID(), e.getMessage(), e);
            }
        }

    }

    private ComboFieldEditor ungardedErrorTypeEditor;
    private ComboFieldEditor unregularErrorTypeEditor;
    private StringFieldEditor maxGraphSizeFieldEditor;
    private int lastUnguardedErrorType;
    private int lastUnregularErrorType;

    public CCSPreferencePage() {
        super(GRID);
        setPreferenceStore(MyPreferenceStore.getStore());
        setDescription("Parameters for evaluating and visualizing CCS terms.");
        setTitle("CCS");
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    @Override
    public void createFieldEditors() {

        // dot executable path
        final FileFieldEditor dotFileFieldEditor = new ExecutableFieldEditor(
            MyPreferenceStore.getDotKey(), "Dot executable path",
            getFieldEditorParent());
        dotFileFieldEditor.setEmptyStringAllowed(false);
        addField(dotFileFieldEditor);

        unregularErrorTypeEditor = new ComboFieldEditor(
            MyPreferenceStore.getUnregularErrorTypeKey(),
            "Unregular process definition",
            new String[][] {
                {"Ignore", Integer.toString(ParsingProblem.IGNORE)},
                {"Error", Integer.toString(ParsingProblem.ERROR)},
                {"Warning", Integer.toString(ParsingProblem.WARNING)}
            },
            getFieldEditorParent());
        addField(unregularErrorTypeEditor);
        ungardedErrorTypeEditor = new ComboFieldEditor(
            MyPreferenceStore.getUnguardedErrorTypeKey(),
            "Unguarded process definition",
            new String[][] {
                {"Ignore", Integer.toString(ParsingProblem.IGNORE)},
                {"Error", Integer.toString(ParsingProblem.ERROR)},
                {"Warning", Integer.toString(ParsingProblem.WARNING)}
            },
            getFieldEditorParent());
        addField(ungardedErrorTypeEditor);
        
        // Maximal Graph Size
        maxGraphSizeFieldEditor = new StringFieldEditor(MyPreferenceStore.getMaxGraphSizeKey(),
        		"Maximal displayed graph size\n(0 for unlimited)",
        		getFieldEditorParent());
        addField(maxGraphSizeFieldEditor);
        
        BooleanFieldEditor tauEditor = new BooleanFieldEditor(MyPreferenceStore.getTauSemanticsKey(),
        		"Should Tau be visible?",
        		getFieldEditorParent());
        addField(tauEditor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        // nothing to do
    }

    @Override
    public boolean performOk() {
        initializeErrorWarning();
        if (!super.performOk())
            return false;
        if (errorWarningChanged() &&
                MessageDialog.openQuestion(getShell(), "Rebuild?",
                "The Error/Warning settings have changed. "
                + "A full rebuild of all CCS Projects is required "
                + "for changes to take effect. Do the full build now?")) {
            new RebuildAllCCSProjectsJob().schedule();
        }
        return true;
    }

    private void initializeErrorWarning() {
        lastUnguardedErrorType = MyPreferenceStore.getUnguardedErrorType();
        lastUnregularErrorType = MyPreferenceStore.getUnregularErrorType();
    }

    private boolean errorWarningChanged() {
        return lastUnguardedErrorType != MyPreferenceStore.getUnguardedErrorType()
            || lastUnregularErrorType != MyPreferenceStore.getUnregularErrorType();
    }
}