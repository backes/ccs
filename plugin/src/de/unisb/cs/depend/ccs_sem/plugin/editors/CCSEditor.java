package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.plugin.views.CCSContentOutlinePage;

public class CCSEditor extends TextEditor {

    private final ColorManager colorManager;
    private CCSContentOutlinePage fOutlinePage;

    public CCSEditor() {
        super();
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new CCSSourceViewerConfiguration(colorManager, this));
        setDocumentProvider(new CCSDocumentProvider());
    }

    @Override
    public void dispose() {
        super.dispose();
        colorManager.dispose();
        if (fOutlinePage != null)
            fOutlinePage.dispose();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class required) {
        if (IContentOutlinePage.class.equals(required)) {
            if (fOutlinePage == null)
                fOutlinePage = new CCSContentOutlinePage(getSourceViewer());
            return fOutlinePage;
        }
        return super.getAdapter(required);
    }

    public IDocument getDocument() {
        final ISourceViewer sourceViewer = getSourceViewer();
        return sourceViewer == null ? null : sourceViewer.getDocument();
    }

    public String getText() {
        final IDocument doc = getDocument();
        return doc == null ? "" : doc.get();
    }

}
