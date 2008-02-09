package de.unisb.cs.depend.ccs_sem.junit.integrationtests;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


/**
 * This Expression cannot be minimized.
 *
 * @author Clemens Hammacher
 */
public class MinimizationTest8 extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a.g + b.(c.e + d.f)";
    }

    @Override
    protected void addStates() {
        addState("a.g.0 + b.(c.e.0 + d.f.0)");
        addState("g.0");
        addState("0");
        addState("c.e.0 + d.f.0");
        addState("e.0");
        addState("f.0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a");
        addTransition(0, 3, "b");
        addTransition(1, 2, "g");
        addTransition(3, 4, "c");
        addTransition(3, 5, "d");
        addTransition(4, 2, "e");
        addTransition(5, 2, "f");
    }

    @Override
    protected boolean isMinimize() {
        return true;
    }

    @Override @Test @Ignore
    public void checkStatesExplicitely() {
        super.checkStatesExplicitely();
    }
}
