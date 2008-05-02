package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/**
 * Test whether the minimization algorithm detects cycles.
 */
public class MinimizationTest4 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X := i.X+a; X";
    }

    @Override
    protected void addStates() {
        addState("X");
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
