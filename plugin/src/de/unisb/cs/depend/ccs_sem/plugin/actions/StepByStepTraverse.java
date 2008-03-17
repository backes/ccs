package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.views.StepByStepTraverseView;


public class StepByStepTraverse implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

    public void dispose() {
        // nothing to do
    }

    public void init(IWorkbenchWindow window) {
        // nothing to do
    }

    public void run(IAction action) {
        try {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            final IWorkbenchWindow activeWorkbenchWindow = workbench == null ? null
                : workbench.getActiveWorkbenchWindow();
            final IWorkbenchPage activePage = activeWorkbenchWindow == null ? null
                : activeWorkbenchWindow.getActivePage();
            if (activePage != null) {
                final IViewPart view = activePage.showView(Global.getStepByStepTraverseViewId());
                final IEditorPart activeEditor = activePage.getActiveEditor();
                if (activeEditor != null && view instanceof StepByStepTraverseView) {
                    ((StepByStepTraverseView)view).changeEditor(activeEditor);
                }
            }
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
