package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Or extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch,a,b] := ch!(a || b ? 1 : 0);\n" +
            "X[a, true, true] + X[b, true, false] + X[c, false, false] + X[d, false, true]";
    }

    @Override
    protected void addStates() {
        addState("X[a, true, true] + X[b, true, false] + X[c, false, false] + X[d, false, true]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!1");
        addTransition(0, 1, "b!1");
        addTransition(0, 1, "c!0");
        addTransition(0, 1, "d!1");
    }

}
