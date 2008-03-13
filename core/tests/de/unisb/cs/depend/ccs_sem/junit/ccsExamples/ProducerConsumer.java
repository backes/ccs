package de.unisb.cs.depend.ccs_sem.junit.ccsExamples;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

SEMA[channel, no, max] = when no < max channel?"up".SEMA[channel, no + 1, max] + when no > 0 channel?"down".SEMA[channel, no - 1, max];
PRODUCER[sema_ch] = sema_ch!produce.sema_ch!up.PRODUCER[sema_ch];
CONSUMER[sema_ch] = sema_ch!down.sema_ch!consume.CONSUMER[sema_ch];
TOGETHER[sema_ch] = PRODUCER[sema_ch] | CONSUMER[sema_ch] | SEMA[sema_ch, 0, 2] \ {sema_ch!down, sema_ch!up, sema_ch?down, sema_ch?up};

TOGETHER[firstlane] | TOGETHER[secondlane]
*/

public class ProducerConsumer extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "SEMA[channel, no, max] = when no < max channel?\"up\".SEMA[channel, no + 1, max] + when no > 0 channel?\"down\".SEMA[channel, no - 1, max];\n"
            + "PRODUCER[sema_ch] = sema_ch!produce.sema_ch!up.PRODUCER[sema_ch];\n"
            + "CONSUMER[sema_ch] = sema_ch!down.sema_ch!consume.CONSUMER[sema_ch];\n"
            + "TOGETHER[sema_ch] = PRODUCER[sema_ch] | CONSUMER[sema_ch] | SEMA[sema_ch, 0, 2] \\ {sema_ch!down, sema_ch!up, sema_ch?down, sema_ch?up};\n"
            + "\n"
            + "TOGETHER[firstlane] | TOGETHER[secondlane]";
    }

    @Override
    protected boolean isMinimize() {
        return true;
    }

    @Override @Test @Ignore
    public void checkStatesExplicitely() {
        super.checkStatesExplicitely();
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
        addState("11");
        addState("12");
        addState("13");
        addState("14");
        addState("15");
        addState("16");
        addState("17");
        addState("18");
        addState("19");
        addState("20");
        addState("21");
        addState("22");
        addState("23");
        addState("24");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "firstlane!produce");
        addTransition(0, 2, "secondlane!produce");
        addTransition(1, 0, "firstlane!consume");
        addTransition(1, 3, "secondlane!produce");
        addTransition(1, 4, "firstlane!produce");
        addTransition(2, 3, "firstlane!produce");
        addTransition(2, 0, "secondlane!consume");
        addTransition(2, 5, "secondlane!produce");
        addTransition(3, 6, "firstlane!produce");
        addTransition(3, 2, "firstlane!consume");
        addTransition(3, 1, "secondlane!consume");
        addTransition(3, 7, "secondlane!produce");
        addTransition(4, 1, "firstlane!consume");
        addTransition(4, 6, "secondlane!produce");
        addTransition(4, 8, "firstlane!produce");
        addTransition(5, 2, "secondlane!consume");
        addTransition(5, 7, "firstlane!produce");
        addTransition(5, 9, "secondlane!produce");
        addTransition(6, 3, "firstlane!consume");
        addTransition(6, 10, "secondlane!produce");
        addTransition(6, 4, "secondlane!consume");
        addTransition(6, 11, "firstlane!produce");
        addTransition(7, 10, "firstlane!produce");
        addTransition(7, 3, "secondlane!consume");
        addTransition(7, 5, "firstlane!consume");
        addTransition(7, 12, "secondlane!produce");
        addTransition(8, 13, "firstlane!produce");
        addTransition(8, 4, "firstlane!consume");
        addTransition(8, 11, "secondlane!produce");
        addTransition(9, 14, "secondlane!produce");
        addTransition(9, 12, "firstlane!produce");
        addTransition(9, 5, "secondlane!consume");
        addTransition(10, 7, "firstlane!consume");
        addTransition(10, 6, "secondlane!consume");
        addTransition(10, 15, "secondlane!produce");
        addTransition(10, 16, "firstlane!produce");
        addTransition(11, 8, "secondlane!consume");
        addTransition(11, 6, "firstlane!consume");
        addTransition(11, 17, "firstlane!produce");
        addTransition(11, 16, "secondlane!produce");
        addTransition(12, 7, "secondlane!consume");
        addTransition(12, 18, "secondlane!produce");
        addTransition(12, 15, "firstlane!produce");
        addTransition(12, 9, "firstlane!consume");
        addTransition(13, 17, "secondlane!produce");
        addTransition(13, 8, "firstlane!consume");
        addTransition(14, 9, "secondlane!consume");
        addTransition(14, 18, "firstlane!produce");
        addTransition(15, 19, "firstlane!produce");
        addTransition(15, 10, "secondlane!consume");
        addTransition(15, 20, "secondlane!produce");
        addTransition(15, 12, "firstlane!consume");
        addTransition(16, 19, "secondlane!produce");
        addTransition(16, 10, "firstlane!consume");
        addTransition(16, 21, "firstlane!produce");
        addTransition(16, 11, "secondlane!consume");
        addTransition(17, 11, "firstlane!consume");
        addTransition(17, 13, "secondlane!consume");
        addTransition(17, 21, "secondlane!produce");
        addTransition(18, 14, "firstlane!consume");
        addTransition(18, 20, "firstlane!produce");
        addTransition(18, 12, "secondlane!consume");
        addTransition(19, 16, "secondlane!consume");
        addTransition(19, 15, "firstlane!consume");
        addTransition(19, 22, "firstlane!produce");
        addTransition(19, 23, "secondlane!produce");
        addTransition(20, 23, "firstlane!produce");
        addTransition(20, 15, "secondlane!consume");
        addTransition(20, 18, "firstlane!consume");
        addTransition(21, 22, "secondlane!produce");
        addTransition(21, 16, "firstlane!consume");
        addTransition(21, 17, "secondlane!consume");
        addTransition(22, 19, "firstlane!consume");
        addTransition(22, 21, "secondlane!consume");
        addTransition(22, 24, "secondlane!produce");
        addTransition(23, 19, "secondlane!consume");
        addTransition(23, 24, "firstlane!produce");
        addTransition(23, 20, "firstlane!consume");
        addTransition(24, 22, "secondlane!consume");
        addTransition(24, 23, "firstlane!consume");
    }
}
