package de.unisb.cs.depend.ccs_sem.plugin.preferencePage;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.unisb.cs.depend.ccs_sem.plugin.Global;

public class CCSPreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    public CCSPreferencePage() {
        super(GRID);
        setPreferenceStore(Global.getPreferenceStore());
        setDescription("Parameters for evaluating and visualizing CCS terms.");
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    @Override
    public void createFieldEditors() {
        addField(new FileFieldEditor(Global.getPreferenceKeyDot(), "Dot executable path",
            getFieldEditorParent()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        // nothing to do
    }

}