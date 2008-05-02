package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class QuotesComplex extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        // a is the constant string "b"
        return "const a := b;\n" +
            "X[b] := a | b | c \\ {a, b};\n" +
            "\n" +
            "X[c]\n";
    }

    @Override
    protected void addStates() {
        addState("X[c]");
    }

    @Override
    protected void addTransitions() {
        // no transitions!
    }

}
