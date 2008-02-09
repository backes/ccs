package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

a.b.0 | c.(a.b.0 | d.e.0)
*/

public class ParallelTest1 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a.b.0 | c.(a.b.0 | d.e.0)";
    }

    @Override
    protected boolean isMinimize() {
        return false;
    }

    @Override
    protected void addStates() {
        addState("a.b.0 | c.(a.b.0 | d.e.0)");
        addState("b.0 | c.(a.b.0 | d.e.0)");
        addState("a.b.0 | a.b.0 | d.e.0");
        addState("b.0 | a.b.0 | d.e.0");
        addState("0 | c.(a.b.0 | d.e.0)");
        addState("a.b.0 | b.0 | d.e.0");
        addState("a.b.0 | a.b.0 | e.0");
        addState("b.0 | a.b.0 | e.0");
        addState("b.0 | b.0 | d.e.0");
        addState("0 | a.b.0 | d.e.0");
        addState("a.b.0 | b.0 | e.0");
        addState("a.b.0 | 0 | d.e.0");
        addState("a.b.0 | a.b.0 | 0");
        addState("0 | a.b.0 | e.0");
        addState("b.0 | a.b.0 | 0");
        addState("b.0 | b.0 | e.0");
        addState("b.0 | 0 | d.e.0");
        addState("0 | b.0 | d.e.0");
        addState("a.b.0 | 0 | e.0");
        addState("a.b.0 | b.0 | 0");
        addState("0 | a.b.0 | 0");
        addState("0 | b.0 | e.0");
        addState("b.0 | b.0 | 0");
        addState("b.0 | 0 | e.0");
        addState("0 | 0 | d.e.0");
        addState("a.b.0 | 0 | 0");
        addState("0 | b.0 | 0");
        addState("0 | 0 | e.0");
        addState("b.0 | 0 | 0");
        addState("0 | 0 | 0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a");
        addTransition(0, 2, "c");
        addTransition(1, 3, "c");
        addTransition(1, 4, "b");
        addTransition(2, 5, "a");
        addTransition(2, 3, "a");
        addTransition(2, 6, "d");
        addTransition(3, 7, "d");
        addTransition(3, 8, "a");
        addTransition(3, 9, "b");
        addTransition(4, 9, "c");
        addTransition(5, 10, "d");
        addTransition(5, 8, "a");
        addTransition(5, 11, "b");
        addTransition(6, 12, "e");
        addTransition(6, 10, "a");
        addTransition(6, 7, "a");
        addTransition(7, 13, "b");
        addTransition(7, 14, "e");
        addTransition(7, 15, "a");
        addTransition(8, 15, "d");
        addTransition(8, 16, "b");
        addTransition(8, 17, "b");
        addTransition(9, 13, "d");
        addTransition(9, 17, "a");
        addTransition(10, 18, "b");
        addTransition(10, 19, "e");
        addTransition(10, 15, "a");
        addTransition(11, 18, "d");
        addTransition(11, 16, "a");
        addTransition(12, 19, "a");
        addTransition(12, 14, "a");
        addTransition(13, 20, "e");
        addTransition(13, 21, "a");
        addTransition(14, 20, "b");
        addTransition(14, 22, "a");
        addTransition(15, 23, "b");
        addTransition(15, 21, "b");
        addTransition(15, 22, "e");
        addTransition(16, 24, "b");
        addTransition(16, 23, "d");
        addTransition(17, 24, "b");
        addTransition(17, 21, "d");
        addTransition(18, 25, "e");
        addTransition(18, 23, "a");
        addTransition(19, 25, "b");
        addTransition(19, 22, "a");
        addTransition(20, 26, "a");
        addTransition(21, 27, "b");
        addTransition(21, 26, "e");
        addTransition(22, 28, "b");
        addTransition(22, 26, "b");
        addTransition(23, 27, "b");
        addTransition(23, 28, "e");
        addTransition(24, 27, "d");
        addTransition(25, 28, "a");
        addTransition(26, 29, "b");
        addTransition(27, 29, "e");
        addTransition(28, 29, "b");
    }
}
