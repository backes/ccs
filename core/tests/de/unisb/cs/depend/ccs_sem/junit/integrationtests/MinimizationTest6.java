package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class MinimizationTest6 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a + i.(b + i.b)";
    }

    @Override
    protected void addStates() {
        addState("a.0 + i.(b.0 + i.b.0)");
        addState("b.0 + i.b.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "i");
        addTransition(0, 2, "a");
        addTransition(1, 2, "b");
    }

    @Override
    protected boolean isMinimize() {
        return true;
    }

    @Override
    protected int getChecks() {
        return CHECK_BISIMILARITY | CHECK_STATE_NR;
    }
}
