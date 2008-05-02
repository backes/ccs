package de.unisb.cs.depend.ccs_sem.junit.integrationtests.operators;

import de.unisb.cs.depend.ccs_sem.junit.IntegrationTest;


public class Eq extends IntegrationTest {

    @Override
    protected String getExpressionString() {
        return "X[ch,a,b] := ch!(a==b);\n" +
            "a!(3==4) + b!(0==-0) + c!(1==-1) + d!(true==true) + e!(false==true) + " +
            "f!(a==b) + g!(a==a) + h!(aa==aaa) + " +
            "xa!(3!=4) + xb!(0!=-0) + xc!(1!=-1) + xd!(true!=true) + xe!(false!=true) + " +
            "xf!(a!=b) + xg!(a!=a) + xh!(aa!=aaa) + " +
            "X[foo, 1, 0] + X[bar, 0, 0]";
    }

    @Override
    protected void addStates() {
        addState("a!false.0 + b!true.0 + c!false.0 + d!true.0 + e!false.0 + f!false.0 + " +
            "g!true.0 + h!false.0 + " +
            "xa!true.0 + xb!false.0 + xc!true.0 + xd!false.0 + xe!true.0 + " +
            "xf!true.0 + xg!false.0 + xh!true.0 + " +
            "X[foo, 1, 0] + X[bar, 0, 0]");
        addState("0");
    }

    @Override
    protected void addTransitions() {
        addTransition(0, 1, "a!false");
        addTransition(0, 1, "b!true");
        addTransition(0, 1, "c!false");
        addTransition(0, 1, "d!true");
        addTransition(0, 1, "e!false");
        addTransition(0, 1, "f!false");
        addTransition(0, 1, "g!true");
        addTransition(0, 1, "h!false");
        addTransition(0, 1, "xa!true");
        addTransition(0, 1, "xb!false");
        addTransition(0, 1, "xc!true");
        addTransition(0, 1, "xd!false");
        addTransition(0, 1, "xe!true");
        addTransition(0, 1, "xf!true");
        addTransition(0, 1, "xg!false");
        addTransition(0, 1, "xh!true");
        addTransition(0, 1, "foo!false");
        addTransition(0, 1, "bar!true");
    }

}
