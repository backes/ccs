package de.unisb.cs.depend.ccs_sem.evalutators;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public interface Evaluator {
    /**
     * Evaluates a single expression.
     * @param expr the expression to evaluate.
     */
    void evaluate(Expression expr);

    /**
     * Evaluates an expression and all successor expressions, i.e. expressions
     * that are available through transitions (recursively).
     * @param expr
     * @param monitor
     */
    void evaluateAll(Expression expr, EvaluationMonitor monitor);

}
