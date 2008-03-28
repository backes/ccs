package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class WholeActionInRestriction2 extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a!2.a!1.0 \\ {a!1}";
    }

    @Override
    protected int getExpectedParsingErrors() {
        return 1;
    }

    @Override
    protected boolean expectParsedProgram() {
        return false;
    }

}
