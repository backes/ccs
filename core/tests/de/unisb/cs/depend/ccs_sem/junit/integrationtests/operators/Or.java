package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Or extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch,a,b] = ch!(a || b);\n" +
            "X[a, true, true] + X[b, true, false] + X[c, false, false] + X[d, false, true]";
    }

    @Override
    protected void addStates() {
        addState("X[a, true, true] + X[b, true, false] + X[c, false, false] + X[d, false, true]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!true");
        addTransition(0, 1, "b!true");
        addTransition(0, 1, "c!false");
        addTransition(0, 1, "d!true");
    }

}
