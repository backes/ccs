package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

x.ERROR + a.(b.0 + (ERROR | bla.foo.0))
*/

public class ErrorMinimizationTest extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "x.ERROR + a.(b.0 + (ERROR | bla.foo.0))";
    }

    @Override
    protected boolean isMinimize() {
        return true;
    }

    @Override @Test @Ignore
    public void checkStatesExplicitely() {
        // do nothing
    }

    @Override
    protected void addStates() {
        addState("0");
        addState("error_-1");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a");
        addTransition(0, 1, "x");
    }
}
