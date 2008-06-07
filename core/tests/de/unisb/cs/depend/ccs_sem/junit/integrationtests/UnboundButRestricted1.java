package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class UnboundButRestricted1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "x?v.y!v \\ {x}";
    }

    @Override
    protected void addStates() {
        addState("x?v.y!v.0 \\ {x}");
    }

    @Override
    protected void addTransitions() {
        // no transitions possible
    }
}
