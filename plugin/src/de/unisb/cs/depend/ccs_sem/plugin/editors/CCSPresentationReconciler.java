package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.KeywordToken;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.OperatorToken;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult.ReadComment;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.UnknownRecursiveExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstString;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;


public class CCSPresentationReconciler implements IPresentationReconciler,
        IPresentationDamager, IPresentationRepairer, IParsingListener, ITextInputListener {

    private final class CreatePresentationJob extends Job {
        private final IDocument document;
        protected ParseStatus result;

        public CreatePresentationJob(IDocument document, ParseStatus result) {
            super("Update CCS Presentation");
            this.document = document;
            this.result = result;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {

            final TextPresentation presentation =
                createPresentationAfterParsing(document, result);

            final StyledText textWidget = textViewer.getTextWidget();
            final Display display = textWidget == null || textWidget.isDisposed()
                ? null : textWidget.getDisplay();
            if (display != null) {
                final Runnable runnable = new Runnable() {
                    public void run() {
                        if (getDocModCount(textViewer) == result.getDocModCount()
                                && textViewer.getTextWidget() != null
                                && !textViewer.getTextWidget().isDisposed()) {
                            textViewer.changeTextPresentation(presentation, true);

                            if (editor != null) {
                                IEditorInput input = editor.getEditorInput();
                                IPath path = input instanceof IFileEditorInput
                                    ? ((IFileEditorInput)input).getFile().getFullPath()
                                    : null;
                                final IResource res = path == null ? null : ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                                if (res != null && res.exists()) {
                                    SafeRunner.run(new SafeRunnable() {
                                        public void run() throws CoreException {
                                            updateMarkers(res, result);
                                        }

                                    });
                                }
                            }
                        }
                    }
                };
                if (result.isSyncExec())
                    display.syncExec(runnable);
                else
                    display.asyncExec(runnable);
            }
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }
    }

    protected ITextViewer textViewer;
    private final ColorManager colorManager;
    protected final CCSEditor editor;
    private Job createPresentationJob = null;


    public CCSPresentationReconciler(ColorManager colorManager, CCSEditor editor) {
        this.colorManager = colorManager;
        this.editor = editor;
    }

    public IPresentationDamager getDamager(String contentType) {
        return this;
    }

    public IPresentationRepairer getRepairer(String contentType) {
        return this;
    }

    public void install(ITextViewer viewer) {
        textViewer = viewer;
        viewer.addTextInputListener(this);
        final IDocument doc = viewer.getDocument();
        if (doc instanceof CCSDocument) {
            ((CCSDocument)doc).addParsingListener(this);
        }
    }

    public void uninstall() {
        textViewer = null;
    }

    protected long getDocModCount(ITextViewer textViewer) {
        final IDocument doc = textViewer.getDocument();
        if (doc instanceof AbstractDocument)
            return ((AbstractDocument)doc).getModificationStamp();
        // else:
        return -1;
    }

    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event,
            boolean documentPartitioningChanged) {
        return new Region(0, event.getDocument().getLength());
    }

    public void setDocument(IDocument document) {
        assert document == null || textViewer == null
            || textViewer.getDocument() == null
            || document.equals(textViewer.getDocument());
    }

    public void createPresentation(TextPresentation presentation,
            ITypedRegion damage) {
        assert textViewer != null;

        final IDocument doc = textViewer.getDocument();
        if (doc instanceof CCSDocument) {
            ((CCSDocument)doc).reparseNow();
            try {
                ((CCSDocument)doc).waitForReparsingDone();
            } catch (final InterruptedException e) {
                // ignore and reset interruption flag
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void parsingDone(IDocument document, final ParseStatus result) {
        if (createPresentationJob  != null)
            createPresentationJob.cancel();
        createPresentationJob = new CreatePresentationJob(document, result);
        createPresentationJob.schedule();
    }

    protected void updateMarkers(IResource res,
            ParseStatus status) throws CoreException {

        IMarker[] oldMarkersArray = res.findMarkers(Constants.MARKER_PROBLEM, true, IResource.DEPTH_INFINITE);
        SortedSet<IMarker> oldMarkers = new TreeSet<IMarker>(new Comparator<IMarker>() {
            public int compare(IMarker m1, IMarker m2) {
                return m1.getAttribute(IMarker.CHAR_START, -1) - m2.getAttribute(IMarker.CHAR_START, -1);
            }
        });
        oldMarkers.addAll(Arrays.asList(oldMarkersArray));

        final ParsingResult result = status.getParsingResult();
        if (result == null)
            return;

        for (final ParsingProblem problem: result.parsingProblems) {
            int severity = problem.getType() == ParsingProblem.ERROR ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING;
            int start = problem.getStartPosition();
            int end;
            if (start == -1)
                end = -1;
            else {
                end = problem.getEndPosition()+1;
                if (end == 0) // endPosition was -1, but startPosition is != -1
                    end = start+1;
            }
            // search if this marker already exists:
            boolean found = false;
            Iterator<IMarker> oldIt = oldMarkers.iterator();
            while (oldIt.hasNext()) {
                IMarker old = oldIt.next();
                int oldStart = old.getAttribute(IMarker.CHAR_START, -1);
                int oldEnd = old.getAttribute(IMarker.CHAR_END, -1);

                if (oldStart > problem.getStartPosition())
                    break;
                if (oldEnd < problem.getEndPosition()) {
                    // this marker was not removed before, so it is not needed any more.
                    // remove it.
                    old.delete();
                    oldIt.remove();
                    continue;
                }
                if (old.getAttribute(IMarker.SEVERITY, severity+1) == severity
                        && oldStart == start && oldEnd == end
                        && (problem.getMessage() == null
                            ? old.getAttribute(IMarker.MESSAGE) == null
                            : problem.getMessage().equals(old.getAttribute(IMarker.MESSAGE)))) {
                    // found an equal marker!
                    oldMarkers.remove(old);
                    found = true;
                    break;
                }
            }
            if (found)
                continue;

            // it was not found, so create a new marker
            final IMarker marker = res.createMarker(Constants.MARKER_PROBLEM);
            marker.setAttribute(IMarker.SEVERITY,
                severity);
            if (start != -1)
                marker.setAttribute(IMarker.CHAR_START, start);
            if (end != -1)
                marker.setAttribute(IMarker.CHAR_END, end);
            marker.setAttribute(IMarker.MESSAGE, problem.getMessage());
        }
        // if there are old markers, remove them
        for (IMarker old: oldMarkers)
            old.delete();
    }

    protected TextPresentation createPresentationAfterParsing(IDocument document, ParseStatus status) {
        final ParsingResult result = status.getParsingResult();

        final TextPresentation presentation = new TextPresentation();
        presentation.setDefaultStyleRange(new StyleRange(0, document.getLength(), null, null));

        for (final ReadComment comment: result.comments) {
            presentation.addStyleRange(new StyleRange(comment.startPosition,
                    comment.endPosition - comment.startPosition + 1,
                    colorManager.getColor(Constants.COMMENT_FOREGROUND_RGB),
                    colorManager.getColor(Constants.COMMENT_BACKGROUND_RGB),
                    Constants.COMMENT_FONTSTYLE));
        }

        for (final Token tok: result.tokens) {
            RGB foregroundRGB = null;
            RGB backgroundRGB = null;
            int fontStyle = SWT.NORMAL;

            if (tok instanceof KeywordToken) {
                foregroundRGB = Constants.KEYWORD_FOREGROUND_RGB;
                backgroundRGB = Constants.KEYWORD_BACKGROUND_RGB;
                fontStyle = Constants.KEYWORD_FONTSTYLE;
            } else if (tok instanceof OperatorToken) {
                foregroundRGB = Constants.OPERATOR_FOREGROUND_RGB;
                backgroundRGB = Constants.OPERATOR_BACKGROUND_RGB;
                fontStyle = Constants.OPERATOR_FONTSTYLE;
            } else if (tok instanceof Identifier) {
                final Object o = result.identifiers.get(tok);
                if (o instanceof Parameter) {
                    // no style so far... (parameter on input action or process
                    // variable)
                } else if (o instanceof ParameterReference) {
                    // can be channel or value, is not distinguished here
                    foregroundRGB = Constants.PARAMETER_REFERENCE_FOREGROUND_RGB;
                    backgroundRGB = Constants.PARAMETER_REFERENCE_BACKGROUND_RGB;
                    fontStyle = Constants.PARAMETER_REFERENCE_FONTSTYLE;
                } else if (o instanceof ConstantValue) {
                    // no style so far...
                } else if (o instanceof ConstString) {
                    // no style so far...
                } else if (o instanceof Range) {
                    // no style so far...
                } else if (o instanceof UnknownRecursiveExpression) {
                    foregroundRGB = Constants.PROCESS_REFERENCE_FOREGROUND_RGB;
                    backgroundRGB = Constants.PROCESS_REFERENCE_BACKGROUND_RGB;
                    fontStyle = Constants.PROCESS_REFERENCE_FONTSTYLE;
                } else if (o instanceof Channel) {
                    // no style so far...
                } else if (o instanceof ProcessVariable) {
                    // no style so far... (process variable definition)
                } else {
                    // we should not get here, if o is set
                    assert o == null;
                }
            }

            if (foregroundRGB != null || backgroundRGB != null || fontStyle != SWT.NORMAL) {
                final StyleRange style = new StyleRange(tok.getStartPosition(),
                    tok.getLength(),
                    colorManager.getColor(foregroundRGB),
                    colorManager.getColor(backgroundRGB),
                    fontStyle);
                presentation.addStyleRange(style);
            }
        }

        return presentation;
    }

    public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
        // not very interesting...
    }

    public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
        if (oldInput instanceof CCSDocument) {
            ((CCSDocument)oldInput).removeParsingListener(this);
        }
        if (newInput instanceof CCSDocument) {
            ((CCSDocument)newInput).addParsingListener(this);
            final ParseStatus result = ((CCSDocument)newInput).reparseIfNecessary();
            if (result != null) {
                parsingDone(newInput, result);
            }
        }
    }

}
