package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class QuotesTest3 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a!1 | a?n.b!n.c!\"n\".d!n \\ {a}";
    }

    @Override
    protected void addStates() {
        addState("a!1.0 | a?n.b!n.c!\"n\".d!n.0 \\ {a}");
        addState("0 | b!1.c!\"n\".d!1.0 \\ {a}");
        addState("0 | c!\"n\".d!1.0 \\ {a}");
        addState("0 | d!1.0 \\ {a}");
        addState("0 | 0 \\ {a}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "i");
        addTransition(1, 2, "b!1");
        addTransition(2, 3, "c!n");
        addTransition(3, 4, "d!1");
    }
}
