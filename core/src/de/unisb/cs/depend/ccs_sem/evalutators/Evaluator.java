package de.unisb.cs.depend.ccs_sem.evalutators;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public interface Evaluator {
    void evaluate(Expression expr);
}
