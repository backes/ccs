package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class RangeTest5 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "READ_INT[from, to] = in?a:(from..to).got!a.READ_INT[a+1, to];\n"
            + "READ_INT[7, 9]";
    }

    @Override
    protected void addStates() {
        addState("READ_INT[7, 9]");
        addState("got!7.READ_INT[8, 9]");
        addState("got!8.READ_INT[9, 9]");
        addState("got!9.READ_INT[10, 9]");
        addState("READ_INT[8, 9]");
        addState("READ_INT[9, 9]");
        addState("READ_INT[10, 9]");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "in?7");
        addTransition(0, 2, "in?8");
        addTransition(0, 3, "in?9");
        addTransition(1, 4, "got!7");
        addTransition(2, 5, "got!8");
        addTransition(3, 6, "got!9");
        addTransition(4, 2, "in?8");
        addTransition(4, 3, "in?9");
        addTransition(5, 3, "in?9");
    }
}
