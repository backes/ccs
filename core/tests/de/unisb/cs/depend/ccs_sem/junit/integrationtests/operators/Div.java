package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Div extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch,a,b] = ch!(a / b);\n" +
            "X[a, 0, 3] + X[b, 1, 3] + X[c, 2, 3] + X[d, 3, 3] + X[e, 4, 3] + " +
            "foo.X[f, 1, 0] + bar.X[g, 0, 0] + " +
            "X[h, -1, 3] + X[i, -2, 3] + X[j, -3, 3] + X[k, -4, 3]";
    }

    @Override
    protected void addStates() {
        addState("X[a, 0, 3] + X[b, 1, 3] + X[c, 2, 3] + X[d, 3, 3] + X[e, 4, 3] + " +
            "foo.X[f, 1, 0] + bar.X[g, 0, 0] + " +
            "X[h, -1, 3] + X[i, -2, 3] + X[j, -3, 3] + X[k, -4, 3]");
        addState("0");
        addState("error_X[f, 1, 0]");
        addState("error_X[g, 0, 0]");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!0");
        addTransition(0, 1, "b!0");
        addTransition(0, 1, "c!0");
        addTransition(0, 1, "d!1");
        addTransition(0, 1, "e!1");
        addTransition(0, 1, "h!0");
        addTransition(0, 1, "i!0");
        addTransition(0, 1, "j!(-1)");
        addTransition(0, 1, "k!(-1)");
        addTransition(0, 2, "foo");
        addTransition(0, 3, "bar");
    }

}
