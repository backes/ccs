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

    public ParallelEvaluator(int numThreads) {
        this.numThreads = numThreads;
    }

    public ParallelEvaluator() {
        this.numThreads = null;
    }

    public void evaluate(Expression expr) {
        if (expr.isEvaluated())
            return;

        evaluate0(expr, false);
    }

    public void evaluateAll(Expression expr) {
        evaluate0(expr, true);
    }

    private void evaluate0(Expression expr, boolean evaluateSuccessors) {
        initialize(evaluateSuccessors);

        synchronized (readyLock) {
            // the EvaluatorJob executes itself automatically
            new EvaluatorJob(expr, evaluateSuccessors);

            try {
                readyLock.wait();
            } catch (final InterruptedException e) {
                throw new InternalSystemException(
                        "Interrupted while waiting for parallel evaluation to finish.");
            }
        }

        shutdown();
    }

    private void initialize(boolean evaluateSuccessors) {
        assert executor == null;
        assert currentlyEvaluating == null;

        final int threadsToInstantiate = numThreads == null
            ? Runtime.getRuntime().availableProcessors() + 1
            : numThreads;

        executor = Executors.newFixedThreadPool(threadsToInstantiate);

        currentlyEvaluating = new ConcurrentHashMap<Expression, EvaluatorJob>();

        if (evaluateSuccessors)
            evaluatedSuccessors = new ConcurrentSet<Expression>(threadsToInstantiate);
    }

    private void shutdown() {
        executor.shutdown();
        executor = null;

        assert currentlyEvaluating.isEmpty();
        currentlyEvaluating = null;

        evaluatedSuccessors = null;
    }

    private static interface Informable {

        void inform();
    }

    private class EvaluatorJob implements Runnable {

        protected List<Informable> waiters = null;
        protected final Expression expr;
        protected final boolean evaluateSuccessors;

        public EvaluatorJob(Expression expr, boolean evaluateSuccessors) {
            this.expr = expr;
            this.evaluateSuccessors = evaluateSuccessors;
            currentlyEvaluating.put(expr, this);
            executor.execute(this);
        }

        private synchronized void addWaiter(Informable info) {
            if (waiters == null)
                waiters = new ArrayList<Informable>(1);
            waiters.add(info);
        }

        public void run() {
            Barrier barrier = null;

            final EvaluateSingleExpressionJob eval = new EvaluateSingleExpressionJob();

            for (final Expression child: expr.getChildren()) {
                synchronized (child) {
                    if (!child.isEvaluated()) {
                        if (barrier == null) {
                            barrier = new Barrier(eval);
                        }
                        EvaluatorJob childEvaluator =
                                currentlyEvaluating.get(child);
                        if (childEvaluator == null)
                            childEvaluator = new EvaluatorJob(child, false);
                        childEvaluator.addWaiter(barrier);
                        barrier.inc();
                    }
                }
            }

            if (barrier == null) {
                // that means, all children are already evaluated
                // so we use this thread to evaluate the expression
                eval.run();
            }
            // otherwise, the barrier will do this work
        }

        protected class EvaluateSingleExpressionJob implements Runnable {

            public void run() {
                System.out.println("Evaluating " + expr);
                expr.evaluate();

                synchronized (expr) {
                    if (waiters != null)
                        for (final Informable inf: waiters)
                            inf.inform();
                }

                if (evaluateSuccessors) {
                    for (final Transition trans: expr.getTransitions()) {
                        final Expression succ = trans.getTarget();
                        if (evaluatedSuccessors.add(succ)) {
                            EvaluatorJob job = currentlyEvaluating.get(succ);
                            if (job == null)
                                job = new EvaluatorJob(succ, true);
                        }
                    }
                }

                // now the work is ready
                currentlyEvaluating.remove(expr);
                if (currentlyEvaluating.isEmpty()) {
                    synchronized (readyLock) {
                        readyLock.notify();
                    }
                }
            }

        }

    }

    private class Barrier implements Informable {

        private final Runnable job;
        private int waitNr = 0;

        public Barrier(Runnable jobToRun) {
            this.job = jobToRun;
        }

        public synchronized void inc() {
            ++waitNr;
        }

        public synchronized void inform() {
            assert waitNr > 0;
            --waitNr;
            if (waitNr == 0) {
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
