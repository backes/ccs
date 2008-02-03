package de.unisb.cs.depend.ccs_sem.evaluators;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import de.unisb.cs.depend.ccs_sem.evaluators.executors.SimpleThreadPoolExecutor;


public class SimpleThreadPoolEvaluator extends ParallelEvaluator {

    public SimpleThreadPoolEvaluator() {
        super();
    }

    public SimpleThreadPoolEvaluator(int numThreads) {
        super(numThreads);
    }

    @Override
    protected ExecutorService getExecutor(int threadsToInstantiate,
            ThreadFactory myThreadFactory) {
        return new SimpleThreadPoolExecutor(threadsToInstantiate, myThreadFactory);
    }

}
