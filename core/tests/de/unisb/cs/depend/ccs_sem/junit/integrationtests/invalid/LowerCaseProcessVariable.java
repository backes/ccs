package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class LowerCaseProcessVariable extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "x := a.0; x";
    }

    @Override
    protected int getExpectedParsingErrors() {
        return 1;
    }

}
