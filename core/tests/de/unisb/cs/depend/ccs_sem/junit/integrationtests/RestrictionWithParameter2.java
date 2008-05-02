package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class RestrictionWithParameter2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a,b] := foo.a!1 \\ {b};\n"
            + "X[out, out]";
    }

    @Override
    protected void addStates() {
        addState("X[out, out]");
        addState("out!1.0 \\ {out}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "foo");
    }
}
