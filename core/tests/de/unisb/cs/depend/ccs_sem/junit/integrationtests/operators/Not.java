package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Not extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch, val] := ch!(!val);\n" +
            "X[a, true] + X[b, false]";
    }

    @Override
    protected void addStates() {
        addState("X[a, true] + X[b, false]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!false");
        addTransition(0, 1, "b!true");
    }

}
