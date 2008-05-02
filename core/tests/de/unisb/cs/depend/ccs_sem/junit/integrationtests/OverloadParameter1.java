package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class OverloadParameter1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] := in?a:(0..2).out!a;\n"
            + "X[1]";
    }

    @Override
    protected void addStates() {
        addState("X[1]");
        addState("out!0.0");
        addState("out!1.0");
        addState("out!2.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "in?0");
        addTransition(0, 2, "in?1");
        addTransition(0, 3, "in?2");
        addTransition(1, 4, "out!0");
        addTransition(2, 4, "out!1");
        addTransition(3, 4, "out!2");
    }
}
