package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class CCSPresentationReconciler implements IPresentationReconciler,
        IPresentationDamager, IPresentationRepairer, IParsingListener {

    protected ITextViewer textViewer;
    private ParseCCSProgramJob reparsingJob;
    private final ColorManager colorManager;
    private ParseStatus lastResult;


    public CCSPresentationReconciler(ColorManager colorManager) {
        this.colorManager = colorManager;
    }

    public IPresentationDamager getDamager(String contentType) {
        return this;
    }

    public IPresentationRepairer getRepairer(String contentType) {
        return this;
    }

    public void install(ITextViewer viewer) {
        textViewer = viewer;
        final IDocument doc = viewer.getDocument();
        if (doc instanceof CCSDocument) {
            ((CCSDocument)doc).addParsingListener(this);
        }
    }

    public void uninstall() {
        // nothing to do
    }

    protected void updatePresentation(final ParseStatus result) {
        this.lastResult = result;
        final TextPresentation newPresentation = new TextPresentation();
        createPresentation0(newPresentation);


        final StyledText textWidget = textViewer.getTextWidget();
        final Display display = textWidget == null ? null : textWidget.getDisplay();
        if (display != null) {
            display.asyncExec(new Runnable() {

                public void run() {
                    if (result.getDocModCount() == getDocModCount(textViewer)) {
                        textViewer.changeTextPresentation(newPresentation , true);
                    }
                }

            });
        }
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
        if (doc instanceof CCSDocument)
            ((CCSDocument)doc).reparseNow();
        // TODO
    }

    private void createPresentation0(TextPresentation presentation) {
        final IDocument doc = textViewer.getDocument();
        final int length = doc == null ? 0 : doc.getLength();
        presentation.setDefaultStyleRange(new StyleRange(0, length, null, null));

        // TODO remove, and add correct presentation
        presentation.addStyleRange(new StyleRange(0, 5, colorManager.getColor(new RGB(255, 0, 0)), null, SWT.BOLD));

    }

    public void parsingDone(IDocument document, Program parsedProgram) {

        // TODO Auto-generated method stub

    }

}
