package de.unisb.cs.depend.ccs_sem.plugin.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;


public class CCSContentOutlinePage implements IContentOutlinePage {
    Composite outerOne;

    public CCSContentOutlinePage(IDocumentProvider documentProvider, CCSEditor editor) {
        // TODO Auto-generated constructor stub
    }

    public void createControl(Composite parent) {

        outerOne = new Composite(parent, SWT.MULTI);

        outerOne.setLayout(new FillLayout(SWT.VERTICAL));

        final Text text = new Text(outerOne, SWT.MULTI);
        text.setText("No outline available so far...");
        text.setEnabled(false);
    }

    public void dispose() {
        // nothing to do
    }

    public Control getControl() {
        return outerOne;
    }

    public void setActionBars(IActionBars actionBars) {
        // TODO Auto-generated method stub

    }

    public void setFocus() {
        outerOne.setFocus();
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        // TODO Auto-generated method stub

    }

    public ISelection getSelection() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeSelectionChangedListener(
                                               ISelectionChangedListener listener) {
        // TODO Auto-generated method stub

    }

    public void setSelection(ISelection selection) {
        // TODO Auto-generated method stub

    }

    public void setInput(IEditorInput input) {
        // TODO Auto-generated method stub
    }

}
