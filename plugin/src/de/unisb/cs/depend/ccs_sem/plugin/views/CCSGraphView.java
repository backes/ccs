package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;
import de.unisb.cs.depend.ccs_sem.plugin.views.components.CCSFrame;


public class CCSGraphView extends ViewPart implements ISelectionListener {

    private static List<CCSGraphView> views = new ArrayList<CCSGraphView>(1);

    private PageBook myPages;

	private Composite defaultComp;

    private Control currentView;

    private final Map<CCSEditor, CCSFrame> frames = new HashMap<CCSEditor, CCSFrame>();

    public CCSGraphView() {
        synchronized (views) {
            views.add(this);
        }
    }

    @Override
    public void createPartControl(Composite parent) {

        myPages = new PageBook(parent, SWT.None);

        defaultComp = new Composite(myPages, SWT.NONE);
        defaultComp.setLayout(new GridLayout(1, true));

        Label defaultLabel = new Label(defaultComp, SWT.None);
        defaultLabel.setText("No CCS file opened.");
        defaultLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        myPages.showPage(currentView = defaultComp);

        final IWorkbenchPartSite site = getSite();
        final IWorkbenchPage page = site.getPage();
        page.addSelectionListener(this);

        final IEditorPart activeEditor = page.getActiveEditor();
        if (activeEditor != null)
            selectionChanged(activeEditor, null);
    }

    @Override
    public void setFocus() {
        myPages.setFocus();
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part instanceof IEditorPart)
            showGraphFor0((IEditorPart) part, false);
    }

    public static void updateViews() {
        synchronized (views) {
            for (final CCSGraphView view: views)
                view.update();
        }
    }

    public synchronized void update() {
        if (currentView instanceof GrappaFrame) {
            ((GrappaFrame)currentView).updateGraph();
        }
    }

    public static void showGraphFor(IEditorPart activeEditor, boolean updateGraph) {
        synchronized (views) {
            for (final CCSGraphView view: views)
                view.showGraphFor0(activeEditor, updateGraph);
        }
    }

    private synchronized void showGraphFor0(IEditorPart activeEditor, boolean updateGraph) {
        if (activeEditor instanceof CCSEditor) {
            final CCSEditor editor = (CCSEditor) activeEditor;
            CCSFrame control = frames.get(editor);
            if (control == null)
                frames.put(editor, control = new CCSFrame(myPages, editor));

            myPages.showPage(currentView = control);
            if (updateGraph)
                control.showGraph(true);
        } else {
            myPages.showPage(currentView = defaultComp);
        }
    }

}
