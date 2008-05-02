package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class RangeWithParameterTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] := read?x:0..a.write!x;\n"
            + "X[2]";
    }

    @Override
    protected void addStates() {
        addState("X[2]");
        addState("write!0.0");
        addState("write!1.0");
        addState("write!2.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "read?0");
        addTransition(0, 2, "read?1");
        addTransition(0, 3, "read?2");
        addTransition(1, 4, "write!0");
        addTransition(2, 4, "write!1");
        addTransition(3, 4, "write!2");
    }
}
