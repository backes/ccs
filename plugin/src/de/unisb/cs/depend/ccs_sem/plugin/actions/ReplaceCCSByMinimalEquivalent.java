package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;


public class ReplaceCCSByMinimalEquivalent extends Action {

    private final TextSelection selection;

    public ReplaceCCSByMinimalEquivalent(CCSEditor editor) {
        super("Replace by minimal CCS equivalent");
        final ISelection tmpSelection = editor.getSelectionProvider().getSelection();
        this.selection = tmpSelection instanceof TextSelection ? ((TextSelection)tmpSelection) : null;
        setEnabled(selection != null && selection.getLength() > 0);
    }

    @Override
    public void run() {
        // TODO implement
        MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            "Not implemented yet", "Sorry, this feature is not implemented yet.");
    }

}
