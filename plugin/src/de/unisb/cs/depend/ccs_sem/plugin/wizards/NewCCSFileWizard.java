package de.unisb.cs.depend.ccs_sem.plugin.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * This is a sample new wizard. Its role is to create a new file
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "ccs". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class NewCCSFileWizard extends Wizard implements INewWizard {
    private NewCCSFileWizardPage page;
    private IStructuredSelection selection;

    /**
     * Constructor for NewCCSFileWizard.
     */
    public NewCCSFileWizard() {
        super();
        setWindowTitle("New CCS File");
        //setNeedsProgressMonitor(true);
    }

    /**
     * Adding the page to the wizard.
     */

    @Override
    public void addPages() {
        page = new NewCCSFileWizardPage("newCCSFilePage", selection);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        return page.finish();
    }

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}