package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class InputVariableAsChannel extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "P[in,out]:=in?b:(0..9).b!out.P[in,out];\n" +
            "P[in,out]";
    }

    @Override
    protected int getExpectedParsingErrors() {
        return 1;
    }

    @Override
    protected boolean expectParsedProgram() {
        return true;
    }

}
