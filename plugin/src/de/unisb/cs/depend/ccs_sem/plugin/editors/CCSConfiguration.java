package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class CCSConfiguration extends SourceViewerConfiguration {
	private CCSDoubleClickStrategy doubleClickStrategy;
	private CCSTagScanner tagScanner;
	private CCSScanner scanner;
	private final ColorManager colorManager;

	public CCSConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}

	@Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			CCSPartitionScanner.XML_COMMENT,
			CCSPartitionScanner.XML_TAG };
	}
	@Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new CCSDoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected CCSScanner getXMLScanner() {
		if (scanner == null) {
			scanner = new CCSScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IXMLColorConstants.DEFAULT))));
		}
		return scanner;
	}
	protected CCSTagScanner getXMLTagScanner() {
		if (tagScanner == null) {
			tagScanner = new CCSTagScanner(colorManager);
			tagScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IXMLColorConstants.TAG))));
		}
		return tagScanner;
	}

	@Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		final PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr =
			new DefaultDamagerRepairer(getXMLTagScanner());
		reconciler.setDamager(dr, CCSPartitionScanner.XML_TAG);
		reconciler.setRepairer(dr, CCSPartitionScanner.XML_TAG);

		dr = new DefaultDamagerRepairer(getXMLScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		final NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(IXMLColorConstants.XML_COMMENT)));
		reconciler.setDamager(ndr, CCSPartitionScanner.XML_COMMENT);
		reconciler.setRepairer(ndr, CCSPartitionScanner.XML_COMMENT);

		return reconciler;
	}

}