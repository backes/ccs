package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.evalutators.Evaluator;
import de.unisb.cs.depend.ccs_sem.evalutators.SequentialEvaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.plugin.views.CCSContentOutlinePage;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;

public class CCSEditor extends TextEditor {

    private final ColorManager colorManager;
    private CCSContentOutlinePage fOutlinePage;
    private Program ccsProgram = null;
    private final Evaluator evaluator = new SequentialEvaluator();

    public CCSEditor() {
        super();
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new CCSConfiguration(colorManager));
        setDocumentProvider(new CCSDocumentProvider());
    }

    @Override
    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class required) {
        if (IContentOutlinePage.class.equals(required)) {
            if (fOutlinePage == null) {
                fOutlinePage = new CCSContentOutlinePage(getDocumentProvider(), this);
                if (getEditorInput() != null)
                    fOutlinePage.setInput(getEditorInput());
            }
            return fOutlinePage;
        }
        return super.getAdapter(required);
    }

    public IDocument getDocument() {
        return getSourceViewer().getDocument();
    }

    public String getText() {
        return getDocument().get();
    }

    public Program getCCSProgram(boolean evaluate) throws ParseException, LexException {
        // TODO watch for changes in the source code
        ccsProgram = null;


        if (ccsProgram == null) {
            ccsProgram = new CCSParser().parse(getText());
        }

        if (evaluate) {
            synchronized (evaluator ) {
                ccsProgram.evaluate(evaluator);
            }
        }

        return ccsProgram;
    }

}
