package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class SynchronizationTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "x!1 | x?x.out!x";
    }

    @Override
    protected void addStates() {
        addState("x!1.0 | x?x.out!x.0");
        addState("0 | x?x.out!x.0");
        addState("0 | out!1.0");
        addState("0 | 0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "x!1");
        addTransition(0, 2, "i");
        addTransition(2, 3, "out!1");
    }
}
