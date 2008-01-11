package de.unisb.cs.depend.ccs_sem.evalutators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public class ParallelEvaluator implements Evaluator {

    private final Integer numThreads;

    private ExecutorService executor = null;

    private Map<Expression, EvaluatorJob> currentlyEvaluating = null;

    public ParallelEvaluator(int numThreads) {
        this.numThreads = numThreads;
    }

    public ParallelEvaluator() {
        this.numThreads = null;
    }

    public void evaluate(Expression expr) {
        if (expr.isEvaluated())
            return;

        initialize();

        final Informable informer = new Informable() {

            public synchronized void inform() {
                this.notify();
            }
        };

        synchronized (informer) {
            final Runnable job = new EvaluatorJob(expr, informer);

            executor.execute(job);

            try {
                informer.wait();
            } catch (final InterruptedException e) {
                throw new InternalSystemException(
                        "Interrupted while waiting for parallel evaluation to finish.");
            }
        }

        shutdown();
    }

    public void evaluateAll(Expression expr) {
        initialize();



        // TODO

        shutdown();
    }

    private void initialize() {
        assert executor == null;
        assert currentlyEvaluating == null;

        final int threadsToInstantiate =
                numThreads == null ? Runtime.getRuntime().availableProcessors()
                                  : numThreads;

        executor = Executors.newFixedThreadPool(threadsToInstantiate);

        currentlyEvaluating = new HashMap<Expression, EvaluatorJob>();
    }

    private void shutdown() {
        executor.shutdown();
        executor = null;

        assert currentlyEvaluating.isEmpty();
        currentlyEvaluating = null;
    }

    private static interface Informable {

        void inform();
    }

    private class EvaluatorJob implements Runnable {

        private List<Informable> waiters = null;
        private final Expression expr;

        public EvaluatorJob(Expression expr) {
            currentlyEvaluating.put(expr, this);
            this.expr = expr;
        }

        public EvaluatorJob(Expression expr, Informable info) {
            this(expr);
            addWaiter(info);
        }

        private synchronized void addWaiter(Informable info) {
            if (waiters == null)
                waiters = new ArrayList<Informable>(1);
            waiters.add(info);
        }

        public void run() {
            Barrier barrier = null;

            final EvaluateNowJob eval = new EvaluateNowJob();

            for (final Expression child: expr.getChildren()) {
                synchronized (child) {
                    if (!child.isEvaluated()) {
                        if (barrier == null) {
                            barrier = new Barrier(eval);
                        }
                        EvaluatorJob childEvaluator =
                                currentlyEvaluating.get(child);
                        if (childEvaluator == null) {
                            childEvaluator = new EvaluatorJob(child, barrier);
                            executor.execute(childEvaluator);
                        } else {
                            childEvaluator.addWaiter(barrier);
                        }
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

        private class EvaluateNowJob implements Runnable {

            public void run() {
                expr.evaluate();

                synchronized (expr) {
                    for (final Informable inf: waiters)
                        inf.inform();
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

}
