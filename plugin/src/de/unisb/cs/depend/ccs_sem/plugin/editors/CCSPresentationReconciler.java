package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

import de.unisb.cs.depend.ccs_sem.lexer.tokens.False;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.IntegerToken;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.True;
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
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;


public class CCSPresentationReconciler implements IPresentationReconciler,
        IPresentationDamager, IPresentationRepairer, IParsingListener, ITextInputListener {

    private final class CreatePresentationJob extends Job {
        protected ParseStatus result;

        public CreatePresentationJob(ParseStatus result) {
            super("Update CCS Presentation");
            this.result = result;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {

            final TextPresentation presentation = new TextPresentation();
            createPresentation(presentation, result);

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
                                final IEditorInput input = editor.getEditorInput();
                                final IPath path = input instanceof IFileEditorInput
                                    ? ((IFileEditorInput)input).getFile().getFullPath()
                                    : null;
                                final IResource res = path == null ? null : ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                                if (res != null && res.exists()) {
                                    SafeRunner.run(new SafeRunnable() {
                                        public void run() throws CoreException {
                                            final ParsingResult parsingResult = result.getParsingResult();
                                            if (parsingResult != null) {
                                                updateMarkers(res, parsingResult);
                                                
                                            }
                                        }
                                    });
                                }
                            }        
                        }
                    }
                };
//                if (result.isSyncExec())
                    display.syncExec(runnable);
//                else
//                    display.asyncExec(runnable);
            }
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }
    }

    private static Map<IResource,Lock> resourceLocks = null;

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
    	// is never called
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
            final ParseStatus status = ((CCSDocument)doc).reparseIfNecessary();
            if (status == null) {
                try {
                    ((CCSDocument)doc).waitForReparsingDone();
                } catch (final InterruptedException e) {
                    // ignore and reset interruption flag
                    Thread.currentThread().interrupt();
                }
            } else {
                parsingDone(doc, status);
            }
        }
    }
    
    public synchronized void parsingDone(IDocument document, final ParseStatus result) {
        if (createPresentationJob  != null)
            createPresentationJob.cancel();
        createPresentationJob = new CreatePresentationJob(result);
        createPresentationJob.schedule();
        try { // DEBUG less concurrency for debugging
			createPresentationJob.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    public static void updateMarkers(IResource res,
            ParsingResult result) throws CoreException {
        if (result == null)
            return;

        final Lock resourceLock = getLock(res);
        assert resourceLock != null;

        final String[] attributeNames = new String[] {
                IMarker.SEVERITY,
                IMarker.CHAR_START,
                IMarker.CHAR_END,
                IMarker.LINE_NUMBER,
                IMarker.MESSAGE,
        };
        resourceLock.lock();
        try {
            final IMarker[] oldMarkersArray = res.findMarkers(Constants.MARKER_PROBLEM, true, IResource.DEPTH_INFINITE);
            final LinkedList<IMarker> oldMarkers = new LinkedList<IMarker>(Arrays.asList(oldMarkersArray));
            Collections.sort(oldMarkers, new Comparator<IMarker>() {
                public int compare(IMarker m1, IMarker m2) {
                    return m1.getAttribute(IMarker.CHAR_START, -1) - m2.getAttribute(IMarker.CHAR_START, -1);
                }
            });

            for (final ParsingProblem problem: result.parsingProblems) {
                final Integer severity;
                if (problem.getType() == ParsingProblem.ERROR)
                    severity = IMarker.SEVERITY_ERROR;
                else if (problem.getType() == ParsingProblem.WARNING)
                    severity = IMarker.SEVERITY_WARNING;
                else
                    continue;
                Integer start = problem.getStartPosition();
                Integer end;
                if (start == -1)
                    end = -1;
                else if (problem.getEndPosition() == -1)
                    end = start + 1;
                else
                    end = problem.getEndPosition()+1;
                Integer line = result.getLineOfOffset(start);
                // check if the marked position is after the document end (e.g.
                // "unexpected eof" markers)
                if (start == result.inputLength && end == start+1) {
                    line = result.getLineCount();
                    start = end = -1;
                }

                // search if this marker already exists:
                boolean found = false;
                final Iterator<IMarker> oldIt = oldMarkers.iterator();
                while (oldIt.hasNext()) {
                    final IMarker old = oldIt.next();

                    final Map<?, ?> oldAttributes = old.getAttributes();

                    final Object oldStart = oldAttributes.get(IMarker.CHAR_START);
                    final Object oldEnd = old.getAttribute(IMarker.CHAR_END);
                    final Object oldLine = old.getAttribute(IMarker.LINE_NUMBER);

                    if (oldStart instanceof Integer && (Integer)oldStart > problem.getStartPosition())
                        break;
                    if (oldEnd instanceof Integer && (Integer)oldEnd < problem.getEndPosition()) {
                        // this marker was not removed before, so it is not needed any more.
                        // remove it.
                        old.delete();
                        oldIt.remove();
                        continue;
                    }
                    if (severity.equals(oldAttributes.get(IMarker.SEVERITY))
                        && start.equals(oldStart)
                        && end.equals(oldEnd)
                        && line.equals(oldLine)
                        && (problem.getMessage() == null
                            ? old.getAttribute(IMarker.MESSAGE) == null
                            : problem.getMessage().equals(old.getAttribute(IMarker.MESSAGE)))) {
                        // found an equal marker!
                        found = true;
                        break;
                    }
                }
                if (found) {
                    oldIt.remove();
                    continue;
                }

                // it was not found, so create a new marker
                final IMarker marker = res.createMarker(Constants.MARKER_PROBLEM);
                marker.setAttributes(attributeNames,
                    new Object[] {
                        severity, start, end, line, problem.getMessage(),
                    });
            }
            // if there are old markers, remove them
            for (final IMarker old: oldMarkers)
                old.delete();
        } finally {
            resourceLock.unlock();
        }
    }

    private static synchronized Lock getLock(IResource res) {
        if (resourceLocks == null)
            resourceLocks = new HashMap<IResource, Lock>();

        Lock lock = resourceLocks.get(res);
        if (lock == null)
            resourceLocks.put(res, lock = new ReentrantLock());

        return lock;
    }

    protected void createPresentation(TextPresentation presentation, ParseStatus status) {
        final ParsingResult result = status.getParsingResult();

        presentation.setDefaultStyleRange(new StyleRange(0, result.inputLength, null, null));

        // the styles have to be in order. so we first add them to this sorted set
        final SortedSet<StyleRange> styleSet = new TreeSet<StyleRange>(new Comparator<StyleRange>() {
            public int compare(StyleRange o1, StyleRange o2) {
                return o1.start - o2.start;
            }
        });

        for (final ReadComment comment: result.comments) {
            styleSet.add(new StyleRange(comment.startPosition,
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
                    foregroundRGB = Constants.CONSTANT_VALUE_FOREGROUND_RGB;
                    backgroundRGB = Constants.CONSTANT_VALUE_BACKGROUND_RGB;
                    fontStyle = Constants.CONSTANT_VALUE_FONTSTYLE;
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
            } else if (tok instanceof True || tok instanceof False ||
                    tok instanceof IntegerToken) {
                foregroundRGB = Constants.CONSTANT_VALUE_FOREGROUND_RGB;
                backgroundRGB = Constants.CONSTANT_VALUE_BACKGROUND_RGB;
                fontStyle = Constants.CONSTANT_VALUE_FONTSTYLE;
            }

            if (foregroundRGB != null || backgroundRGB != null || fontStyle != SWT.NORMAL) {
                styleSet.add(new StyleRange(tok.getStartPosition(),
                    tok.getLength(),
                    colorManager.getColor(foregroundRGB),
                    colorManager.getColor(backgroundRGB),
                    fontStyle));
            }
        }

        for (final StyleRange s: styleSet)
            presentation.addStyleRange(s);
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