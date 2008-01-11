package de.unisb.cs.depend.ccs_sem.exporters.helpers;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class StateNumerator {

    public static Map<Expression, Integer> numerateStates(
            Expression mainExpression) {

        final Map<Expression, Integer> numbers = new HashMap<Expression, Integer>();

        final Queue<Expression> toNumerate = new ArrayDeque<Expression>();
        toNumerate.add(mainExpression);

        while (!toNumerate.isEmpty()) {
            final Expression expr = toNumerate.poll();
            numbers.put(expr, numbers.size());

            assert expr.isEvaluated();
            for (final Transition trans: expr.getTransitions()) {
                final Expression succ = trans.getTarget();
                if (!numbers.containsKey(succ))
                    toNumerate.add(succ);
            }
        }

        return numbers;
    }
}
