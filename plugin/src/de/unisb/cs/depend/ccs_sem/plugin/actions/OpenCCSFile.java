package de.unisb.cs.depend.ccs_sem.plugin.actions;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


public class OpenCCSFile implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    public void dispose() {
        // nothing to do
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
        // nothing to do
    }

    public void run(IAction action) {

        try {
            final IWorkbenchPage page =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            FileDialog fd = new FileDialog(window.getShell());
            fd.setFilterExtensions(new String[] { "*.ccs", "*" });
            String filename = fd.open();
            if (filename == null)
                return;
            
            IPath path = new Path(filename);
            path.makeAbsolute();
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(makeProjectName(path));
            if (!project.exists()) {
                project.create(null);
                project.open(null);
                IProjectDescription desc = project.getDescription();
                desc.setLocation(path.uptoSegment(path.segmentCount() - 1));
                project.setDescription(desc, null);
            }
            if (!project.isOpen()) {
                project.open(null);
            }
            
            IFile projectFile = project.getFile(path.lastSegment());
            if (!projectFile.exists()) {
                projectFile.createLink(path, IResource.REPLACE, null);
            }
    
            try {
                IDE.openEditor(page, projectFile, true);
            } catch (Throwable t) {
                // ignore
                System.out.println("bla.");
            }
        } catch (CoreException e) {
            MessageDialog.openInformation(
                window.getShell(),
                "Exception",
                e.getMessage());
        }
    }

    private String makeProjectName(IPath path) {
        String filename = path.toString();
        filename = filename.replaceAll("[^a-zA-Z-_.]", "_");
        return filename;
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }

}
