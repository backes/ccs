package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class QuotesTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "\"a\"";
    }

    @Override
    protected void addStates() {
        addState("\"a\".0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a");
    }
}
