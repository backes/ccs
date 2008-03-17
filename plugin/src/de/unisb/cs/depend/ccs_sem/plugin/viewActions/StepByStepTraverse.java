package de.unisb.cs.depend.ccs_sem.plugin.viewActions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;


public class StepByStepTraverse implements IViewActionDelegate {

    private IViewPart view;

    public void init(IViewPart view) {
        this.view = view;
    }

    public void run(IAction action) {
        // TODO
        new de.unisb.cs.depend.ccs_sem.plugin.actions.StepByStepTraverse().run(null);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // ignore
    }

}
