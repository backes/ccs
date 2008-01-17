package de.unisb.cs.depend.ccs_sem.evalutators;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class SequentialEvaluator implements Evaluator {

    public void evaluate(Expression expr) {
        if (expr.isEvaluated())
            return;

        for (final Expression child: expr.getChildren()) {
            evaluate(child);
        }

        expr.evaluate();
    }

    public void evaluateAll(Expression expr, EvaluationMonitor monitor) {
        final Queue<Expression> toEvaluate = new ArrayDeque<Expression>();
        toEvaluate.add(expr);

        final Set<Expression> seen = new HashSet<Expression>();
        seen.add(expr);

        while (!toEvaluate.isEmpty()) {
            final Expression e = toEvaluate.poll();
            if (monitor != null)
                monitor.newState();
            evaluate(e);
            if (monitor != null)
                monitor.newTransitions(e.getTransitions().size());
            for (final Transition trans: e.getTransitions()) {
                final Expression succ = trans.getTarget();
                if (seen.add(succ))
                    toEvaluate.add(succ);
            }
        }

        if (monitor != null)
            monitor.ready();
    }

}
