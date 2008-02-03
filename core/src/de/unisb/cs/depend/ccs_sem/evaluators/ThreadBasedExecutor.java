package de.unisb.cs.depend.ccs_sem.evaluators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class ThreadBasedExecutor extends AbstractExecutorService {

    private final int poolSize;
    private final ThreadFactory threadFactory;
    protected volatile boolean isShutdown = false;
    protected final Map<Thread, Queue<Runnable>> threadJobs =
        new ConcurrentHashMap<Thread, Queue<Runnable>>();
    protected volatile boolean forcedStop = false;

    // object for synchronization
    protected Object newJobs = new Object();

    public ThreadBasedExecutor(int poolSize,
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
        if (!isShutdown)
            return false;

        return threadJobs == null;
    }

    public void shutdown() {
        isShutdown = true;
        synchronized (newJobs) {
            newJobs.notifyAll();
        }
    }

    public List<Runnable> shutdownNow() {
        forcedStop = true;
        isShutdown = true;
        for (final Thread thread: threadJobs.keySet()) {
            thread.interrupt();
        }

        for (final Thread thread: threadJobs.keySet()) {
            while (true) {
                try {
                    thread.join();
                } catch (final InterruptedException e) {
                    // ignore
                }
            }
        }
        final List<Runnable> list = new ArrayList<Runnable>();
        for (final Queue<Runnable> q: threadJobs.values())
            list.addAll(q);

        return list;
    }

    public void execute(Runnable command) {
        if (isShutdown)
            throw new RejectedExecutionException("Already shutdown.");

        final Thread thread = Thread.currentThread();
        Queue<Runnable> jobs = threadJobs.get(thread);
        if (jobs == null) {
            while (threadJobs.size() == 0)
                Thread.yield();
            // add to the first queue
            jobs = threadJobs.values().iterator().next();
        }
        if (jobs == null)
            throw new RejectedExecutionException("No running thread found ?!?");
        jobs.add(command);
        synchronized (newJobs) {
            newJobs.notify();
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
            myJobs = threadJobs.get(myThread);
            if (myJobs == null)
                threadJobs.put(myThread, myJobs = new ConcurrentLinkedQueue<Runnable>());
            while (!forcedStop) {
                Runnable nextJob = getNextJob();
                if (nextJob == null) {
                    if (isShutdown) {
                        // look a last time for a new job
                        nextJob = getNextJob();
                        if (nextJob == null)
                            break;
                    }
                }
                if (nextJob != null)
                    nextJob.run();
            }
        }

        private Runnable getNextJob() {
            Runnable nextJob = null;
            nextJob = myJobs.poll();
            if (nextJob != null)
                return nextJob;

            Queue<Runnable> preferredQueue = null;
            int maxSize = 0;
            for (final Queue<Runnable> q: threadJobs.values()) {
                if (q.size() > maxSize) {
                    maxSize = q.size();
                    preferredQueue = q;
                }
            }

            if (preferredQueue != null) {
                nextJob = preferredQueue.poll();
            }

            if (nextJob == null && !isShutdown) {
                synchronized (newJobs) {
                    if (myJobs.isEmpty()) {
                        try {
                            newJobs.wait();
                        } catch (final InterruptedException e) {
                            // hm, then go on...
                        }
                    }
                }
            }

            return nextJob;
        }

    }
}
