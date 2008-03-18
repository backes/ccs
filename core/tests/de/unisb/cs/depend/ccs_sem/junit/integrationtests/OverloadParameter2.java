package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class OverloadParameter2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] = in?a:{a, -a}.in?a:{2*a, -a}.out!a;\n"
            + "X[3]";
    }

    @Override
    protected void addStates() {
        addState("X[3]");
        addState("in?a:{-3, 6}.out!a.0");
        addState("in?a:{-6, 3}.out!a.0");
        addState("out!6.0");
        addState("out!(-3).0");
        addState("out!(-6).0");
        addState("out!3.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "in?3");
        addTransition(0, 2, "in?(-3)");
        addTransition(1, 3, "in?6");
        addTransition(1, 4, "in?(-3)");
        addTransition(2, 5, "in?(-6)");
        addTransition(2, 6, "in?3");
        addTransition(3, 7, "out!6");
        addTransition(4, 7, "out!(-3)");
        addTransition(5, 7, "out!(-6)");
        addTransition(6, 7, "out!3");
    }
}
