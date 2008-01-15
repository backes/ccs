package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.plugin.views.CCSContentOutlinePage;

public class CCSEditor extends TextEditor {

    private final ColorManager colorManager;
    private CCSContentOutlinePage fOutlinePage;

    public CCSEditor() {
        super();
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new CCSConfiguration(colorManager));
        setDocumentProvider(new CCSDocumentProvider());
    }

    @Override
    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class required) {
        if (IContentOutlinePage.class.equals(required)) {
            if (fOutlinePage == null) {
                fOutlinePage = new CCSContentOutlinePage(getDocumentProvider(), this);
                if (getEditorInput() != null)
                    fOutlinePage.setInput(getEditorInput());
            }
            return fOutlinePage;
        }
        return super.getAdapter(required);
    }

}
