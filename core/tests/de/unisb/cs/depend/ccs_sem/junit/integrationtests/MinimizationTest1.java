package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class MinimizationTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "i.(i.i.a + i.i.a)";
    }

    @Override
    protected void addStates() {
        addState("i.(i.i.a.0 + i.i.a.0)");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a");
    }

    @Override
    protected boolean isMinimize() {
        return true;
    }
}
