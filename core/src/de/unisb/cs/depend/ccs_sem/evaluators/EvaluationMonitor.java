package de.unisb.cs.depend.ccs_sem.evaluators;


public interface EvaluationMonitor {

    void newState();

    void newTransitions(int count);

    void ready();

    void error(String errorString);

}
