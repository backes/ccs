package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;


public class CCSGraphView extends ViewPart implements ISelectionListener {

    private static List<CCSGraphView> views = new ArrayList<CCSGraphView>(1);


    private Composite myComp;


    private Text defaultText;


    private Control currentView;


    private final Map<CCSEditor, GrappaFrame> frames = new HashMap<CCSEditor, GrappaFrame>();

    public CCSGraphView() {
        views.add(this);
    }

    @Override
    public void createPartControl(Composite parent) {

        myComp = new Composite(parent, SWT.None);

        myComp.setLayout(new FillLayout(SWT.VERTICAL));

        defaultText = new Text(myComp, SWT.None);
        defaultText.setText("No Graph to display...");
        defaultText.setEditable(false);

        currentView = defaultText;

        getSite().getPage().addSelectionListener(this);

        selectionChanged(getSite().getPart(), null);
    }

    @Override
    public void setFocus() {
        myComp.setFocus();
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part instanceof IEditorPart)
            showGraphFor0((IEditorPart) part);
    }

    public static void updateViews() {
        for (final CCSGraphView view: views)
            view.update();
    }

    public void update() {
        if (currentView instanceof GrappaFrame) {
            ((GrappaFrame)currentView).update();
        }
    }

    public static void showGraphFor(IEditorPart activeEditor) {
        for (final CCSGraphView view: views)
            view.showGraphFor0(activeEditor);
    }

    private void showGraphFor0(IEditorPart activeEditor) {
        if (activeEditor instanceof CCSEditor) {
            final CCSEditor editor = (CCSEditor) activeEditor;
            GrappaFrame gFrame = frames.get(editor);
            if (gFrame == null)
                frames.put(editor, gFrame = new GrappaFrame(myComp, SWT.None, editor));
            currentView.setVisible(false);
            gFrame.setVisible(true);
            currentView = gFrame;
        } else {
            currentView.setVisible(false);
            currentView = defaultText;
            currentView.setVisible(true);
        }
    }

}
