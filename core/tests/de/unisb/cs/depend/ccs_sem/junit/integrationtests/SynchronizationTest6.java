package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class SynchronizationTest6 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "out!1 + a.out!3 | out?x.read!x \\ {out}";
    }

    @Override
    protected void addStates() {
        addState("out!1.0 + a.out!3.0 | out?x.read!x.0 \\ {out}");
        addState("0 | read!1.0 \\ {out}");
        addState("out!3.0 | out?x.read!x.0 \\ {out}");
        addState("0 | read!3.0 \\ {out}");
        addState("0 | 0 \\ {out}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "i");
        addTransition(0, 2, "a");
        addTransition(1, 4, "read!1");
        addTransition(2, 3, "i");
        addTransition(3, 4, "read!3");
    }
}
