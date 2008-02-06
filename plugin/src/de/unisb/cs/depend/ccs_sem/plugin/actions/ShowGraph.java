package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView;


public class ShowGraph implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

    public void dispose() {
        // nothing to do
    }

    public void init(IWorkbenchWindow window) {
        // nothing to do
    }

    public void run(IAction action) {
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(Global.getGraphViewId());
            final IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            CCSGraphView.showGraphFor(activeEditor, true);
        } catch (final PartInitException e) {
            e.printStackTrace();
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        // nothing to do
    }

}
