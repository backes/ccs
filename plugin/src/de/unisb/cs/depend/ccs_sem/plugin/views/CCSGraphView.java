package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.views.components.CCSFrame;


public class CCSGraphView extends ViewPart implements ISelectionListener {

    private PageBook myPages;

    private Composite defaultComp;

    private Control currentPage;

    private final Map<CCSEditor, CCSFrame> frames = new HashMap<CCSEditor, CCSFrame>();

    @Override
    public void createPartControl(Composite parent) {

        myPages = new PageBook(parent, SWT.None);

        defaultComp = new Composite(myPages, SWT.NONE);
        defaultComp.setLayout(new GridLayout(1, true));

        final Label defaultLabel = new Label(defaultComp, SWT.None);
        defaultLabel.setText("No CCS file opened.");
        defaultLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        myPages.showPage(currentPage = defaultComp);

        final IWorkbenchPartSite site = getSite();
        final IWorkbenchPage page = site.getPage();
        page.addSelectionListener(this);

        final IEditorPart activeEditor = page.getActiveEditor();
        if (activeEditor != null)
            selectionChanged(activeEditor, null);
    }

    @Override
    public void dispose() {
        myPages.dispose();
        super.dispose();
    }

    @Override
    public void setFocus() {
        myPages.setFocus();
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part instanceof IEditorPart)
            showGraphFor((IEditorPart) part, false);
    }

    public synchronized void update() {
        if (currentPage instanceof CCSFrame) {
            ((CCSFrame)currentPage).updateEvaluation();
        }
    }

    public synchronized void showGraphFor(IEditorPart activeEditor, boolean updateGraph) {
        if (activeEditor instanceof CCSEditor) {
            final CCSEditor editor = (CCSEditor) activeEditor;
            CCSFrame ccsFrame = frames.get(editor);
            if (ccsFrame == null)
                frames.put(editor, ccsFrame = new CCSFrame(myPages, editor));

            myPages.showPage(currentPage = ccsFrame);
            if (updateGraph)
                ccsFrame.showGraph(true);
        } else {
            myPages.showPage(currentPage = defaultComp);
        }
    }

}
