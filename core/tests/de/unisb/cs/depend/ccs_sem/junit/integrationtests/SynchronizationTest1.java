package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class SynchronizationTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        // "x?x" is only instantiated for "x?1" and a warning is produced
        return "x!1 | x?x.out!x \\ {x}";
    }

    @Override
    protected void addStates() {
        addState("x!1.0 | x?x.out!x.0 \\ {x}");
        addState("0 | out!1.0 \\ {x}");
        addState("0 | 0 \\ {x}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "i");
        addTransition(1, 2, "out!1");
    }
}
