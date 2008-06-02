package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Comp extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a!(3<4?1:0) + b!(3<=4?1:0) + c!(4<4?1:0) + d!(4<=4?1:0) + e!(4<3?1:0) + f!(4<=3?1:0) + " +
            "g!(3>4?1:0) + h!(3>=4?1:0) + j!(4>4?1:0) + k!(4>=4?1:0) + l!(4>3?1:0) + m!(4>=3?1:0)";
    }

    @Override
    protected void addStates() {
        addState("a!1.0 + b!1.0 + c!0.0 + d!1.0 + e!0.0 + f!0.0 + " +
            "g!0.0 + h!0.0 + j!0.0 + k!1.0 + l!1.0 + m!1.0");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!1");
        addTransition(0, 1, "b!1");
        addTransition(0, 1, "c!0");
        addTransition(0, 1, "d!1");
        addTransition(0, 1, "e!0");
        addTransition(0, 1, "f!0");
        addTransition(0, 1, "g!0");
        addTransition(0, 1, "h!0");
        addTransition(0, 1, "j!0");
        addTransition(0, 1, "k!1");
        addTransition(0, 1, "l!1");
        addTransition(0, 1, "m!1");
    }

}
