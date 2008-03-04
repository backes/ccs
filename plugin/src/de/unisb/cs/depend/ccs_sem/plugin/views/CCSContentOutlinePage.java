package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class CCSContentOutlinePage implements IContentOutlinePage, Observer {
    private Composite outerOne;
    private Program program;

    public CCSContentOutlinePage(CCSEditor editor) {
        editor.registerReparsingListener(this);
    }

    public void createControl(Composite parent) {

        outerOne = new Composite(parent, SWT.MULTI);

        outerOne.setLayout(new FillLayout(SWT.VERTICAL));

        final Text text = new Text(outerOne, SWT.MULTI);
        text.setText("No outline available so far...");
        text.setEnabled(false);
    }

    public void dispose() {
        outerOne.dispose();
    }

    public Control getControl() {
        return outerOne;
    }

    public void setActionBars(IActionBars actionBars) {
        // nothing to do
    }

    public void setFocus() {
        outerOne.setFocus();
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        // nothing to do
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

    public void update(Observable o, Object arg) {
        // check if there was a change
        if (arg instanceof Program) {
            final Program newProgram = (Program) arg;
            if (newProgram.equals(program))
                return;
        }
        // TODO
    }

}
