package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class OperatorArgumentType5 extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "a!(false && x)";
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
