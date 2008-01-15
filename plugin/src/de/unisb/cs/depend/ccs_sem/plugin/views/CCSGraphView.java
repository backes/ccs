package de.unisb.cs.depend.ccs_sem.plugin.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;


public class CCSGraphView extends ViewPart implements ISelectionListener {

    private Composite myComp;

    @Override
    public void createPartControl(Composite parent) {

        myComp = new Composite(parent, SWT.None);

        getSite().getPage().addSelectionListener(this);
        // TODO Auto-generated method stub

    }

    @Override
    public void setFocus() {
        myComp.setFocus();
        // TODO Auto-generated method stub

    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        System.out.println("Selection changed");
        // TODO Auto-generated method stub

    }

}
