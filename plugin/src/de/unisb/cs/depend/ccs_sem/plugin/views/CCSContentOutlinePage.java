package de.unisb.cs.depend.ccs_sem.plugin.views;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

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
            final ParseStatus result = ccsDocument.reparseIfNecessary();
            if (result != null) {
                parsingDone(ccsDocument, result);
            }
        }
    }

    @Override
    public void setActionBars(IActionBars actionBars) {
        // nothing to do
    }

    public void parsingDone(IDocument document, final ParseStatus result) {
        if (getControl().isDisposed()) {
            if (document instanceof CCSDocument)
                ((CCSDocument)document).removeParsingListener(this);
            return;
        }
        final Runnable runnable = new Runnable() {
            public void run() {
                if (getControl().isDisposed())
                    return;
                final TreeViewer treeViewer2 = getTreeViewer();
                if (firstDisplay) {
                    firstDisplay = false;
                    treeViewer2.setInput(result);
                    treeViewer2.expandAll();
                } else {
                    final Object[] expanded = treeViewer2.getExpandedElements();
                    treeViewer2.setInput(result);
                    treeViewer2.setExpandedElements(expanded);
                }
            }
        };
        if (result.isSyncExec())
            getControl().getDisplay().syncExec(runnable);
        else
            getControl().getDisplay().asyncExec(runnable);
    }

    @Override
    protected TreeViewer getTreeViewer() {
        return super.getTreeViewer();
    }

}
