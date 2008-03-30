package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class QuotesRestriction extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a!1 | \"a\"!2 \\ {a}";
    }

    @Override
    protected void addStates() {
        addState("a!1.0 | \"a\"!2.0 \\ {a}");
    }

    @Override
    protected void addTransitions() {
        // no transitions possible
    }
}
