package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import de.unisb.cs.depend.ccs_sem.plugin.MyPreferenceStore;
import de.unisb.cs.depend.ccs_sem.plugin.utils.ISemanticDependend;

public class ToggleSemantic extends Action implements IViewActionDelegate, ISemanticDependend {

	private static final String[] images = {"tauTurnOff.png","tauTurnOn.png"};
	private static final String[] toolTip = {"Make interaction invisible" ,
			"Make interaction visible"};
	
	public ToggleSemantic() {
		super("ToggleSemantic");
		updateInfos();
		MyPreferenceStore.addSemanticObserver(this);
	}
	
	public void dispose() {
		MyPreferenceStore.removeSemanticObserver(this);
	}
	
	// true <-> tau visible
	private String getImageFile() {
		return images[MyPreferenceStore.getVisibleTauSemantic() ? 0 : 1];
	}
	
	private String getToolTip() {
		return toolTip[MyPreferenceStore.getVisibleTauSemantic() ? 0 : 1];
	}
	
	private void updateInfos() {
		setImageDescriptor(ImageDescriptor.createFromURL(getClass().getResource("/resources/icons/"+getImageFile())));
        setToolTipText(getToolTip());
	}
	
	public void run(IAction action) {
		run();
	}
	
	@Override
	public void run() {
		MyPreferenceStore.setVisibleTauSemantic(!MyPreferenceStore.getVisibleTauSemantic());
	}

	public void updateSemantic() {
		updateInfos();
	}
	
	// ignore
	public void init(IViewPart view) {}
	public void selectionChanged(IAction action, ISelection selection) {}
}
