package de.unisb.cs.depend.ccs_sem.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GapContent;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;


public class CSSDocument extends AbstractDocument {

    private static final long serialVersionUID = -22551510844728123L;
    private Element rootElem;

    public CSSDocument() {
        super(createEmptyContent());
        initializeRootElem();
    }

    private static GapContent createEmptyContent() {
        GapContent content = new GapContent();
        // TODO remove
        try {
            content.insertString(0, "ab\ncd\n");
        } catch (BadLocationException e) {
            throw new InternalSystemException(e);
        }
        return content;
    }
    
    public CSSDocument(File file) throws IOException {
        super(readcontent(file));
        initializeRootElem();
    }

    private static Content readcontent(File file) throws IOException {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        Content content = createEmptyContent();
        
        String line;
        while ((line = br.readLine()) != null) {
            try {
                content.insertString(content.length(), line);
            } catch (BadLocationException e) {
                throw new InternalSystemException(e);
            }
        }
        
        return content;
    }
    
    private void initializeRootElem() {
        rootElem = new CCSElement(this);
    }

    @Override
    public Element getDefaultRootElement() {
        return rootElem;
    }

    @Override
    public Element getParagraphElement(int pos) {
        return rootElem;
    }

}
