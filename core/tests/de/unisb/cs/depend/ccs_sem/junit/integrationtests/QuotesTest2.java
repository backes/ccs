package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class QuotesTest2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] := a!1.\"a\"!2.0;\n"
            + "X[b]";
    }

    @Override
    protected void addStates() {
        addState("X[b]");
        addState("\"a\"!2.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "b!1");
        addTransition(1, 2, "a!2");
    }
}
