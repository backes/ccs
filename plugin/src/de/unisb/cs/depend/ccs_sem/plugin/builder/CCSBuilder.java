package de.unisb.cs.depend.ccs_sem.plugin.builder;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSPresentationReconciler;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;

public class CCSBuilder extends IncrementalProjectBuilder {

    class CCSDeltaVisitor implements IResourceDeltaVisitor {

        public boolean visit(IResourceDelta delta) {
            final IResource resource = delta.getResource();
            switch (delta.getKind()) {
            case IResourceDelta.ADDED:
                // handle added resource
                parseCCS(resource);
                break;
            case IResourceDelta.REMOVED:
                // handle removed resource
                break;
            case IResourceDelta.CHANGED:
                // handle changed resource
                parseCCS(resource);
                break;
            }
            // return true to continue visiting children.
            return true;
        }
    }

    class CCSResourceVisitor implements IResourceVisitor {

        public boolean visit(IResource resource) {
            parseCCS(resource);
            // return true to continue visiting children.
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
        if (kind == FULL_BUILD) {
            fullBuild(monitor);
        } else {
            final IResourceDelta delta = getDelta(getProject());
            if (delta == null) {
                fullBuild(monitor);
            } else {
                incrementalBuild(delta, monitor);
            }
        }
        return null;
    }

    void parseCCS(IResource resource) {
        if (resource instanceof IFile
                && "ccs".equals(((IFile) resource).getFileExtension())) {
            try {
                final IFile file = (IFile) resource;
                final Reader input = new InputStreamReader(file.getContents(), file.getCharset());
                final ParseCCSProgramJob job = new ParseCCSProgramJob(input);
                final IProgressMonitor monitor = new NullProgressMonitor();
                final ParseStatus status = job.run(monitor);
                final ParsingResult result = status.getParsingResult();
                if (result != null) {
                    CCSPresentationReconciler.updateMarkers(resource, result);
                }
            } catch (final Exception e) {
                StatusManager.getManager().handle(new Status(IStatus.ERROR,
                    Global.getPluginID(), e.getMessage(), e));
            }
        }
    }

    protected void fullBuild(final IProgressMonitor monitor)
            throws CoreException {
        getProject().accept(new CCSResourceVisitor());
    }

    protected void incrementalBuild(IResourceDelta delta,
            IProgressMonitor monitor) throws CoreException {
        // the visitor does the work.
        delta.accept(new CCSDeltaVisitor());
    }
}
