package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Comp extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a!(3<4) + b!(3<=4) + c!(4<4) + d!(4<=4) + e!(4<3) + f!(4<=3) + " +
            "g!(3>4) + h!(3>=4) + j!(4>4) + k!(4>=4) + l!(4>3) + m!(4>=3)";
    }

    @Override
    protected void addStates() {
        addState("a!true.0 + b!true.0 + c!false.0 + d!true.0 + e!false.0 + f!false.0 + " +
            "g!false.0 + h!false.0 + j!false.0 + k!true.0 + l!true.0 + m!true.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!true");
        addTransition(0, 1, "b!true");
        addTransition(0, 1, "c!false");
        addTransition(0, 1, "d!true");
        addTransition(0, 1, "e!false");
        addTransition(0, 1, "f!false");
        addTransition(0, 1, "g!false");
        addTransition(0, 1, "h!false");
        addTransition(0, 1, "j!false");
        addTransition(0, 1, "k!true");
        addTransition(0, 1, "l!true");
        addTransition(0, 1, "m!true");
    }

}
