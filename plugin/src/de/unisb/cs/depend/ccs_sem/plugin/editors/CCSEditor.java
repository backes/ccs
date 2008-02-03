package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.evaluators.Evaluator;
import de.unisb.cs.depend.ccs_sem.evaluators.SequentialEvaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.plugin.views.CCSContentOutlinePage;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;

public class CCSEditor extends TextEditor implements IDocumentListener {

    private final ColorManager colorManager;
    private CCSContentOutlinePage fOutlinePage;
    private Program ccsProgram = null;
    private final Evaluator evaluator = new SequentialEvaluator();
    private boolean listenerAdded = false;
    private boolean lastMinimizeGraph = false;
    private boolean lastEvaluate = true;

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

    public Program getCCSProgram(boolean evaluate, boolean minimizeGraph) throws ParseException, LexException {
        if (!listenerAdded) {
            getDocument().addDocumentListener(this);
            listenerAdded = true;
        }

        if (ccsProgram == null || minimizeGraph != lastMinimizeGraph || evaluate != lastEvaluate) {
            ccsProgram = new CCSParser().parse(getText());
            lastMinimizeGraph = minimizeGraph;
            lastEvaluate  = evaluate;
            if (!ccsProgram.isGuarded())
                throw new ParseException("Your recursive definitions are not guarded.");
            if (!ccsProgram.isRegular())
                throw new ParseException("Your recursive definitions are not regular.");
        }

        if (evaluate) {
            synchronized (evaluator ) {
                ccsProgram.evaluate(evaluator);
            }
            if (minimizeGraph)
                ccsProgram.minimizeTransitions();
        }

        return ccsProgram;
    }

    public void documentAboutToBeChanged(DocumentEvent event) {
        // not very interesting
    }

    public void documentChanged(DocumentEvent event) {
        ccsProgram = null;
    }

}
