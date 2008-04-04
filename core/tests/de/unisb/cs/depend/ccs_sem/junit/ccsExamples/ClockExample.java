package de.unisb.cs.depend.ccs_sem.junit.ccsExamples;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

T = tick!.T;
CLOCK = STOPPED[0] | T \ {tick};
STOPPED[x] = tick?.STOPPED[x] + start?.RUNNING[x] + stop?.STOPPED[x] + reset?.STOPPED[0] + get!x.STOPPED[x];
RUNNING[x] = tick?.when x < 3 RUNNING[x + 1] + start?.RUNNING[x] + stop?.STOPPED[x] + reset?.FREEZED[x, x] + get!x.RUNNING[x];
FREEZED[x, y] = tick?.when x < 3 FREEZED[x + 1, y] + start?.FREEZED[x, y] + stop?.STOPPED[x] + reset?.RUNNING[x] + get!y.FREEZED[x, y];

CLOCK
*/

public class ClockExample extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return decode("T = tick!.T%59%13%10CLOCK = STOPPED[0] | T %92 {tick}%59%13%10STOPPED[x] = tick?.STOPPED[x] %43 start?.RUNNING[x] %43 stop?.STOPPED[x] %43 reset?.STOPPED[0] %43 get!x.STOPPED[x]%59%13%10RUNNING[x] = tick?.when x %60 3 RUNNING[x %43 1] %43 start?.RUNNING[x] %43 stop?.STOPPED[x] %43 reset?.FREEZED[x, x] %43 get!x.RUNNING[x]%59%13%10FREEZED[x, y] = tick?.when x %60 3 FREEZED[x %43 1, y] %43 start?.FREEZED[x, y] %43 stop?.STOPPED[x] %43 reset?.RUNNING[x] %43 get!y.FREEZED[x, y]%59%13%10%13%10CLOCK");
    }

    @Override
    protected void addStates() {
        addState("CLOCK");
        addState(decode("RUNNING[0] | T %92 {tick}"));
        addState(decode("STOPPED[0] | T %92 {tick}"));
        addState(decode("FREEZED[0, 0] | T %92 {tick}"));
        addState(decode("RUNNING[1] | T %92 {tick}"));
        addState(decode("FREEZED[1, 0] | T %92 {tick}"));
        addState(decode("STOPPED[1] | T %92 {tick}"));
        addState(decode("FREEZED[1, 1] | T %92 {tick}"));
        addState(decode("RUNNING[2] | T %92 {tick}"));
        addState(decode("FREEZED[2, 0] | T %92 {tick}"));
        addState(decode("FREEZED[2, 1] | T %92 {tick}"));
        addState(decode("STOPPED[2] | T %92 {tick}"));
        addState(decode("FREEZED[2, 2] | T %92 {tick}"));
        addState(decode("RUNNING[3] | T %92 {tick}"));
        addState(decode("FREEZED[3, 0] | T %92 {tick}"));
        addState(decode("FREEZED[3, 1] | T %92 {tick}"));
        addState(decode("FREEZED[3, 2] | T %92 {tick}"));
        addState(decode("STOPPED[3] | T %92 {tick}"));
        addState(decode("FREEZED[3, 3] | T %92 {tick}"));
        addState(decode("0 | T %92 {tick}"));
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "start?");
        addTransition(0, 2, "stop?");
        addTransition(0, 2, "reset?");
        addTransition(0, 2, "get!0");
        addTransition(0, 2, "i");
        addTransition(1, 1, "start?");
        addTransition(1, 2, "stop?");
        addTransition(1, 3, "reset?");
        addTransition(1, 1, "get!0");
        addTransition(1, 4, "i");
        addTransition(2, 1, "start?");
        addTransition(2, 2, "stop?");
        addTransition(2, 2, "reset?");
        addTransition(2, 2, "get!0");
        addTransition(2, 2, "i");
        addTransition(3, 3, "start?");
        addTransition(3, 2, "stop?");
        addTransition(3, 1, "reset?");
        addTransition(3, 3, "get!0");
        addTransition(3, 5, "i");
        addTransition(4, 4, "start?");
        addTransition(4, 6, "stop?");
        addTransition(4, 7, "reset?");
        addTransition(4, 4, "get!1");
        addTransition(4, 8, "i");
        addTransition(5, 5, "start?");
        addTransition(5, 6, "stop?");
        addTransition(5, 4, "reset?");
        addTransition(5, 5, "get!0");
        addTransition(5, 9, "i");
        addTransition(6, 4, "start?");
        addTransition(6, 6, "stop?");
        addTransition(6, 2, "reset?");
        addTransition(6, 6, "get!1");
        addTransition(6, 6, "i");
        addTransition(7, 7, "start?");
        addTransition(7, 6, "stop?");
        addTransition(7, 4, "reset?");
        addTransition(7, 7, "get!1");
        addTransition(7, 10, "i");
        addTransition(8, 8, "start?");
        addTransition(8, 11, "stop?");
        addTransition(8, 12, "reset?");
        addTransition(8, 8, "get!2");
        addTransition(8, 13, "i");
        addTransition(9, 9, "start?");
        addTransition(9, 11, "stop?");
        addTransition(9, 8, "reset?");
        addTransition(9, 9, "get!0");
        addTransition(9, 14, "i");
        addTransition(10, 10, "start?");
        addTransition(10, 11, "stop?");
        addTransition(10, 8, "reset?");
        addTransition(10, 10, "get!1");
        addTransition(10, 15, "i");
        addTransition(11, 8, "start?");
        addTransition(11, 11, "stop?");
        addTransition(11, 2, "reset?");
        addTransition(11, 11, "get!2");
        addTransition(11, 11, "i");
        addTransition(12, 12, "start?");
        addTransition(12, 11, "stop?");
        addTransition(12, 8, "reset?");
        addTransition(12, 12, "get!2");
        addTransition(12, 16, "i");
        addTransition(13, 13, "start?");
        addTransition(13, 17, "stop?");
        addTransition(13, 18, "reset?");
        addTransition(13, 13, "get!3");
        addTransition(13, 19, "i");
        addTransition(14, 14, "start?");
        addTransition(14, 17, "stop?");
        addTransition(14, 13, "reset?");
        addTransition(14, 14, "get!0");
        addTransition(14, 19, "i");
        addTransition(15, 15, "start?");
        addTransition(15, 17, "stop?");
        addTransition(15, 13, "reset?");
        addTransition(15, 15, "get!1");
        addTransition(15, 19, "i");
        addTransition(16, 16, "start?");
        addTransition(16, 17, "stop?");
        addTransition(16, 13, "reset?");
        addTransition(16, 16, "get!2");
        addTransition(16, 19, "i");
        addTransition(17, 13, "start?");
        addTransition(17, 17, "stop?");
        addTransition(17, 2, "reset?");
        addTransition(17, 17, "get!3");
        addTransition(17, 17, "i");
        addTransition(18, 18, "start?");
        addTransition(18, 17, "stop?");
        addTransition(18, 13, "reset?");
        addTransition(18, 18, "get!3");
        addTransition(18, 19, "i");
    }
}
