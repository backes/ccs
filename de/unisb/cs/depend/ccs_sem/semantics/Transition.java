package de.unisb.cs.depend.ccs_sem.semantics;

import com.sun.corba.se.spi.orbutil.fsm.Action;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public class Transition {
    private Action action;
    private Expression target;
    
    public Transition(Action action, Expression target) {
        super();
        this.action = action;
        this.target = target;
    }
    
    @Override
    public String toString() {
        return "--" + action + "-> " + target;
    }

}
