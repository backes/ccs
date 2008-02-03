package de.unisb.cs.depend.ccs_sem.evaluators.executors;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * This class should demonstrate how an easy implemented thread pool works.
 *
 * @author Clemens Hammacher
 */
public class SimpleThreadPoolExecutor extends AbstractExecutorService {

    private final int poolSize;
    private final ThreadFactory threadFactory;
    protected boolean isShutdown = false;
    protected Stack<Runnable> jobs = new Stack<Runnable>();
    private List<Worker> workers;

    public SimpleThreadPoolExecutor(int poolSize,
            ThreadFactory myThreadFactory) {
        super();
        if (poolSize < 1)
            throw new IllegalArgumentException("Poolsize must be > 0");
        this.poolSize = poolSize;
        this.threadFactory = myThreadFactory;
        initialize();
    }

    private void initialize() {
        for (int i = 0; i < poolSize; ++i) {
            final Thread newThread = threadFactory.newThread(new Worker());
            newThread.start();
        }
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public boolean isTerminated() {
        return jobs.isEmpty() && isShutdown;
    }

    public void shutdown() {
        isShutdown = true;
        synchronized (jobs) {
            jobs.notifyAll();
        }
    }

    public List<Runnable> shutdownNow() {
        isShutdown = true;
        for (final Worker worker: workers) {
            worker.shutdownNow();
        }

        final List<Runnable> list = new ArrayList<Runnable>();
        list.addAll(jobs);
        jobs.clear();

        return list;
    }

    public void execute(Runnable job) {
        if (isShutdown)
            throw new RejectedExecutionException("Already shutdown.");

        jobs.push(job);
        synchronized (jobs) {
            jobs.notify();
        }
    }

    private class Worker implements Runnable {

        private Thread myThread;
        private boolean forcedStop = false;

        public Worker() {
            // nothing
        }

        public void run() {
            myThread = Thread.currentThread();
            while (!forcedStop) {
                final Runnable nextJob = getNextJob();
                if (nextJob != null)
                    nextJob.run();
                else if (isShutdown)
                    break;
            }
        }

        private Runnable getNextJob() {
            try {
                return jobs.pop();
            } catch (EmptyStackException e) {
                // then wait for an incoming job...
            }
            
            synchronized (jobs) {
                if (jobs.isEmpty()) {
                    try {
                        jobs.wait();
                        return jobs.pop();
                    } catch (InterruptedException e) {
                        return null;
                    } catch (EmptyStackException e) {
                        return null;
                    }
                } else {
                    return jobs.pop();
                }
            }
        }

        public void shutdownNow() {
            forcedStop = true;
            myThread.interrupt();
            while (true) {
                try {
                    myThread.join();
                } catch (final InterruptedException e) {
                    // ignore
                }
            }
        }

    }
}
