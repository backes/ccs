package de.unisb.cs.depend.ccs_sem.gui.components;

import java.io.File;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;

import de.unisb.cs.depend.ccs_sem.exceptions.FileReadException;
import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
import de.unisb.cs.depend.ccs_sem.gui.CSSDocument;


public class CSSEditorPanel extends JEditorPane {

    private static final long serialVersionUID = 4502547458903616422L;
    
    public CSSEditorPanel() {
        super();
        try {
            getDocument().insertString(0, "abc", null);
        } catch (BadLocationException e) {
            throw new InternalSystemException(e);
        }
        /*
        super();
        setDocument(new CSSDocument());
        */
    }

    public CSSEditorPanel(File file) throws FileReadException {
        super();
        openFile(file);
    }

    private void openFile(File file) throws FileReadException {
        CSSDocument doc;
        try {
            doc = new CSSDocument(file);
        } catch (IOException e) {
            throw new FileReadException(e);
        }
        setDocument(doc);
    }

}
