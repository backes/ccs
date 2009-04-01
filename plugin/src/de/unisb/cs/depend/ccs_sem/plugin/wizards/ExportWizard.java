package de.unisb.cs.depend.ccs_sem.plugin.wizards;

import org.eclipse.jface.wizard.Wizard;

public class ExportWizard extends Wizard {

	public ExportWizard() {
		super();
		setHelpAvailable(false);
		setWindowTitle("Export results");
	}
	
	@Override
	public boolean performFinish() {
		System.out.println("Wizard finished");
		// TODO ExportWizard finish to implement
		return true;
	}

	@Override
	public void addPages() {
		addPage(new ExportWizardPage());
	}
}
