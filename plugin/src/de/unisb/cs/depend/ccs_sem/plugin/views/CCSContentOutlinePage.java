package de.unisb.cs.depend.ccs_sem.plugin.views;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.editors.IParsingListener;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class CCSContentOutlinePage extends ContentOutlinePage
        implements IParsingListener {

    private Program program;
    private Composite outerOne;
    private Tree outlineTree;

    public CCSContentOutlinePage(CCSEditor editor) {
        final IDocument doc = editor.getDocument();
        if (doc instanceof CCSDocument) {
            ((CCSDocument)doc).addParsingListener(this);
        }
    }

    @Override
    public void createControl(Composite parent) {

        outerOne = new Composite(parent, SWT.NONE);

        outerOne.setLayout(new FillLayout());

        outlineTree = new Tree(outerOne, SWT.NONE);

        final TreeItem defaultItem = new TreeItem(outlineTree, SWT.NONE);
        defaultItem.setText("-- not build yet --");
    }

    @Override
    public void dispose() {
        outerOne.dispose();
    }

    @Override
    public Control getControl() {
        return outerOne;
    }

    @Override
    public void setActionBars(IActionBars actionBars) {
        // nothing to do
    }

    @Override
    public void setFocus() {
        outerOne.setFocus();
    }


    public void parsingDone(IDocument document, Program parsedProgram) {
        // TODO Auto-generated method stub

    }

}
