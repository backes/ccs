package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/*
The CCS program:

in?x:({2, 3, 4, 4711, bla, foo}).one.two.three.out!x.0
*/

public class RangeTest2 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "in?x:{2, 3, 4, 4711, bla, foo}.one.two.three.out!x.0";
    }

    @Override
    protected void addStates() {
        addState("in?x:{2, 3, 4, 4711, bla, foo}.one.two.three.out!x.0");
        addState("one.two.three.out!2.0");
        addState("one.two.three.out!3.0");
        addState("one.two.three.out!4.0");
        addState("one.two.three.out!4711.0");
        addState("two.three.out!2.0");
        addState("two.three.out!3.0");
        addState("two.three.out!4.0");
        addState("two.three.out!4711.0");
        addState("three.out!2.0");
        addState("three.out!3.0");
        addState("three.out!4.0");
        addState("three.out!4711.0");
        addState("out!2.0");
        addState("out!3.0");
        addState("out!4.0");
        addState("out!4711.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "in?2");
        addTransition(0, 2, "in?3");
        addTransition(0, 3, "in?4");
        addTransition(0, 4, "in?4711");
        addTransition(1, 5, "one");
        addTransition(2, 6, "one");
        addTransition(3, 7, "one");
        addTransition(4, 8, "one");
        addTransition(5, 9, "two");
        addTransition(6, 10, "two");
        addTransition(7, 11, "two");
        addTransition(8, 12, "two");
        addTransition(9, 13, "three");
        addTransition(10, 14, "three");
        addTransition(11, 15, "three");
        addTransition(12, 16, "three");
        addTransition(13, 17, "out!2");
        addTransition(14, 17, "out!3");
        addTransition(15, 17, "out!4");
        addTransition(16, 17, "out!4711");
    }
}
