package de.unisb.cs.depend.ccs_sem.plugin.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.unisb.cs.depend.ccs_sem.plugin.Global;

public class SimulationPerspective implements IPerspectiveFactory {

	private IPageLayout factory;
	
	public SimulationPerspective() {
		super();
	}
	
	public void createInitialLayout(IPageLayout layout) {
		this.factory = layout;
        factory.setFixed(false);
        addViews();
   	}

	private void addViews() {
		factory.setEditorAreaVisible(false);
		final IFolderLayout center =
            factory.createFolder(
                "center",
                IPageLayout.RIGHT,
                0.75f,
                null);
		
		final IFolderLayout leftTop =
            factory.createFolder(
                "leftTop",
                IPageLayout.LEFT,
                0.25f,
                "center");
		
		final IFolderLayout leftBottom =
            factory.createFolder(
                "leftBottom",
                IPageLayout.BOTTOM,
                0.5f,
                "leftTop");
		
		leftTop.addView(Global.getChooseActionViewId());
		leftBottom.addView(Global.getTraceViewId());
		center.addView(Global.getTopLevelGraphViewId());
	}

}
