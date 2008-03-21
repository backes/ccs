package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class ProcessVariableAsAction extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X = 0; a.X.0";
    }

    @Override
    protected int getExpectedParsingErrors() {
        return 1;
    }

}
