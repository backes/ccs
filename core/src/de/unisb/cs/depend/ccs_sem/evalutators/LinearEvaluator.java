package de.unisb.cs.depend.ccs_sem.evalutators;

import java.util.Collection;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public class LinearEvaluator implements Evaluator {

    public void evaluate(Expression expr) {
        Collection<Expression> children = expr.getChildren();
        
        for (Expression child: children) {
            child.evaluate();
        }

        expr.evaluate();
    }

}
