package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class CCSSourceViewerConfiguration extends SourceViewerConfiguration {
	private static final String[] CONTENT_TYPES = { IDocument.DEFAULT_CONTENT_TYPE };

    private CCSDoubleClickStrategy doubleClickStrategy;

	private final ColorManager colorManager;

	public CCSSourceViewerConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}

	@Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
	    return CONTENT_TYPES;
	}

	@Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new CCSDoubleClickStrategy();
		return doubleClickStrategy;
	}

	@Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
	    return new CCSPresentationReconciler(colorManager);
	}

}