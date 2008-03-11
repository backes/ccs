package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class CCSDocumentProvider extends FileDocumentProvider {

    @Override
    protected IDocument createEmptyDocument() {
        return new CCSDocument();
    }

}
