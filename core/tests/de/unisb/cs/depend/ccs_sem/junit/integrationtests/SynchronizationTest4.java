package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

OUTPUT[min, max] = x!min.0 + when max > min i.OUTPUT[min + 1, max];

OUTPUT[1, 4] | x?x:((1..3) - {2}).out!x.0 \ {x}
*/

public class SynchronizationTest4 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "OUTPUT[min, max] := x!min.0 + when max > min i.OUTPUT[min + 1, max];\n"
            + "\n"
            + "OUTPUT[1, 4] | x?x:((1..3) - {2}).out!x.0 \\ {x}";
    }

    @Override
    protected boolean isMinimize() {
        return false;
    }

    @Override
    protected void addStates() {
        addState("OUTPUT[1, 4] | x?x:((1..3) - {2}).out!x.0 \\ {x}");
        addState("OUTPUT[2, 4] | x?x:((1..3) - {2}).out!x.0 \\ {x}");
        addState("0 | out!1.0 \\ {x}");
        addState("OUTPUT[3, 4] | x?x:((1..3) - {2}).out!x.0 \\ {x}");
        addState("0 | 0 \\ {x}");
        addState("OUTPUT[4, 4] | x?x:((1..3) - {2}).out!x.0 \\ {x}");
        addState("0 | out!3.0 \\ {x}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "i");
        addTransition(0, 2, "i");
        addTransition(1, 3, "i");
        addTransition(2, 4, "out!1");
        addTransition(3, 5, "i");
        addTransition(3, 6, "i");
        addTransition(6, 4, "out!3");
    }
}
