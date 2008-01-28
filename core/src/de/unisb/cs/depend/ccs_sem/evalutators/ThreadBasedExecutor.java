package de.unisb.cs.depend.ccs_sem.evalutators;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class ThreadBasedExecutor extends AbstractExecutorService {

    private final int poolSize;
    private final ThreadFactory threadFactory;
    private boolean isShutdown = false;
    protected Map<Thread, Queue<Runnable>> threadJobs = null;

    public ThreadBasedExecutor(int poolSize,
            ThreadFactory myThreadFactory) {
        super();
        this.poolSize = poolSize;
        this.threadFactory = myThreadFactory;
        initialize();
    }

    private void initialize() {
        threadJobs = new HashMap<Thread, Queue<Runnable>>();
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
        if (!isShutdown)
            return false;

        return threadJobs == null;
    }

    public void shutdown() {
        isShutdown = true;

        // TODO Auto-generated method stub

    }

    public List<Runnable> shutdownNow() {
        // TODO Auto-generated method stub
        return null;
    }

    public void execute(Runnable command) {
        if (isShutdown)
            throw new RejectedExecutionException("Already shutdown.");

        final Thread thread = Thread.currentThread();
        Queue<Runnable> jobs = threadJobs.get(thread);
        if (jobs == null && threadJobs != null) {
            synchronized (threadJobs) {
                if (threadJobs.size() > 0) {
                    // add to the first queue
                    jobs = threadJobs.values().iterator().next();
                }
            }
        }
        if (jobs == null)
            throw new RejectedExecutionException("No running thread found ?!?");
        synchronized (jobs) {
            jobs.add(command);
        }
    }

    private class Worker implements Runnable {

        private Queue<Runnable> myJobs;
        private Thread myThread;

        public Worker() {
            // nothing
        }

        public void run() {
            myThread = Thread.currentThread();
            synchronized (threadJobs) {
                myJobs = threadJobs.get(myThread);
                if (myJobs == null)
                    threadJobs.put(myThread, myJobs = new ArrayDeque<Runnable>());
            }
            while (true) {
                final Runnable nextJob = getNextJob();
                nextJob.run();
            }
        }

        private Runnable getNextJob() {
            Runnable nextJob = null;
            while (nextJob == null) {
                synchronized (myJobs) {
                    nextJob = myJobs.poll();
                }
                if (nextJob != null)
                    return nextJob;

                Queue<Runnable> preferredQueue = null;
                int maxSize = 0;
                for (final Queue<Runnable> q: threadJobs.values()) {
                    synchronized (q) {
                        if (q.size() > maxSize) {
                            maxSize = q.size();
                            preferredQueue = q;
                        }
                    }
                }

                if (preferredQueue != null) {
                    synchronized (preferredQueue) {
                        nextJob = preferredQueue.poll();
                    }
                }

                if (nextJob == null)
                    Thread.yield();
            }

            return nextJob;
        }

    }
}
