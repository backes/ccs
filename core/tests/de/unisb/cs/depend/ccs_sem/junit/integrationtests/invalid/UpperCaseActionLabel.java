package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class UpperCaseActionLabel extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a.B.c.0";
    }

    @Override
    protected int getExpectedParsingErrors() {
        return 2; // unexpected "." after "B", and "B" is not defined
    }

    @Override
    protected boolean expectParsedProgram() {
        return false;
    }

}
