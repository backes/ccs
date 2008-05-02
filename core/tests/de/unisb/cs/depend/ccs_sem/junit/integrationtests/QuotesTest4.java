package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class QuotesTest4 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a,b] := a!b . \"a\"!b . a!\"b\";\n"
            + "Y[x] := x! . X[x,\"x\"];\n"
            + "\n"
            + "Y[c]\n";
    }

    @Override
    protected void addStates() {
        addState("Y[c]");
        addState("X[c, \"x\"]");
        addState("\"a\"!\"x\".c!\"b\".0");
        addState("c!\"b\".0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "c!");
        addTransition(1, 2, "c!x");
        addTransition(2, 3, "a!x");
        addTransition(3, 4, "c!b");
    }

}
