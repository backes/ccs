package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


public class OpenCCSFile implements IWorkbenchWindowActionDelegate {

    public void dispose() {
        // nothing to do
    }

    public void init(IWorkbenchWindow window) {
        // nothing to do
    }

    public void run(IAction action) {
        // TODO
        final IFile file = null;
        final IWorkbenchPage page =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            IDE.openEditor(page, file, true);
        } catch (final PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }

}
