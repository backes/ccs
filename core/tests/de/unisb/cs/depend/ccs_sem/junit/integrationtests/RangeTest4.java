package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class RangeTest4 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] = in?a:{a}.out!(-a);\n"
            + "X[13] | X[31]";
    }

    @Override
    protected void addStates() {
        addState("X[13] | X[31]");
        addState("out!(-13).0 | X[31]");
        addState("0 | X[31]");
        addState("out!(-13).0 | out!(-31).0");
        addState("0 | out!(-31).0");
        addState("out!(-13).0 | 0");
        addState("X[13] | out!(-31).0");
        addState("X[13] | 0");
        addState("0 | 0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "in?13");
        addTransition(0, 6, "in?31");
        addTransition(1, 2, "out!-13");
        addTransition(1, 3, "in?31");
        addTransition(2, 4, "in?31");
        addTransition(3, 4, "out!-13");
        addTransition(3, 5, "out!-31");
        addTransition(4, 8, "out!-31");
        addTransition(5, 8, "out!-13");
        addTransition(6, 3, "in?13");
        addTransition(6, 7, "out!-31");
        addTransition(7, 5, "in?13");
    }
}
