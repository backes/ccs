package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class Help extends Action implements IViewActionDelegate {

	private IViewPart view;
	private String key;
	
	public Help(String key) {
		super("Help");
		this.key = key;
		
		setImageDescriptor(ImageDescriptor.createFromURL(getClass().getResource(
				"/resources/icons/help.png")));
		setToolTipText("Opens a Help-View.");
	}
	
	public void init(IViewPart view) {
		this.view = view;
	}

	public void run(IAction action) {
		run();
	}
	
	@Override
	public void run() {
		System.out.println("Magic-Key: " + key);
	}

	public void selectionChanged(IAction action, ISelection selection) {} // ignore

}
