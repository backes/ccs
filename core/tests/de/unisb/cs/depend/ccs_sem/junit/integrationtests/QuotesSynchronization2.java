package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class QuotesSynchronization2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[y] := y!1;\n"
            + "X[\"a\"] | a?n.b!n \\ {a}";
    }

    @Override
    protected void addStates() {
        addState("X[\"a\"] | a?n.b!n.0 \\ {a}");
        addState("0 | b!1.0 \\ {a}");
        addState("0 | 0 \\ {a}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "i");
        addTransition(1, 2, "b!1");
    }
}
