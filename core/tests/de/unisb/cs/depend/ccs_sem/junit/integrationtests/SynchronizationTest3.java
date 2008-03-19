package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class SynchronizationTest3 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        // no synchronization (the two processes are interleaved)
        return "x!1 | x?";
    }

    @Override
    protected void addStates() {
        addState("x!1.0 | x?.0");
        addState("0 | x?.0");
        addState("x!1.0 | 0");
        addState("0 | 0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "x!1");
        addTransition(0, 2, "x?");
        addTransition(1, 3, "x?");
        addTransition(2, 3, "x!1");
    }
}
