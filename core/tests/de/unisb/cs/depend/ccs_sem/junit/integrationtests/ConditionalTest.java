package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

STRING[b] = out!(b ? yes : no).(when b INTEGER[false] + when !b STRING[!b]);
INTEGER[b] = out!(b ? 1 : 0).(when b BOOLEAN[!b] + when !b INTEGER[!b]);
BOOLEAN[b] = out!(b ? false : true).when !b BOOLEAN[true];

STRING[false]
*/

public class ConditionalTest extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "STRING[b] = out!(b ? yes : no).(when b INTEGER[false] else STRING[!b]);\n"
            + "INTEGER[b] = out!(b ? 1 : 0).(when b BOOLEAN[!b] + when !b INTEGER[!b]);\n"
            + "BOOLEAN[b] = out!(b ? false : true).when !b BOOLEAN[true];\n"
            + "\n"
            + "STRING[false]";
    }

    @Override
    protected void addStates() {
        addState("STRING[false]");
        addState("0 + STRING[true]");
        addState("INTEGER[false] + 0");
        addState("0 + INTEGER[true]");
        addState("BOOLEAN[false] + 0");
        addState("BOOLEAN[true]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "out!no");
        addTransition(1, 2, "out!yes");
        addTransition(2, 3, "out!0");
        addTransition(3, 4, "out!1");
        addTransition(4, 5, "out!true");
        addTransition(5, 6, "out!false");
    }
}
