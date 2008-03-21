package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class WrongParameter1b extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[channel,message] = channel!message.do.something.X[channel,channel]; "
            + "X[out,0]";
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
