package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class ParameterTypeCheckRecursive extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] := out!(a+1); Y[a] := foo.X[a]; Y[2]";
    }

    @Override
    protected void addStates() {
        addState("Y[2]");
        addState("X[2]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "foo");
        addTransition(1, 2, "out!3");
    }

}
