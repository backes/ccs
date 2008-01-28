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
        addState("one.two.three.out!bla.0");
        addState("one.two.three.out!foo.0");
        addState("two.three.out!2.0");
        addState("two.three.out!3.0");
        addState("two.three.out!4.0");
        addState("two.three.out!4711.0");
        addState("two.three.out!bla.0");
        addState("two.three.out!foo.0");
        addState("three.out!2.0");
        addState("three.out!3.0");
        addState("three.out!4.0");
        addState("three.out!4711.0");
        addState("three.out!bla.0");
        addState("three.out!foo.0");
        addState("out!2.0");
        addState("out!3.0");
        addState("out!4.0");
        addState("out!4711.0");
        addState("out!bla.0");
        addState("out!foo.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "in?2");
        addTransition(0, 2, "in?3");
        addTransition(0, 3, "in?4");
        addTransition(0, 4, "in?4711");
        addTransition(0, 5, "in?bla");
        addTransition(0, 6, "in?foo");
        addTransition(1, 7, "one");
        addTransition(2, 8, "one");
        addTransition(3, 9, "one");
        addTransition(4, 10, "one");
        addTransition(5, 11, "one");
        addTransition(6, 12, "one");
        addTransition(7, 13, "two");
        addTransition(8, 14, "two");
        addTransition(9, 15, "two");
        addTransition(10, 16, "two");
        addTransition(11, 17, "two");
        addTransition(12, 18, "two");
        addTransition(13, 19, "three");
        addTransition(14, 20, "three");
        addTransition(15, 21, "three");
        addTransition(16, 22, "three");
        addTransition(17, 23, "three");
        addTransition(18, 24, "three");
        addTransition(19, 25, "out!2");
        addTransition(20, 25, "out!3");
        addTransition(21, 25, "out!4");
        addTransition(22, 25, "out!4711");
        addTransition(23, 25, "out!bla");
        addTransition(24, 25, "out!foo");
    }
}
