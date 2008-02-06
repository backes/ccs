package de.unisb.cs.depend.ccs_sem.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class StateNumerator {

    // caching
    private static Expression lastExpression = null;
    private static int lastStartIndex;
    private static Map<Expression, Integer> lastStateNumbers;

    /**
     * Numerates the graph built by the given Expression. The starting expression
     * always gets the startIndex, the other Expressions are numbered in a BFS manner.
     * @param mainExpression the starting expression of the graph to numerate
     * @param startIndex
     * @return a mapping from Expression to corresponding state number
     */
    public static Map<Expression, Integer> numerateStates(
            Expression mainExpression, int startIndex) {

        if (mainExpression.equals(lastExpression) && startIndex == lastStartIndex)
            return lastStateNumbers;

        final Map<Expression, Integer> numbers = new HashMap<Expression, Integer>();
        numbers.put(mainExpression, startIndex);

        final Queue<Expression> numerateSuccessors = new LinkedList<Expression>();
        numerateSuccessors.add(mainExpression);

        int nextIndex = startIndex;

        while (!numerateSuccessors.isEmpty()) {
            final Expression expr = numerateSuccessors.poll();

            assert expr.isEvaluated();
            for (final Transition trans: expr.getTransitions()) {
                final Expression succ = trans.getTarget();
                if (!numbers.containsKey(succ)) {
                    numbers.put(succ, ++nextIndex);
                    numerateSuccessors.add(succ);
                }
            }
        }

        lastExpression = mainExpression;
        lastStartIndex = startIndex;
        lastStateNumbers = numbers;

        return numbers;
    }

    public static Map<Expression, Integer> numerateStates(Expression mainExpression) {
        return numerateStates(mainExpression, 0);
    }
}
