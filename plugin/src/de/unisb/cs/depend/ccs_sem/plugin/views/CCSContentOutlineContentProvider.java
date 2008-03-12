package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.List;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;

import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult.ReadDeclaration;


public class CCSContentOutlineContentProvider implements ITreeContentProvider,
        ISelectionChangedListener {

    private final ISourceViewer sourceViewer;
    private Object input;

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
            if (!(input instanceof ParsingResult))
                return new Object[] { "-- ERROR --" };

            final List<ReadDeclaration> list = ((ParsingResult)input).declarations;
            if (list.size() == 0)
                return new String[] {"(no declarations)"};
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
        input = newInput;
    }

    public void selectionChanged(SelectionChangedEvent event) {
        final ISelection sel = event.getSelection();
        if (sel instanceof TreeSelection) {
            final TreeSelection treeSel = (TreeSelection) sel;
            final Object first = treeSel.getFirstElement();
            if (first instanceof ReadDeclaration && sourceViewer != null) {
                final ReadDeclaration decl = (ReadDeclaration) first;
                final int start = decl.getPositionStart();
                final int end = decl.getPositionEnd();
                sourceViewer.setSelectedRange(start, end-start+1);
            } else if (first == mainExpressionItem
                    && input instanceof ParsingResult && sourceViewer != null) {
                final ParsingResult res = (ParsingResult) input;
                final int start = res.tokens.get(res.mainExpressionTokenIndexStart).getStartPosition();
                final int end = res.tokens.get(res.mainExpressionTokenIndexEnd).getEndPosition();
                sourceViewer.setSelectedRange(start, end-start+1);
            }
        }
    }

}
