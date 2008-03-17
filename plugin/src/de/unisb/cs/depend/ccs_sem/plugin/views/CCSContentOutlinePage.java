package de.unisb.cs.depend.ccs_sem.plugin.views;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.plugin.editors.IParsingListener;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;


public class CCSContentOutlinePage extends ContentOutlinePage
        implements IParsingListener {

    private CCSDocument ccsDocument;
    private final ISourceViewer sourceViewer;
    protected volatile boolean firstDisplay = true;

    public CCSContentOutlinePage(ISourceViewer sourceViewer) {
        super();
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
        if (ccsDocument != null) {
            ccsDocument.addParsingListener(this);
            try {
                ccsDocument.reparseNow(false);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                // ... and ignore
            }
        }
    }

    @Override
    public void setActionBars(IActionBars actionBars) {
        // nothing to do
    }

    public void parsingDone(IDocument document, ParseStatus result) {
        final ParsingResult parsingResult = result.getParsingResult();

        getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                final TreeViewer treeViewer2 = getTreeViewer();
                final Object[] expanded = treeViewer2.getExpandedElements();
                treeViewer2.setInput(parsingResult);
                if (firstDisplay) {
                    firstDisplay = false;
                    treeViewer2.expandAll();
                } else {
                    treeViewer2.setExpandedElements(expanded);
                }
            }
        });
    }

    @Override
    protected TreeViewer getTreeViewer() {
        return super.getTreeViewer();
    }

}
