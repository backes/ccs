package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import att.grappa.Graph;
import de.unisb.cs.depend.ccs_sem.exporters.AiSeeGraphExporter;
import de.unisb.cs.depend.ccs_sem.exporters.CCSExporter;
import de.unisb.cs.depend.ccs_sem.exporters.ETMCCExporter;
import de.unisb.cs.depend.ccs_sem.exporters.GraphVizExporter;
import de.unisb.cs.depend.ccs_sem.plugin.actions.Evaluate;
import de.unisb.cs.depend.ccs_sem.plugin.actions.ExportGraph;
import de.unisb.cs.depend.ccs_sem.plugin.actions.ExportProgram;
import de.unisb.cs.depend.ccs_sem.plugin.actions.StepByStepTraverse;
import de.unisb.cs.depend.ccs_sem.plugin.dotExporters.GifDotExporter;
import de.unisb.cs.depend.ccs_sem.plugin.dotExporters.PNGDotExporter;
import de.unisb.cs.depend.ccs_sem.plugin.dotExporters.PostscriptDotExporter;
import de.unisb.cs.depend.ccs_sem.plugin.dotExporters.SVGDotExporter;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob;
import de.unisb.cs.depend.ccs_sem.plugin.views.components.CCSFrame;


public class CCSGraphView extends ViewPart implements ISelectionListener, IPartListener {

    private PageBook myPages;

    private Composite defaultComp;

    private Control currentPage;

    private final Map<CCSEditor, CCSFrame> frames = new HashMap<CCSEditor, CCSFrame>();

    private final Set<CCSEditor> closedCCSEditors = new HashSet<CCSEditor>();

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
        final IWorkbenchPage page = site == null ? null : site.getPage();
        if (page != null) {
            page.addSelectionListener(this);
            page.addPartListener(this);
        }

        final IEditorPart activeEditor = page.getActiveEditor();
        if (activeEditor != null)
            selectionChanged(activeEditor, null);

        final IActionBars bars = getViewSite().getActionBars();
        fillToolbar(bars.getToolBarManager());
        fillMenu(bars.getMenuManager());
    }

    private void fillToolbar(IToolBarManager toolBarManager) {
        toolBarManager.add(new Evaluate());
        toolBarManager.add(new StepByStepTraverse());
//        toolBarManager.add(new Help("CCSGraphView"));
    }

    private void fillMenu(IMenuManager menuManager) {
        final IMenuManager exportMenu = new MenuManager("Export");
        exportMenu.add(new ExportProgram("Export to dot file", new GraphVizExporter(),
            new String[] { "*.dot", "Dot File (*.dot)" }));
        exportMenu.add(new ExportProgram("Export to aiSee graph file", new AiSeeGraphExporter(),
            new String[] { "*.gdl", "AiSee Graph File (*.gdl)" }));
        exportMenu.add(new ExportProgram("Export to ETMCC format", new ETMCCExporter(),
            new String[] { "*.tra", "ETMCC File (*.tra)" }));
        exportMenu.add(new ExportProgram("Export to CCS file", new CCSExporter("PROC_"),
            new String[] { "*.ccs", "CCS File (*.ccs)" }));

        exportMenu.add(new Separator());

        exportMenu.add(new ExportGraph("Export to postscript (using dot)",
            new PostscriptDotExporter(),
            this,
            new String[] { "*.ps", "Postscript File (*.ps)" }));
        exportMenu.add(new ExportGraph("Export to SVG (using dot)",
            new SVGDotExporter(),
            this,
            new String[] { "*.svg", "SVG File (*.svg)" }));
        exportMenu.add(new ExportGraph("Export to PNG (using dot)",
            new PNGDotExporter(),
            this,
            new String[] { "*.png", "PNG File (*.png)" }));
        exportMenu.add(new ExportGraph("Export to GIF (using dot)",
            new GifDotExporter(),
            this,
            new String[] { "*.gif", "GIF File (*.gif)" }));

        menuManager.add(exportMenu);
        
//        menuManager.add(new Help("index"));
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
            ((CCSFrame)currentPage).updateEvaluation(false);
        }
    }
    
    public synchronized EvaluationJob getUpdateJob() {
    	if (currentPage instanceof CCSFrame) {
            return ((CCSFrame)currentPage).getUpdateJob(false);
        }
    	return null;
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

            // and now, dispose all frames whose editor has been closed
            final Set<CCSEditor> toDispose = new HashSet<CCSEditor>(closedCCSEditors );
            toDispose.retainAll(frames.keySet());
            for (final CCSEditor closed: toDispose) {
                final CCSFrame frame = frames.get(closed);
                if (frame != null) {
                    frames.remove(closed);
                    closedCCSEditors.remove(closed);
                    frame.dispose();
                }
            }
        } else {
            myPages.showPage(currentPage = defaultComp);
        }
    }

    public void partActivated(IWorkbenchPart part) {
        // ignore
    }

    public void partBroughtToTop(IWorkbenchPart part) {
        // ignore
    }

    public void partClosed(IWorkbenchPart part) {
        if (part instanceof CCSEditor) {
            closedCCSEditors.add((CCSEditor)part);
        }
    }

    public void partDeactivated(IWorkbenchPart part) {
        // ignore
    }

    public void partOpened(IWorkbenchPart part) {
        // ignore
    }

    public boolean isMinimize() {
        final Control page = currentPage;
        if (page instanceof CCSFrame)
            return ((CCSFrame)page).isMinimize();
        return false;
    }

    public Graph getGraph() {
        final Control page = currentPage;
        if (page instanceof CCSFrame)
            return ((CCSFrame)page).getGraph();
        return null;
    }

    public GrappaFrame getGrappaFrame() {
        if (currentPage instanceof CCSFrame)
            return ((CCSFrame)currentPage).getGrappaFrame();
        return null;
    }
}
