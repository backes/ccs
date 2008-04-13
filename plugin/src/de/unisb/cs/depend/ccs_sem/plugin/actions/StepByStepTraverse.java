package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.views.StepByStepTraverseView;


public class StepByStepTraverse extends Action implements
        IWorkbenchWindowActionDelegate, IEditorActionDelegate, IViewActionDelegate {

    public static final ISafeRunnable safeRunnable = new SafeRunnable() {

        public void run() throws Exception {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            final IWorkbenchWindow activeWorkbenchWindow = workbench == null ? null
                : workbench.getActiveWorkbenchWindow();
            final IWorkbenchPage activePage = activeWorkbenchWindow == null ? null
                : activeWorkbenchWindow.getActivePage();
            if (activePage == null)
                throw new RuntimeException("No active page found.");
            final IViewPart view = activePage.showView(Global.getStepByStepTraverseViewId());
            final IEditorPart activeEditor = activePage.getActiveEditor();
            if (activeEditor != null && view instanceof StepByStepTraverseView) {
                ((StepByStepTraverseView)view).changeEditor(activeEditor);
            }
        }
    };

    public StepByStepTraverse() {
        super("Step by step traverse");
        setImageDescriptor(ImageDescriptor.createFromURL(getClass().getResource("/icons/step.gif")));
        setDisabledImageDescriptor(ImageDescriptor.createFromURL(getClass().getResource("/icons/step_dis.gif")));
        setToolTipText("Step by step traverse");
    }

    public void dispose() {
        // nothing to do
    }

    public void init(IWorkbenchWindow window) {
        // nothing to do
    }

    @Override
    public void run() {
        SafeRunner.run(safeRunnable);
    }

    public void run(IAction action) {
        run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        // nothing to do
    }

    public void init(IViewPart view) {
        // nothing to do
    }

}
