package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class CCSDocumentProvider extends FileDocumentProvider {

    @Override
    protected IDocument createEmptyDocument() {
        return new CCSDocument();
    }

    @Override
    protected boolean setDocumentContent(IDocument document,
            IEditorInput editorInput, String encoding) throws CoreException {
        if (editorInput instanceof IURIEditorInput) {
            final URI uri = ((IURIEditorInput)editorInput).getURI();
            final File file = new File(uri);
            FileInputStream stream;
            try {
                stream = new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                return false;
            }
            try {
                setDocumentContent(document, stream, encoding);
            } finally {
                try {
                    stream.close();
                } catch (final IOException x) {
                    // ignore
                }
            }
            return true;
        }
        return super.setDocumentContent(document, editorInput, encoding);
    }

}
