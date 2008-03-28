package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;


public class StepByStepViewCopyToClipboard implements IViewActionDelegate {

    private ISelection selection;

    public void init(IViewPart view) {
        // null operation
    }

    public void run(IAction action) {
        System.out.println(selection);
        // TODO Auto-generated method stub

    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

}
