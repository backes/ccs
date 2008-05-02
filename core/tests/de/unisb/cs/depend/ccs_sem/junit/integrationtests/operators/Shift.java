package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Shift extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch,a,b] := ch!(a << b); Y[ch,a,b] := ch!(a >> b);\n" +
            "X[a, 0, 0] + X[b, 0, 4] + X[c, 1, 3] + X[d, -4, 4] + X[e, 7, 2] +" +
            "Y[f, 0, 0] + Y[g, 0, 4] + Y[h, 7, 1] + Y[i, -28, 2] + " +
            "X[j, 3, 0] + Y[k, 3, 0]";
    }

    @Override
    protected void addStates() {
        addState("X[a, 0, 0] + X[b, 0, 4] + X[c, 1, 3] + X[d, -4, 4] + X[e, 7, 2] + " +
            "Y[f, 0, 0] + Y[g, 0, 4] + Y[h, 7, 1] + Y[i, -28, 2] + " +
            "X[j, 3, 0] + Y[k, 3, 0]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!0");
        addTransition(0, 1, "b!0");
        addTransition(0, 1, "c!8");
        addTransition(0, 1, "d!(-64)");
        addTransition(0, 1, "e!28");
        addTransition(0, 1, "f!0");
        addTransition(0, 1, "g!0");
        addTransition(0, 1, "h!3");
        addTransition(0, 1, "i!(-7)");
        addTransition(0, 1, "j!3");
        addTransition(0, 1, "k!3");
    }

}
