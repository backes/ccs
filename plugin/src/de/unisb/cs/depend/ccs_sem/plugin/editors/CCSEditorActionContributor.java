package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;

import de.unisb.cs.depend.ccs_sem.exporters.AiSeeGraphExporter;
import de.unisb.cs.depend.ccs_sem.exporters.CCSExporter;
import de.unisb.cs.depend.ccs_sem.exporters.ETMCCExporter;
import de.unisb.cs.depend.ccs_sem.exporters.GraphVizExporter;
import de.unisb.cs.depend.ccs_sem.plugin.actions.ExportGraph;
import de.unisb.cs.depend.ccs_sem.plugin.actions.ExportProgram;
import de.unisb.cs.depend.ccs_sem.plugin.actions.ShowGraph;
import de.unisb.cs.depend.ccs_sem.plugin.actions.StepByStepTraverse;
import de.unisb.cs.depend.ccs_sem.plugin.dotExporters.GifDotExporter;
import de.unisb.cs.depend.ccs_sem.plugin.dotExporters.PNGDotExporter;
import de.unisb.cs.depend.ccs_sem.plugin.dotExporters.PostscriptDotExporter;
import de.unisb.cs.depend.ccs_sem.plugin.dotExporters.SVGDotExporter;


public class CCSEditorActionContributor extends BasicTextEditorActionContributor {

    public CCSEditorActionContributor() {
        super();
    }

    @Override
    public void contributeToMenu(IMenuManager menu) {
        super.contributeToMenu(menu);

        final IMenuManager ccsMenu = new MenuManager("CCS");
        ccsMenu.add(new ShowGraph());
        ccsMenu.add(new StepByStepTraverse());

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
            null,
            new String[] { "*.ps", "Postscript File (*.ps)" }));
        exportMenu.add(new ExportGraph("Export to SVG (using dot)",
            new SVGDotExporter(),
            null,
            new String[] { "*.svg", "SVG File (*.svg)" }));
        exportMenu.add(new ExportGraph("Export to PNG (using dot)",
            new PNGDotExporter(),
            null,
            new String[] { "*.png", "PNG File (*.png)" }));
        exportMenu.add(new ExportGraph("Export to GIF (using dot)",
            new GifDotExporter(),
            null,
            new String[] { "*.gif", "GIF File (*.gif)" }));

        ccsMenu.add(exportMenu);

        menu.add(ccsMenu);

    }

    @Override
    public void contributeToToolBar(IToolBarManager toolBarManager) {
        super.contributeToToolBar(toolBarManager);
        toolBarManager.add(new ShowGraph());
        toolBarManager.add(new StepByStepTraverse());
    }

}
