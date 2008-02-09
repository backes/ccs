package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

A[x] = out!x.B[(x + 1) % 4];
B[x] = A[x] + foo.B[x];

A[2]
*/

public class RecursionTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return decode("A[x] = out!x.B[(x + 1) %37 4];\n"
            + "B[x] = A[x] + foo.B[x];\n"
            + "\n"
            + "A[2]");
    }

    @Override
    protected boolean isMinimize() {
        return false;
    }

    @Override
    protected void addStates() {
        addState("A[2]");
        addState("B[3]");
        addState("B[0]");
        addState("B[1]");
        addState("B[2]");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "out!2");
        addTransition(1, 2, "out!3");
        addTransition(1, 1, "foo");
        addTransition(2, 3, "out!0");
        addTransition(2, 2, "foo");
        addTransition(3, 4, "out!1");
        addTransition(3, 3, "foo");
        addTransition(4, 1, "out!2");
        addTransition(4, 4, "foo");
    }
}
