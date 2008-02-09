package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

a.0 + b.0 + c.0 \ {b}
*/

public class RestrictionTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a.0 + b.0 + c.0 \\ {b}";
    }

    @Override
    protected boolean isMinimize() {
        return false;
    }

    @Override
    protected void addStates() {
        addState("a.0 + b.0 + c.0 \\ {b}");
        addState("0 \\ {b}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a");
        addTransition(0, 1, "c");
    }
}
