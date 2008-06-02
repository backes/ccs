package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Not extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch, val] := ch!(val?1:0) + when val n.X[ch, !val];\n" +
            "X[a, true] + X[b, false]";
    }

    @Override
    protected void addStates() {
        addState("X[a, true] + X[b, false]");
        addState("X[a, false]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 2, "a!1");
        addTransition(0, 2, "b!0");
        addTransition(0, 1, "n");
        addTransition(1, 2, "a!0");
    }

}
