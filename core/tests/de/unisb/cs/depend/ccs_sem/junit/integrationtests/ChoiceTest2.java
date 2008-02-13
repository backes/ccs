package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

b + a + b
*/

public class ChoiceTest2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "b + a + b";
    }

    @Override
    protected boolean isMinimize() {
        return false;
    }

    @Override
    protected void addStates() {
        addState("b.0 + a.0 + b.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "b");
        addTransition(0, 1, "a");
    }
}
