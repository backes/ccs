package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class RangeWithParameterTest3 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a,b,c] := read?x:((a..b)+{c}).write!x;\n"
            + "X[5,7,4711]";
    }

    @Override
    protected void addStates() {
        addState("X[5, 7, 4711]");
        addState("write!5.0");
        addState("write!6.0");
        addState("write!7.0");
        addState("write!4711.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "read?5");
        addTransition(0, 2, "read?6");
        addTransition(0, 3, "read?7");
        addTransition(0, 4, "read?4711");
        addTransition(1, 5, "write!5");
        addTransition(2, 5, "write!6");
        addTransition(3, 5, "write!7");
        addTransition(4, 5, "write!4711");
    }
}
