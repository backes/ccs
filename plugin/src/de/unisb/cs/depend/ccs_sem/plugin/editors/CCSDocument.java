package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;


public class CCSDocument extends Document implements IDocumentListener {

    private final class JobDoneListener extends JobChangeAdapter {

        protected JobDoneListener() {
            // nothing to do
        }

        @Override
        public void done(IJobChangeEvent event) {
            final IStatus result = event.getResult();
            if (result instanceof ParseStatus) {
                final ParseStatus parseResult = (ParseStatus)result;
                parsedProgram(parseResult);
            }
        }
    }

    private final JobDoneListener jobDoneListener = new JobDoneListener();
    private long modStampOfCachedResult = -1;
    private final ListenerList parsingListeners = new ListenerList();
    private ParseCCSProgramJob reparsingJob;
    private final Lock lock = new ReentrantLock(true);
    private ParseStatus lastResult;

    public CCSDocument() {
        super();
        addDocumentListener(this);
    }

    protected synchronized void parsedProgram(ParseStatus result) {
        if (result.getDocModCount() >= modStampOfCachedResult) {
            modStampOfCachedResult = result.getDocModCount();
            lastResult = result;
            final Object[] listeners = parsingListeners.getListeners();
            for (final Object o: listeners) {
                ((IParsingListener)o).parsingDone(this, result);
            }
        }
    }

    public void addParsingListener(IParsingListener listener) {
        parsingListeners.add(listener);
    }

    public void removeParsingListener(
            IParsingListener listener) {
        parsingListeners.remove(listener);
    }

    public void documentAboutToBeChanged(DocumentEvent event) {
        if (reparsingJob != null && !reparsingJob.shouldRunImmediately)
                reparsingJob.cancel();
        lock(); // unlocked in documentChanged
    }

    public synchronized void documentChanged(DocumentEvent event) {
        unlock(); // locked in documentAboutToBeChanged
        reparsingJob = new ParseCCSProgramJob(this);
        reparsingJob.addJobChangeListener(jobDoneListener);
        reparsingJob.schedule(500);
    }

    public void reparseNow() {
        reparseNow(false);
    }

    public synchronized void reparseNow(boolean syncExec) {
        if (reparsingJob == null || reparsingJob.getState() != Job.WAITING) {
            reparsingJob = new ParseCCSProgramJob(this);
            reparsingJob.addJobChangeListener(jobDoneListener);
        }
        reparsingJob.shouldRunImmediately = true;
        if (syncExec)
            reparsingJob.syncExec = true;
        reparsingJob.schedule();
    }

    public void waitForReparsingDone() throws InterruptedException {
        if (reparsingJob != null)
            reparsingJob.shouldRunImmediately = true;
        reparsingJob.join();
    }

    /**
     * Reparse the Document if necessary (i.e. if the cached result
     * is outdated), otherwise trigger an immediate reparse.
     *
     * @return the cached result, or <code>null</code> if a reparse was triggered.
     */
    public synchronized ParseStatus reparseIfNecessary() {
        if (modStampOfCachedResult == getModificationStamp())
            return lastResult;

        reparseNow();

        return null;
    }

    public void lock() {
        lock .lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public ParseStatus getLastParseResult() {
        return lastResult;
    }

}
