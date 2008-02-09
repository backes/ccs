package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

X[a:{0, 1, 2, 3}] = out!a.X[a + 1];

X[0]
*/

public class RangeTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a:{0, 1, 2, 3}] = out!a.X[a + 1];\n"
            + "\n"
            + "X[0]";
    }

    @Override
    protected void addStates() {
        addState("X[0]");
        addState("X[1]");
        addState("X[2]");
        addState("X[3]");
        addState("error_X[4]");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "out!0");
        addTransition(1, 2, "out!1");
        addTransition(2, 3, "out!2");
        addTransition(3, 4, "out!3");
    }
}
