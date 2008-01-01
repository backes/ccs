package de.unisb.cs.depend.ccs_sem.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;


public class CCSElement implements Element {
    
    private Document doc;

    public CCSElement(CSSDocument document) {
        this.doc = document;
    }

    public AttributeSet getAttributes() {
        return new SimpleAttributeSet();
    }

    public Document getDocument() {
        return doc;
    }

    public Element getElement(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getElementCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getElementIndex(int offset) {
        // TODO Auto-generated method stub
        return -1;
    }

    public int getEndOffset() {
        // TODO Auto-generated method stub
        return doc.getLength();
    }

    public String getName() {
        // TODO Auto-generated method stub
        return "TODO";
    }

    public Element getParentElement() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getStartOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isLeaf() {
        // TODO Auto-generated method stub
        return true;
    }

}
