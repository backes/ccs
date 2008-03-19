package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class SynchronizationTest7 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        // they can synchronized, but are not forced to do so
        return "a! | a?";
    }

    @Override
    protected void addStates() {
        addState("a!.0 | a?.0");
        addState("a!.0 | 0");
        addState("0 | a?.0");
        addState("0 | 0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a?");
        addTransition(0, 2, "a!");
        addTransition(0, 3, "i");
        addTransition(1, 3, "a!");
        addTransition(2, 3, "a?");
    }
}
