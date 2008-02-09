package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

x.ERROR + a.(b.0 + (ERROR | bla.foo.0))
*/

public class ErrorTest extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "x.ERROR + a.(b.0 + (ERROR | bla.foo.0))";
    }

    @Override
    protected boolean isMinimize() {
        return false;
    }

    @Override
    protected void addStates() {
        addState("x.ERROR + a.(b.0 + (ERROR | bla.foo.0))");
        addState("ERROR");
        addState("error_b.0 + (ERROR | bla.foo.0)");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "x");
        addTransition(0, 2, "a");
    }
}
