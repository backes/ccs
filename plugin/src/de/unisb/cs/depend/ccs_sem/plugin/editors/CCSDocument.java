package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.utils.ConcurrentHashSet;


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
                parsedProgram(parseResult.getParsedProgram(),
                    parseResult.getDocModCount());
            }
        }
    }

    private final JobDoneListener jobDoneListener = new JobDoneListener();
    private Program ccsProgram = null;
    private long modStampOfCachedProgram = -1;
    private final Set<IParsingListener> parsingListeners = new ConcurrentHashSet<IParsingListener>();
    private ParseCCSProgramJob reparsingJob;
    private final Lock lock = new ReentrantLock(true);

    public CCSDocument() {
        super();
        addDocumentListener(this);
    }

    protected synchronized void parsedProgram(Program parsedProgram, long docModCount) {
        if (docModCount > modStampOfCachedProgram) {
            modStampOfCachedProgram = docModCount;
            ccsProgram = parsedProgram;
            for (final IParsingListener parsingListener: parsingListeners) {
                parsingListener.parsingDone(this, parsedProgram);
            }
        }
    }

    public void addParsingListener(IParsingListener listener) {
        parsingListeners .add(listener);
    }

    public void documentAboutToBeChanged(DocumentEvent event) {
        if (reparsingJob != null) {
            if (!reparsingJob.shouldRunImmediately)
                reparsingJob.cancel();
        }
    }

    public void documentChanged(DocumentEvent event) {
        reparsingJob = new ParseCCSProgramJob(this);
        reparsingJob.addJobChangeListener(jobDoneListener);
        reparsingJob.schedule(500);
    }

    public void reparseNow() {
        if (reparsingJob.getState() == Job.WAITING) {
            reparsingJob.shouldRunImmediately = true;
            reparsingJob.schedule();
        } else {
            reparsingJob = new ParseCCSProgramJob(this);
            reparsingJob.addJobChangeListener(jobDoneListener);
            reparsingJob.schedule();
        }
    }

    public void lock() {
        lock .lock();
    }

    public void unlock() {
        lock.unlock();
    }

}
