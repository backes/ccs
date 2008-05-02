package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Add extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch,a,b] := ch!(a + b);\n" +
            "X[a, 0, 0] + X[b, 3, 4] + X[c, -2, -3] + X[d, -4, 4]";
    }

    @Override
    protected void addStates() {
        addState("X[a, 0, 0] + X[b, 3, 4] + X[c, -2, -3] + X[d, -4, 4]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!0");
        addTransition(0, 1, "b!7");
        addTransition(0, 1, "c!(-5)");
        addTransition(0, 1, "d!0");
    }

}
