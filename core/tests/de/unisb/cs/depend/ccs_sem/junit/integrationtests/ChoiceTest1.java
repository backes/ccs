package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

a.b.0 + c.(a.b.0 + d.e.0)
*/

public class ChoiceTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a.b.0 + c.(a.b.0 + d.e.0)";
    }

    @Override
    protected boolean isMinimize() {
        return false;
    }

    @Override
    protected void addStates() {
        addState("a.b.0 + c.(a.b.0 + d.e.0)");
        addState("b.0");
        addState("a.b.0 + d.e.0");
        addState("0");
        addState("e.0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a");
        addTransition(0, 2, "c");
        addTransition(1, 3, "b");
        addTransition(2, 1, "a");
        addTransition(2, 4, "d");
        addTransition(4, 3, "e");
    }
}
