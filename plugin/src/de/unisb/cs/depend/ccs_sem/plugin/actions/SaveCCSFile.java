package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


public class SaveCCSFile implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

    public void dispose() {
        // nothing to do
    }

    public void init(IWorkbenchWindow window) {
        // nothing to do
    }

    public void run(IAction action) {
        final IEditorPart editorPart =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        editorPart.doSave(null);
        //PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveEditor(editorPart, false);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        // nothing to do
    }

}
