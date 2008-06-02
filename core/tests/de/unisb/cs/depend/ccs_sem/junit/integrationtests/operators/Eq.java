package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Eq extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch,a,b] := ch!(a==b?1:0);\n" +
            "a!(3==4?1:0) + b!(0==-0?1:0) + c!(1==-1?1:0) + d!(true==true?1:0) + e!(false==true?1:0) + " +
            "f!(a==b?1:0) + g!(a==a?1:0) + h!(aa==aaa?1:0) + " +
            "xa!(3!=4?1:0) + xb!(0!=-0?1:0) + xc!(1!=-1?1:0) + xd!(true!=true?1:0) + xe!(false!=true?1:0) + " +
            "xf!(a!=b?1:0) + xg!(a!=a?1:0) + xh!(aa!=aaa?1:0) + " +
            "X[foo, 1, 0] + X[bar, 0, 0]";
    }

    @Override
    protected void addStates() {
        addState("a!0.0 + b!1.0 + c!0.0 + d!1.0 + e!0.0 + f!0.0 + " +
            "g!1.0 + h!0.0 + " +
            "xa!1.0 + xb!0.0 + xc!1.0 + xd!0.0 + xe!1.0 + " +
            "xf!1.0 + xg!0.0 + xh!1.0 + " +
            "X[foo, 1, 0] + X[bar, 0, 0]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!0");
        addTransition(0, 1, "b!1");
        addTransition(0, 1, "c!0");
        addTransition(0, 1, "d!1");
        addTransition(0, 1, "e!0");
        addTransition(0, 1, "f!0");
        addTransition(0, 1, "g!1");
        addTransition(0, 1, "h!0");
        addTransition(0, 1, "xa!1");
        addTransition(0, 1, "xb!0");
        addTransition(0, 1, "xc!1");
        addTransition(0, 1, "xd!0");
        addTransition(0, 1, "xe!1");
        addTransition(0, 1, "xf!1");
        addTransition(0, 1, "xg!0");
        addTransition(0, 1, "xh!1");
        addTransition(0, 1, "foo!0");
        addTransition(0, 1, "bar!1");
    }

}
