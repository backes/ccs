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
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


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
    private Program ccsProgram = null;
    private long modStampOfCachedProgram = -1;
    private final ListenerList parsingListeners = new ListenerList();
    private ParseCCSProgramJob reparsingJob;
    private final Lock lock = new ReentrantLock(true);

    public CCSDocument() {
        super();
        addDocumentListener(this);
    }

    protected synchronized void parsedProgram(ParseStatus result) {
        if (result.getDocModCount() > modStampOfCachedProgram) {
            modStampOfCachedProgram = result.getDocModCount();
            ccsProgram = result.getParsedProgram();
            final Object[] listeners = parsingListeners.getListeners();
            for (final Object o: listeners) {
                ((IParsingListener)o).parsingDone(this, result);
            }
        }
    }

    public void addParsingListener(IParsingListener listener) {
        parsingListeners.add(listener);
    }

    public void documentAboutToBeChanged(DocumentEvent event) {
        if (reparsingJob != null) {
            if (!reparsingJob.shouldRunImmediately)
                reparsingJob.cancel();
        }
        lock();
    }

    public void documentChanged(DocumentEvent event) {
        unlock();
        reparsingJob = new ParseCCSProgramJob(this);
        reparsingJob.addJobChangeListener(jobDoneListener);
        reparsingJob.schedule(500);
    }

    public void reparseNow(boolean waitForFinish) throws InterruptedException {
        if (reparsingJob.getState() == Job.WAITING) {
            reparsingJob.shouldRunImmediately = true;
        } else {
            reparsingJob = new ParseCCSProgramJob(this);
            reparsingJob.addJobChangeListener(jobDoneListener);
        }
        reparsingJob.schedule();
        if (waitForFinish)
            reparsingJob.join();
    }

    public void lock() {
        lock .lock();
    }

    public void unlock() {
        lock.unlock();
    }

}
