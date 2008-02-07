package de.unisb.cs.depend.ccs_sem.evaluators;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class SequentialEvaluator implements Evaluator {

    public boolean evaluate(Expression expr) {
        if (expr.isEvaluated())
            return true;

        for (final Expression child: expr.getChildren()) {
            evaluate(child);
        }

        expr.evaluate();
        return true;
    }

    public boolean evaluateAll(Expression expr, EvaluationMonitor monitor) {
        final Stack<Expression> toEvaluate = new Stack<Expression>();
        toEvaluate.add(expr);

        final Set<Expression> seen = new HashSet<Expression>();
        seen.add(expr);

        while (!toEvaluate.isEmpty()) {
            final Expression e = toEvaluate.peek();
            if (monitor != null)
                monitor.newState();
            final int stackSizeBefore = toEvaluate.size();
            for (final Expression child: e.getChildren())
                if (!child.isEvaluated())
                    toEvaluate.push(child);
            if (stackSizeBefore != toEvaluate.size())
                continue;
            toEvaluate.pop();
            e.evaluate();
            if (monitor != null)
                monitor.newTransitions(e.getTransitions().size());
            for (final Transition trans: e.getTransitions()) {
                final Expression succ = trans.getTarget();
                if (seen.add(succ))
                    toEvaluate.push(succ);
            }
        }

        if (monitor != null)
            monitor.ready();

        return true;
    }

}
