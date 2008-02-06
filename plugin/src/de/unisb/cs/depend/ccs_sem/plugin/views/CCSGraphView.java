package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;


public class CCSGraphView extends ViewPart implements ISelectionListener {

    private static List<CCSGraphView> views = new ArrayList<CCSGraphView>(1);


    private PageBook myPages;


    private Text defaultText;


    private Control currentView;


    private final Map<CCSEditor, GrappaFrame> frames = new HashMap<CCSEditor, GrappaFrame>();

    public CCSGraphView() {
        views.add(this);
    }

    @Override
    public void createPartControl(Composite parent) {

        myPages = new PageBook(parent, SWT.None);

        defaultText = new Text(myPages, SWT.None);
        defaultText.setText("No CCS file opened...");
        defaultText.setEditable(false);

        myPages.showPage(currentView = defaultText);

        getSite().getPage().addSelectionListener(this);
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
        for (final CCSGraphView view: views)
            view.update();
    }

    public void update() {
        if (currentView instanceof GrappaFrame) {
            ((GrappaFrame)currentView).updateGraph();
        }
    }

    public static void showGraphFor(IEditorPart activeEditor, boolean updateGraph) {
        for (final CCSGraphView view: views)
            view.showGraphFor0(activeEditor, updateGraph);
    }

    private void showGraphFor0(IEditorPart activeEditor, boolean updateGraph) {
        if (activeEditor instanceof CCSEditor) {
            final CCSEditor editor = (CCSEditor) activeEditor;
            GrappaFrame gFrame = frames.get(editor);
            if (gFrame == null)
                frames.put(editor, gFrame = new GrappaFrame(myPages, SWT.None, editor));

            myPages.showPage(currentView = gFrame);
            if (updateGraph)
                gFrame.updateGraph();
        } else {
            myPages.showPage(currentView = defaultText);
        }
    }

}
