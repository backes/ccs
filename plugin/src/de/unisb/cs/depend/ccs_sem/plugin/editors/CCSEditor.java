package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class CCSEditor extends TextEditor {

	private ColorManager colorManager;

	public CCSEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
