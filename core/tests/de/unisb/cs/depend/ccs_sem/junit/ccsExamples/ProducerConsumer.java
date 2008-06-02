package de.unisb.cs.depend.ccs_sem.junit.ccsExamples;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

const MAX_PER_LANE := 3;

const UP := 1;
const DOWN := -1;

SEMA[channel, no, max] := when no < max channel?x:{UP}.SEMA[channel, no + 1, max] + when no > 0 channel?x:{DOWN}.SEMA[channel, no - 1, max];
PRODUCER[sema_ch] := worked.sema_ch!UP.PRODUCER[sema_ch];
CONSUMER[sema_ch] := sema_ch!DOWN.consumed.CONSUMER[sema_ch];
TOGETHER[sema_ch] := PRODUCER[sema_ch] | CONSUMER[sema_ch] | SEMA[sema_ch, 0, MAX_PER_LANE] \ {sema_ch};

TOGETHER[firstlane] | TOGETHER[secondlane]
*/

public class ProducerConsumer extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "SEMA[channel, no, max] := when no < max channel?x:{1}.SEMA[channel, no + 1, max] + when no > 0 channel?x:{-1}.SEMA[channel, no - 1, max];\n"
            + "PRODUCER[sema_ch] := worked.sema_ch!1.PRODUCER[sema_ch];\n"
            + "CONSUMER[sema_ch] := sema_ch!(-1).consumed.CONSUMER[sema_ch];\n"
            + "TOGETHER[sema_ch] := PRODUCER[sema_ch] | CONSUMER[sema_ch] | SEMA[sema_ch, 0, 3] \\ {sema_ch};\n"
            + "\n"
            + "TOGETHER[firstlane] | TOGETHER[secondlane]";
    }

    @Override
    protected boolean isMinimize() {
        return true;
    }

    @Override
    protected void addStates() {
        addState("0");
        addState("1");
        addState("2");
        addState("3");
        addState("4");
        addState("5");
        addState("6");
        addState("7");
        addState("8");
        addState("9");
        addState("10");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "worked");
        addTransition(1, 0, "consumed");
        addTransition(1, 2, "worked");
        addTransition(2, 3, "worked");
        addTransition(2, 1, "consumed");
        addTransition(3, 4, "worked");
        addTransition(3, 2, "consumed");
        addTransition(4, 5, "worked");
        addTransition(4, 3, "consumed");
        addTransition(5, 6, "worked");
        addTransition(5, 4, "consumed");
        addTransition(6, 7, "worked");
        addTransition(6, 5, "consumed");
        addTransition(7, 8, "worked");
        addTransition(7, 6, "consumed");
        addTransition(8, 7, "consumed");
        addTransition(8, 9, "worked");
        addTransition(9, 8, "consumed");
        addTransition(9, 10, "worked");
        addTransition(10, 9, "consumed");
    }
}
