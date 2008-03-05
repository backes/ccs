package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class RangeTest3 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] = in?a:{a}.out!(-a);\n"
            + "X[13]";
    }

    @Override
    protected void addStates() {
        addState("X[13]");
        addState("out!(-13).0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "in?13");
        addTransition(1, 2, "out!(-13)");
    }
}
