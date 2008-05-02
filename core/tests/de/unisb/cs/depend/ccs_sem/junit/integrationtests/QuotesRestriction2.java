package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class QuotesRestriction2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] := a!1 + \"a\"!2 \\ {\"a\"};\n"
            + "x.X[b] + y.X[a]";
    }

    @Override
    protected void addStates() {
        addState("x.X[b] + y.X[a]");
        addState("X[b]");
        addState("X[a]");
        addState("0 \\ {\"a\"}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "x");
        addTransition(0, 2, "y");
        addTransition(1, 3, "b!1");
        // no transitions from state 2
    }
}
