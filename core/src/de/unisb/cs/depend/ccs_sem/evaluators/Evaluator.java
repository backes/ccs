package de.unisb.cs.depend.ccs_sem.evaluators;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public interface Evaluator {
    /**
     * Evaluates a single expression.
     *
     * @param expr the expression to evaluate.
     * @return <code>true</code> on success, <code>false</code> if there was an error
     */
    boolean evaluate(Expression expr)
        throws InterruptedException;

    /**
     * Evaluates an expression and all successor expressions, i.e. expressions
     * that are available through transitions (recursively).
     * @param expr the expression to evaluate
     * @param monitor a monitor that is informed about new states and transitions
     * @return <code>true</code> on success, <code>false</code> if there was an error
     */
    boolean evaluateAll(Expression expr, EvaluationMonitor monitor)
        throws InterruptedException;

}
