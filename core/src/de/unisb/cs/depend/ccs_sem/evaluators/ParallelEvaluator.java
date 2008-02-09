package de.unisb.cs.depend.ccs_sem.evaluators;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class ParallelEvaluator implements Evaluator {

    private final Integer numThreads;

    protected ExecutorService executor = null;

    protected Map<Expression, EvaluatorJob> currentlyEvaluating = null;

    protected Set<Expression> evaluatedSuccessors = null;

    protected final Object readyLock = new Object();

    protected EvaluationMonitor monitor;

    public boolean errorOccured = false;

    public ParallelEvaluator(int numThreads) {
        this.numThreads = numThreads;
    }

    public ParallelEvaluator() {
        this.numThreads = null;
    }

    public boolean evaluate(Expression expr) {
        return evaluate(expr, null);
    }

    public boolean evaluate(Expression expr, EvaluationMonitor monitor) {
        if (expr.isEvaluated())
            return true;

        return evaluate0(expr, false, monitor);
    }

    public boolean evaluateAll(Expression expr, EvaluationMonitor monitor) {
        return evaluate0(expr, true, monitor);
    }

    // synchronized s.t. it can only be called once at a time
    private synchronized boolean evaluate0(Expression expr, boolean evaluateSuccessors, EvaluationMonitor monitor) {
        try {
            synchronized (readyLock) {
                initialize(evaluateSuccessors, monitor);

                // the EvaluatorJob executes itself automatically
                if (evaluateSuccessors)
                    evaluatedSuccessors.add(expr);
                new EvaluatorJob(expr, evaluateSuccessors);

                try {
                    readyLock.wait();
                } catch (final InterruptedException e) {
                    throw new InternalSystemException(
                            "Interrupted while waiting for parallel evaluation to finish.");
                }
            }
        } finally {
            shutdown();
        }

        return !errorOccured;
    }

    protected void initialize(boolean evaluateSuccessors, EvaluationMonitor monitor2) {
        assert executor == null;
        assert currentlyEvaluating == null;
        assert monitor == null;

        final int threadsToInstantiate = numThreads == null
            ? Runtime.getRuntime().availableProcessors() + 1
            : numThreads;

        final UncaughtExceptionHandler eh = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                errorOccured = true;
                synchronized (readyLock) {
                    System.err.print("Exception in thread \""
                        + t.getName() + "\" ");
                    e.printStackTrace(System.err);
                    if (monitor != null)
                        monitor.error(e.getClass().getName() + ": " + e.getMessage());
                    readyLock.notifyAll();
                }
            }
        };
        final ThreadFactory myThreadFactory = new MyThreadFactory(eh);
        executor = getExecutor(threadsToInstantiate, myThreadFactory);

        currentlyEvaluating = new ConcurrentHashMap<Expression, EvaluatorJob>();

        if (evaluateSuccessors)
            evaluatedSuccessors = new ConcurrentSet<Expression>(threadsToInstantiate);

        monitor = monitor2;

        errorOccured = false;
    }

    protected ExecutorService getExecutor(final int threadsToInstantiate,
            final ThreadFactory myThreadFactory) {
        return Executors.newFixedThreadPool(threadsToInstantiate, myThreadFactory);
    }

    private void shutdown() {
        if (errorOccured && executor != null)
            executor.shutdownNow();

        if (executor != null)
            executor.shutdown();
        executor = null;

        assert errorOccured || currentlyEvaluating == null || currentlyEvaluating.isEmpty();
        currentlyEvaluating = null;

        evaluatedSuccessors = null;
    }

    private class EvaluatorJob implements Runnable {

        private volatile List<Barrier> waiters = null;
        private final Expression expr;
        private final boolean evaluateSuccessors;
        private volatile boolean childrenEvaluated = false;

        public EvaluatorJob(Expression expr, boolean evaluateSuccessors) {
            this.expr = expr;
            this.evaluateSuccessors = evaluateSuccessors;
            currentlyEvaluating.put(expr, this);
            executor.execute(this);
        }

        // is externally synchronized (over the expression)
        private void addWaiter(Barrier waiter) {
            if (waiters == null)
                waiters = new ArrayList<Barrier>(2);
            waiters.add(waiter);
        }

        public void run() {
            if (!childrenEvaluated && !expr.isEvaluated()) {
                Barrier barrier = null;

                for (final Expression child: expr.getChildren()) {
                    synchronized (child) {
                        if (!child.isEvaluated()) {
                            if (barrier == null)
                                barrier = new Barrier(executor, this, 1);

                            EvaluatorJob childEvaluator =
                                    currentlyEvaluating.get(child);
                            if (childEvaluator == null)
                                childEvaluator = new EvaluatorJob(child, false);
                            barrier.inc();
                            childEvaluator.addWaiter(barrier);
                        }
                    }
                }

                childrenEvaluated = true;

                if (barrier != null) {
                    // inform barrier that all children have been added
                    barrier.inform();
                    // the barrier will call us again when all children are evaluated
                    return;
                }
            }

            expr.evaluate();

            synchronized (expr) {
                if (waiters != null)
                    for (final Barrier waiter: waiters)
                        waiter.inform();
            }

            if (evaluateSuccessors) {
                if (monitor != null) {
                    monitor.newState();
                    monitor.newTransitions(expr.getTransitions().size());
                }

                for (final Transition trans: expr.getTransitions()) {
                    final Expression succ = trans.getTarget();
                    if (evaluatedSuccessors.add(succ)) {
                        new EvaluatorJob(succ, true);
                    }
                }
            }

            // now the work is ready
            final Object removed = currentlyEvaluating.remove(expr);
            assert removed != null;

            // if everything is evaluated, inform the waiting thread(s)
            if (currentlyEvaluating.isEmpty()) {
                synchronized (readyLock) {
                    if (monitor != null)
                        monitor.ready();
                    readyLock.notifyAll();
                }
            }
        }
    }

    // static to improve performance, even if we have to copy the
    // ExecutorService reference
    private static class Barrier {

        private final Runnable job;
        private int waitNr = 0;
        private final ExecutorService executor;

        public Barrier(ExecutorService executor, Runnable jobToRun, int startNr) {
            this.executor = executor;
            this.job = jobToRun;
            this.waitNr = startNr;
        }

        public void inc() {
            ++waitNr;
        }

        public synchronized void inform() {
            assert waitNr > 0;
            --waitNr;
            if (waitNr == 0)
                executor.execute(job);
        }

    }

    public static class ConcurrentSet<E> extends AbstractSet<E> {

        private transient final ConcurrentMap<E, Object> map;

        // Dummy value to associate with an Object in the backing Map
        private static final Object PRESENT = new Object();

        public ConcurrentSet() {
            map = new ConcurrentHashMap<E, Object>();
        }

        public ConcurrentSet(int concurrencyLevel) {
            map = new ConcurrentHashMap<E, Object>(16, .75f, concurrencyLevel);
        }

        @Override
        public Iterator<E> iterator() {
            return map.keySet().iterator();
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return map.containsKey(o);
        }

        @Override
        public boolean add(E e) {
            return map.putIfAbsent(e, PRESENT) == null;
        }

        @Override
        public boolean remove(Object o) {
            return map.remove(o) == PRESENT;
        }
    }

    private static class MyThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;
        private final UncaughtExceptionHandler eh;

        public MyThreadFactory(UncaughtExceptionHandler eh) {
            this.eh = eh;
            group = Thread.currentThread().getThreadGroup();
            namePrefix = "parallelEvaluator-";
        }

        public Thread newThread(Runnable r) {
            final Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            t.setUncaughtExceptionHandler(eh);
            return t;
        }
    }

}
