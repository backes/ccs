package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

OUTPUT[min, max] = x!min.0 + when max > min i.OUTPUT[min + 1, max];

OUTPUT[1, 4] | (x?x.out!x.0 \ {x?2, x?4}) \ {x!, x?}
*/

public class SynchronizationTest5 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "OUTPUT[min, max] = x!min.0 + when max > min i.OUTPUT[min + 1, max];\n"
            + "\n"
            + "OUTPUT[1, 4] | (x?x.out!x.0 \\ {x?2, x?4}) \\ {x!, x?}";
    }

    @Override
    protected void addStates() {
        addState("OUTPUT[1, 4] | (x?x.out!x.0 \\ {x?2, x?4}) \\ {x!, x?}");
        addState("0 | (out!3.0 \\ {x?2, x?4}) \\ {x!, x?}");
        addState("0 | (out!1.0 \\ {x?2, x?4}) \\ {x!, x?}");
        addState("0 | (0 \\ {x?2, x?4}) \\ {x!, x?}");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "i");
        addTransition(0, 2, "i");
        addTransition(1, 3, "out!3");
        addTransition(2, 3, "out!1");
    }
}
