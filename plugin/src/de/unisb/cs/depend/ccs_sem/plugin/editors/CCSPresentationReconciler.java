package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SafeRunner;
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

    protected ITextViewer textViewer;
    private final ColorManager colorManager;
    protected final CCSEditor editor;


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

    public void parsingDone(IDocument document, final ParseStatus result) {
        final TextPresentation presentation =
            createPresentationAfterParsing(document, result);

        final StyledText textWidget = textViewer.getTextWidget();
        final Display display = textWidget == null ? null : textWidget.getDisplay();
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
                                        res.deleteMarkers(Constants.MARKER_PROBLEM, true, IResource.DEPTH_INFINITE);

                                        addMarkers(res, result);
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
    }

    protected void addMarkers(IResource res,
            ParseStatus status) throws CoreException {

        final ParsingResult result = status.getParsingResult();
        if (result == null)
            return;

        for (final ParsingProblem problem: result.parsingProblems) {
            final IMarker marker = res.createMarker(Constants.MARKER_PROBLEM);
            marker.setAttribute(IMarker.SEVERITY,
                problem.getType() == ParsingProblem.ERROR ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING);
            marker.setAttribute(IMarker.CHAR_START, problem.getStartPosition());
            // CHAR_END is exclusive!
            marker.setAttribute(IMarker.CHAR_END, problem.getEndPosition()+1);
            marker.setAttribute(IMarker.MESSAGE, problem.getMessage());
        }
    }

    private TextPresentation createPresentationAfterParsing(IDocument document, ParseStatus status) {
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
                    // we should not get here
                    assert false;
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
