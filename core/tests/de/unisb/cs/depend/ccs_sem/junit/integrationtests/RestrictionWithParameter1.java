package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class RestrictionWithParameter1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a,b] := a!1 \\ {b};\n"
            + "X[out, res]";
    }

    @Override
    protected void addStates() {
        addState("X[out, res]");
        addState("0 \\ {res}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "out!1");
    }
}
