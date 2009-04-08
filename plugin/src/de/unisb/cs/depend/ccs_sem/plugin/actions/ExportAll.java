package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import de.unisb.cs.depend.ccs_sem.plugin.wizards.ExportWizard;

public class ExportAll extends Action implements IViewActionDelegate {

	private IViewPart view;
	
	public ExportAll() {
		super("Export Results");
		setImageDescriptor(ImageDescriptor.createFromURL(getClass().getResource("/resources/icons/export.png")));
		setToolTipText("Opens a Wizard to export anything.");
	}
	
	public void init(IViewPart view) {
		this.view = view;
	}

	public void run(IAction action) {
		run();
	}
	
	@Override
	public void run() {
		Wizard wizard = new ExportWizard();
		WizardDialog wizDia = new WizardDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
				, wizard);
		wizDia.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// ignore
	}

}
