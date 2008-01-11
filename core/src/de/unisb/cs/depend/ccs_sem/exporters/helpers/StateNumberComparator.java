package de.unisb.cs.depend.ccs_sem.exporters.helpers;

import java.util.Comparator;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public class StateNumberComparator implements Comparator<Expression> {

    private final Map<Expression, Integer> stateNumbers;


    public StateNumberComparator(Map<Expression, Integer> stateNumbers) {
        this.stateNumbers = stateNumbers;
    }

    public int compare(Expression e1, Expression e2) {
        assert stateNumbers.containsKey(e1) && stateNumbers.containsKey(e2);

        return stateNumbers.get(e1).compareTo(stateNumbers.get(e2));
    }

}
