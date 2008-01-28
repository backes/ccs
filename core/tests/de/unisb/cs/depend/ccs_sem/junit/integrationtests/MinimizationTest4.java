package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/**
 * Test whether the minimization algorithm detects cycles.
 */
public class MinimizationTest4 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X=i.X+a; X";
    }

    @Override
    protected void addStates() {
        addState("X");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 0, "i");
        addTransition(0, 1, "a");
    }
}
