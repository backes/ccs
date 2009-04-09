package de.unisb.cs.depend.ccs_sem.plugin.views.simulation;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.plugin.Global;

public class SimulationPerspectiveView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		IWorkbenchPage page = getSite().getPage();
		if( page.getPerspective().getId().equals(
				Global.getSimulationPerspectiveID() ) ) {
			System.out.println("Richtige perspektive");
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
