package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.ResourceBundle;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;


public class CCSEditorActionContributor extends BasicTextEditorActionContributor {

    private final ResourceBundle myBundle = ResourceBundle.getBundle("CCSEditorActions");
    private final RetargetTextEditorAction showGraphAction;

    public CCSEditorActionContributor() {
        super();
        showGraphAction = new RetargetTextEditorAction(myBundle , "CCS.showGraph");
        // TODO Auto-generated constructor stub
    }
    // TODO

    @Override
    public void setActiveEditor(IEditorPart part) {
        super.setActiveEditor(part);
    }
}
