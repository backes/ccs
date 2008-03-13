package de.unisb.cs.depend.ccs_sem.evaluators;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import de.unisb.cs.depend.ccs_sem.evaluators.executors.ThreadBasedExecutor;


public class ThreadBasedEvaluator extends ParallelEvaluator {

    public ThreadBasedEvaluator() {
        super();
    }

    public ThreadBasedEvaluator(int numThreads) {
        super(numThreads);
    }

    @Override
    protected ExecutorService getExecutor(int threadsToInstantiate,
            ThreadFactory threadFactory) {
        return new ThreadBasedExecutor(threadsToInstantiate, threadFactory);
    }

}
