package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult.ReadDeclaration;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.plugin.editors.IParsingListener;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class CCSContentOutlinePage extends ContentOutlinePage
        implements IParsingListener {

    private Program program;
    private List<ReadDeclaration> displayedDeclarations;
    private CCSDocument ccsDocument;
    private final ISourceViewer sourceViewer;

    public CCSContentOutlinePage(ISourceViewer sourceViewer) {
        this.sourceViewer = sourceViewer;
        final IDocument doc = sourceViewer.getDocument();
        if (doc instanceof CCSDocument) {
            this.ccsDocument = (CCSDocument)doc;
        }
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        final CCSContentOutlineContentProvider contentProvider = new CCSContentOutlineContentProvider(sourceViewer);
        getTreeViewer().setContentProvider(contentProvider);
        getTreeViewer().addSelectionChangedListener(contentProvider);
        if (ccsDocument != null)
            ccsDocument.addParsingListener(this);
    }

    @Override
    public void setActionBars(IActionBars actionBars) {
        // nothing to do
    }

    public void parsingDone(IDocument document, ParseStatus result) {
        final ParsingResult parsingResult = result.getParsingResult();

        getControl().getDisplay().asyncExec(new Runnable() {
            @SuppressWarnings("synthetic-access")
            public void run() {
                getTreeViewer().setInput(parsingResult);
                //outlineTree.refresh();
            }
        });
    }

}
