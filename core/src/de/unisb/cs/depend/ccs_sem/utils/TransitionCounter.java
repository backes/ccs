package de.unisb.cs.depend.ccs_sem.utils;

import java.util.List;
import java.util.Queue;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class TransitionCounter {
    // caching
    private static Expression lastExpression = null;
    private static int lastCount;

    private TransitionCounter() {
        // prevend from instantiation
    }

    public static int countTransitions(Expression mainExpr) {

        synchronized (TransitionCounter.class) {
            if (mainExpr.equals(lastExpression))
                return lastCount;
        }

        final Queue<Expression> toCount = new UniqueQueue<Expression>();
        toCount.add(mainExpr);

        int count = 0;

        while (!toCount.isEmpty()) {
            final Expression expr = toCount.poll();

            assert expr.isEvaluated();

            final List<Transition> transitions = expr.getTransitions();
            count += transitions.size();

            for (final Transition trans: transitions) {
                toCount.add(trans.getTarget());
            }
        }

        synchronized (TransitionCounter.class) {
            lastExpression = mainExpr;
            lastCount = count;
        }

        return count;
    }
}
