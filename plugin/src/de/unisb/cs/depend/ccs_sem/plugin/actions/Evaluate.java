package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView;


public class Evaluate extends Action implements IViewActionDelegate {

    private IViewPart view;

    public Evaluate() {
        super("Evaluate");
        setImageDescriptor(ImageDescriptor.createFromURL(getClass().getResource("/icons/refresh.gif")));
        setDisabledImageDescriptor(ImageDescriptor.createFromURL(getClass().getResource("/icons/refresh_dis.gif")));
        setToolTipText("Evaluate (refresh this graph and other informations)");
    }

    public void init(IViewPart view) {
        this.view = view;
    }

    public void run(IAction action) {
        run();
    }

    @Override
    public void run() {
        IViewPart myView = view;
        if (myView == null) {
            final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            final IWorkbenchPage activePage = activeWorkbenchWindow == null ? null : activeWorkbenchWindow.getActivePage();
            myView = activePage == null ? null : activePage.findView(Global.getGraphViewId());
        }
        if (myView instanceof CCSGraphView)
            ((CCSGraphView)myView).update();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // ignore
    }

}
