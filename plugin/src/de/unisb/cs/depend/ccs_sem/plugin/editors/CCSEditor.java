package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.Observer;
import java.util.Set;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.plugin.views.CCSContentOutlinePage;
import de.unisb.cs.depend.ccs_sem.utils.ConcurrentHashSet;

public class CCSEditor extends TextEditor implements IDocumentListener {

    private static final long REPARSE_INTERVAL = 500;
    private final ColorManager colorManager;
    private CCSContentOutlinePage fOutlinePage;

    private final ParseCCSProgramJob parseJob;

    private final Set<Observer> reparsingListeners =
        new ConcurrentHashSet<Observer>();

    public CCSEditor() {
        super();
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new CCSConfiguration(colorManager));
        setDocumentProvider(new CCSDocumentProvider());
        parseJob = new ParseCCSProgramJob(this);
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
            if (fOutlinePage == null)
                fOutlinePage = new CCSContentOutlinePage(this);
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

    public void documentAboutToBeChanged(DocumentEvent event) {
        // not very interesting
    }

    public void documentChanged(DocumentEvent event) {
        parseJob.schedule(REPARSE_INTERVAL);
    }

    public void registerReparsingListener(Observer observer) {
        reparsingListeners.add(observer);
    }

    public void reparsed(ParseStatus status) {

        // TODO Auto-generated method stub

        for (final Observer obs: reparsingListeners)
            obs.update(null, status);

    }

}
