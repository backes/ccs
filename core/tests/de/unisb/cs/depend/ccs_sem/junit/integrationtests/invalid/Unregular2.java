package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class Unregular2 extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] := out!a | Y[true] | Y[false]; "
            + "Y[n] := uepsilon!n . when n (fertisch | X[n ? 17 : 71]); "
            + "X[0]";
    }

    @Override
    protected boolean expectRegularity() {
        return false;
    }

}
