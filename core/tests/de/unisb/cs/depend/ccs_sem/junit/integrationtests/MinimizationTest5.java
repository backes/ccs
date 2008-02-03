package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class MinimizationTest5 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a + i.(i.b + i.i.b)";
    }

    @Override
    protected void addStates() {
        addState("a.0 + i.(i.b.0 + i.i.b.0)");
        addState("i.b.0 + i.i.b.0");
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
}
