package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class QuotesSynchronization1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "\"a\"! | a?.b";
    }

    @Override
    protected void addStates() {
        addState("\"a\"!.0 | a?.b.0");
        addState("0 | a?.b.0");
        addState("\"a\"!.0 | b.0");
        addState("0 | b.0");
        addState("\"a\"!.0 | 0");
        addState("0 | 0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!");
        addTransition(0, 2, "a?");
        addTransition(0, 3, "i");
        addTransition(1, 3, "a?");
        addTransition(2, 3, "a!");
        addTransition(2, 4, "b");
        addTransition(3, 5, "b");
        addTransition(4, 5, "a!");
    }
}
