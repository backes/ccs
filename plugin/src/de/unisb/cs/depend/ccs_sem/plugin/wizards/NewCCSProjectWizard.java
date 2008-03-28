package de.unisb.cs.depend.ccs_sem.plugin.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import de.unisb.cs.depend.ccs_sem.plugin.Global;


public class NewCCSProjectWizard extends BasicNewProjectResourceWizard {

    public NewCCSProjectWizard() {
        super();
        setWindowTitle("New CCS Project");
    }

    @Override
    public boolean performFinish() {
        if (super.performFinish()) {
            try {
                final IProject newProject = getNewProject();
                // add the CCS Nature:
                final IProjectDescription description = newProject.getDescription();
                final String[] oldNatures = description.getNatureIds();
                boolean natureThere = false;
                for (final String nature: oldNatures)
                    natureThere |= nature.equals(Global.getNatureId());
                if (!natureThere) {
                    final String[] newNatures = new String[oldNatures.length + 1];
                    System.arraycopy(oldNatures, 0, newNatures, 0, oldNatures.length);
                    newNatures[oldNatures.length] = Global.getNatureId();
                    description.setNatureIds(newNatures);
                    newProject.setDescription(description, null);
                }
            } catch (final Throwable e) {
                StatusManager.getManager().handle(new Status(
                    IStatus.ERROR, Global.getPluginID(), e.getMessage(), e));
            }
            return true;
        }
        return false;
    }

}
