package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView;


public class ShowGraph implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

    private static final String GRAPH_VIEW_ID = "de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView";

    public void dispose() {
        // nothing to do
    }

    public void init(IWorkbenchWindow window) {
        // nothing to do
    }

    public void run(IAction action) {
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(GRAPH_VIEW_ID);
            CCSGraphView.showGraphFor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(), true);
        } catch (final PartInitException e) {
            // TODO Auto-generated catch block
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
