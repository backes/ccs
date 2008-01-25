package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class SimplestTest extends IntegrationTest {

    @Override
    protected void addStates() {
        addState("0");
    }

    @Override
    protected void addTransitions() {
        // no transitions
    }

    @Override
    protected String getExpressionString() {
        return "0";
    }

}
