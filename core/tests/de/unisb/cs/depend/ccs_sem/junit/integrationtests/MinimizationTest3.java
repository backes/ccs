package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/**
 * Minimization should remove all transitions here.
 */
public class MinimizationTest3 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "i";
    }

    @Override
    protected void addStates() {
        addState("i.0");
    }

    @Override
    protected void addTransitions() {
        // no transitions
    }

    @Override
    protected boolean isMinimize() {
        return true;
    }
}
