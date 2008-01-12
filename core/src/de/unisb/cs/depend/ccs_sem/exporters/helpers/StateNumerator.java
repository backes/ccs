package de.unisb.cs.depend.ccs_sem.exporters.helpers;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class StateNumerator {

    /**
     * Numerates the graph built by the given Expression. The starting expression
     * always gets the startIndex, the other Expressions are numbered in a BFS manner.
     * @param mainExpression the starting expression of the graph to numerate
     * @param startIndex
     * @return a mapping from Expression to corresponding state number
     */
    public static Map<Expression, Integer> numerateStates(
            Expression mainExpression, int startIndex) {

        final Map<Expression, Integer> numbers = new HashMap<Expression, Integer>();

        final Queue<Expression> toNumerate = new ArrayDeque<Expression>();
        toNumerate.add(mainExpression);

        int nextIndex = startIndex;

        while (!toNumerate.isEmpty()) {
            final Expression expr = toNumerate.poll();
            numbers.put(expr, nextIndex++);

            assert expr.isEvaluated();
            for (final Transition trans: expr.getTransitions()) {
                final Expression succ = trans.getTarget();
                if (!numbers.containsKey(succ))
                    toNumerate.add(succ);
            }
        }

        return numbers;
    }

    public static Map<Expression, Integer> numerateStates(Expression mainExpression) {
        return numerateStates(mainExpression, 0);
    }
}
