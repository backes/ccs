package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class WrongParameterRecursive1 extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] = out!(a+1); Y[a] = foo.X[a]; Y[true]";
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
