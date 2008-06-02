package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class Unregular1 extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X := a.b + c.d.Y[4711]; "
            + "Y[foo] := out!foo | spawn.X; "
            + "X";
    }

    @Override
    protected boolean expectRegularity() {
        return false;
    }

}
