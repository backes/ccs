package de.unisb.cs.depend.ccs_sem.plugin.views.simulation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class TopLevelGraphView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite top = new Composite(parent,SWT.None);
		new Label(top,SWT.None).setText("Test TopLevelGraphView");
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}