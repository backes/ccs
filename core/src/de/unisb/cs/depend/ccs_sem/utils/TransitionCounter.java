package de.unisb.cs.depend.ccs_sem.utils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class TransitionCounter {
    // caching
    private static Expression lastExpression = null;
    private static int lastCount;

    public static int countTransitions(Expression mainExpr) {

        if (mainExpr.equals(lastExpression))
            return lastCount;

        final Queue<Expression> toCount = new LinkedList<Expression>();
        toCount.add(mainExpr);

        final Set<Expression> counted = new HashSet<Expression>();
        counted.add(mainExpr);

        int count = 0;

        while (!toCount.isEmpty()) {
            final Expression expr = toCount.poll();

            assert expr.isEvaluated();

            final List<Transition> transitions = expr.getTransitions();
            count += transitions.size();

            for (final Transition trans: transitions) {
                final Expression succ = trans.getTarget();
                if (counted.add(succ))
                    toCount.add(succ);
            }
        }

        lastExpression = mainExpr;
        lastCount = count;

        return count;
    }
}
