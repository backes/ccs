package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PlatformUI;

import de.unisb.cs.depend.ccs_sem.plugin.wizards.NewCCSFileWizard;


public class NewCCSFile extends Action implements IWorkbenchWindowActionDelegate, IWorkbenchWindowPulldownDelegate {

    private IWorkbenchWindow window;
    private ISelection selection;

    public void dispose() {
        // nothing to do
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
        // nothing to do
    }

    public void run(IAction action) {

        final NewCCSFileWizard wizard = new NewCCSFileWizard();
        final IWorkbench workbench = window == null ? PlatformUI.getWorkbench() : window.getWorkbench();
        final IStructuredSelection sel = selection instanceof IStructuredSelection ? (IStructuredSelection)selection : null;
        wizard.init(workbench, sel);

        final WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
        dialog.open();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    public Menu getMenu(Control parent) {
        final Menu menu = new Menu(parent);
        final ActionContributionItem newCCSFileItem = new ActionContributionItem(this);
        newCCSFileItem.fill(menu, -1);
        return menu;
    }
}
