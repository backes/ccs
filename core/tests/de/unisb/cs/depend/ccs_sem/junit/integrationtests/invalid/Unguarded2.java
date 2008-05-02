package de.unisb.cs.depend.ccs_sem.junit.integrationtests.invalid;


import de.unisb.cs.depend.ccs_sem.junit.FailingIntegrationTest;


public class Unguarded2 extends FailingIntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[a] := X[a+1]; X[0]";
    }

    @Override
    protected boolean expectGuardedness() {
        return false;
    }

}
