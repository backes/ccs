package de.unisb.cs.depend.ccs_sem.evalutators;


public interface EvaluationMonitor {

    void newState();

    void newTransitions(int size);

    void ready();

    void error(String errorString);

}
