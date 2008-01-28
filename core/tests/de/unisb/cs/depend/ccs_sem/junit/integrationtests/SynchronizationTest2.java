package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class SynchronizationTest2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "x! | x?x";
    }

    @Override
    protected void addStates() {
        addState("x!.0 | x?x.0");
        addState("0 | x?x.0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "x!");
    }
}
