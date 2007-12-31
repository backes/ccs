package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.Transition;


public interface Expression {
    List<Transition> evaluate();

    Collection<Expression> getChildren();
}
