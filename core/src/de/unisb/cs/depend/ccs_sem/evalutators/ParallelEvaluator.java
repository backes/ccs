package de.unisb.cs.depend.ccs_sem.evalutators;

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

    public ParallelEvaluator(int numThreads) {
        this.numThreads = numThreads;
    }

    public ParallelEvaluator() {
        this.numThreads = null;
    }

    public void evaluate(Expression expr) {
        evaluate(expr, null);
    }

    public void evaluate(Expression expr, EvaluationMonitor monitor) {
        if (expr.isEvaluated())
            return;

        evaluate0(expr, false, monitor);
    }

    public void evaluateAll(Expression expr) {
        evaluateAll(expr, null);
    }

    public void evaluateAll(Expression expr, EvaluationMonitor monitor) {
        evaluate0(expr, true, monitor);
    }

    // synchronized s.t. it can only be called once at a time
    private synchronized void evaluate0(Expression expr, boolean evaluateSuccessors, EvaluationMonitor monitor) {
        initialize(evaluateSuccessors, monitor);

        try {
            synchronized (readyLock) {
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
    }

    private void initialize(boolean evaluateSuccessors, EvaluationMonitor monitor2) {
        assert executor == null;
        assert currentlyEvaluating == null;
        assert monitor == null;

        final int threadsToInstantiate = numThreads == null
            ? Runtime.getRuntime().availableProcessors() + 1
            : numThreads;

        executor = Executors.newFixedThreadPool(threadsToInstantiate);

        currentlyEvaluating = new ConcurrentHashMap<Expression, EvaluatorJob>();

        if (evaluateSuccessors)
            evaluatedSuccessors = new ConcurrentSet<Expression>(threadsToInstantiate);

        monitor = monitor2;
    }

    private void shutdown() {
        executor.shutdown();
        executor = null;

        assert currentlyEvaluating.isEmpty();
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
            if (!childrenEvaluated) {
                Barrier barrier = null;

                for (final Expression child: expr.getChildren()) {
                    synchronized (child) {
                        if (!child.isEvaluated()) {
                            if (barrier == null)
                                barrier = new Barrier(executor, this);

                            EvaluatorJob childEvaluator =
                                    currentlyEvaluating.get(child);
                            if (childEvaluator == null)
                                childEvaluator = new EvaluatorJob(child, false);
                            childEvaluator.addWaiter(barrier);
                            barrier.inc();
                        }
                    }
                }

                childrenEvaluated = true;

                if (barrier != null)
                    // then the barrier will call us again if all children are evaluated
                    return;
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
                    if (evaluatedSuccessors.add(succ))
                        if (currentlyEvaluating.get(succ) == null)
                            new EvaluatorJob(succ, true);
                }
            }

            // now the work is ready
            final Object removed = currentlyEvaluating.remove(expr);
            assert removed != null;

            if (currentlyEvaluating.isEmpty()) {
                synchronized (readyLock) {
                    if (monitor != null)
                        monitor.ready();
                    readyLock.notify();
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

        public Barrier(ExecutorService executor, Runnable jobToRun) {
            this.executor = executor;
            this.job = jobToRun;
        }

        public void inc() {
            ++waitNr;
        }

        public synchronized void inform() {
            assert waitNr > 0;
            --waitNr;
            if (waitNr == 0) {
                // is this synchronized?
                executor.execute(job);
            }
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

}
