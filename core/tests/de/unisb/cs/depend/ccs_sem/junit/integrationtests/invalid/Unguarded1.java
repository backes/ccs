package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class Unguarded1 extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] = out!a . X[a+1] | Y[a+1]; "
            + "Y[n] = uepsilon!n . fertisch | X[2*n]; "
            + "X[0]";
    }

    @Override
    protected boolean expectGuardedness() {
        return false;
    }

    @Override
    protected boolean expectRegularity() {
        return false;
    }

}
