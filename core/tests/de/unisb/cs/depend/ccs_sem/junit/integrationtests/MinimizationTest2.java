package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class MinimizationTest2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "i.(a + i.(b + i.c))";
    }

    @Override
    protected void addStates() {
        addState("i.(a.0 + i.(b.0 + i.c.0))");
        addState("b.0 + i.c.0");
        addState("c.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 3, "a");
        addTransition(0, 1, "i");
        addTransition(1, 3, "b");
        addTransition(1, 2, "i");
        addTransition(2, 3, "c");
    }

    @Override
    protected boolean isMinimize() {
        return true;
    }

    @Override @Test @Ignore
    public void checkStatesExplicitely() {
        super.checkStatesExplicitely();
    }
}
