package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class MinimizationTest7 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a.i + i.a";
    }

    @Override
    protected void addStates() {
        addState("a.i.0 + i.a.0");
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

    @Override @Test @Ignore
    public void checkStatesExplicitely() {
        super.checkStatesExplicitely();
    }
}
