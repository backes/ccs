package de.unisb.cs.depend.ccs_sem.exporters.helpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class TransitionCounter {
    public static int countTransitions(Expression mainExpr) {

        final Stack<Expression> toCount = new Stack<Expression>();
        toCount.push(mainExpr);

        final Set<Expression> counted = new HashSet<Expression>();
        counted.add(mainExpr);

        int count = 0;

        while (!toCount.isEmpty()) {
            final Expression expr = toCount.pop();

            assert expr.isEvaluated();

            final List<Transition> transitions = expr.getTransitions();
            count += transitions.size();

            for (final Transition trans: transitions) {
                final Expression succ = trans.getTarget();
                if (counted.add(succ))
                    toCount.add(succ);
            }
        }

        return count;
    }
}
