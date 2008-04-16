package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Negative extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch,a] = ch!(-a);\n" +
            "X[a, 0] + X[b, 3] + X[c, -2] + X[d, -0] + X[e, ----6] + X[f, -----6]";
    }

    @Override
    protected void addStates() {
        addState("X[a, 0] + X[b, 3] + X[c, -2] + X[d, 0] + X[e, 6] + X[f, -6]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!0");
        addTransition(0, 1, "b!(-3)");
        addTransition(0, 1, "c!2");
        addTransition(0, 1, "d!0");
        addTransition(0, 1, "e!(-6)");
        addTransition(0, 1, "f!6");
    }

}
