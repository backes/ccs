package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;

import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult.ReadDeclaration;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;


public class CCSContentOutlineContentProvider implements ITreeContentProvider,
        ISelectionChangedListener {

    private final ISourceViewer sourceViewer;
    private volatile ParseStatus input;

    private final String mainExpressionItem = "Main Expression";
    private final String declarationItem = "Declarations";
    private final String[] rootItems = {
        declarationItem,
        mainExpressionItem
    };

    public CCSContentOutlineContentProvider(ISourceViewer sourceViewer) {
        this.sourceViewer = sourceViewer;
    }

    public void dispose() {
        // nothing to do
    }

    public Object[] getChildren(Object parentElement) {
        if (parentElement == declarationItem) {
            if (input == null)
                return new Object[] { "-- ERROR --" };

            ParsingResult parsingResult = input.getParsingResult();
			final List<ReadDeclaration> list = parsingResult == null
				? null : parsingResult.declarations;
            if (list == null || list.size() == 0)
                return new String[] { "(no declarations)" };
            return list.toArray();
        }

        // else
        return new Object[0];
    }

    public Object getParent(Object element) {
        if (element instanceof ReadDeclaration)
            return declarationItem;
        if (element == mainExpressionItem || element == declarationItem)
            return input;
        return null;
    }

    public boolean hasChildren(Object element) {
        return element == declarationItem;
    }

    public Object[] getElements(Object inputElement) {
        return rootItems;
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        input = newInput instanceof ParseStatus ? (ParseStatus)newInput : null;
    }

    public void selectionChanged(SelectionChangedEvent event) {
        final ISelection sel = event.getSelection();
        if (sel instanceof TreeSelection) {
            final TreeSelection treeSel = (TreeSelection) sel;
            final Object first = treeSel.getFirstElement();
            ensureUpToDate();
            if (first instanceof ReadDeclaration && sourceViewer != null) {
                final ReadDeclaration decl = (ReadDeclaration) first;
                final int start = decl.getPositionStart();
                final int end = decl.getPositionEnd();
                sourceViewer.setSelectedRange(start, end-start+1);
            } else if (first == mainExpressionItem
                    && sourceViewer != null) {
            	ParsingResult result = input == null ? null : input.getParsingResult();
                final int start = result.tokens == null || result.mainExpressionTokenIndexStart < 0
                	|| result.mainExpressionTokenIndexStart >= result.tokens.size()
                	? 0
        			: result.tokens.get(result.mainExpressionTokenIndexStart).getStartPosition();
                final int end = result.tokens == null || result.mainExpressionTokenIndexEnd < 0
                	|| result.mainExpressionTokenIndexEnd >= result.tokens.size()
                	? 0
        			: result.tokens.get(result.mainExpressionTokenIndexEnd).getEndPosition();
                sourceViewer.setSelectedRange(start, end-start+1);
            }
        }
    }

	private void ensureUpToDate() {
		boolean upToDate = true;
		IDocument doc = sourceViewer == null ? null : sourceViewer.getDocument();
		if (input == null) {
			upToDate = false;
		} else if (doc instanceof CCSDocument) {
				upToDate &= ((CCSDocument)doc).getModificationStamp() == input.getDocModCount();
		}
		if (!upToDate && doc instanceof CCSDocument) {
			try {
				((CCSDocument)doc).reparseNow(true);
			} catch (InterruptedException e) {
				// reset interruption flag and ignore...
				Thread.currentThread().interrupt();
			}
		}
	}

}
