package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class UnboundButRestricted2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[in, out] := in?v.out!v + a; X[x, y] \\ {x}";
    }

    @Override
    protected void addStates() {
        addState("X[x, y] \\ {x}");
        addState("0 \\ {x}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a");
    }
}
