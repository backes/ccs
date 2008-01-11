package de.unisb.cs.depend.ccs_sem.exporters.helpers;

import java.util.Comparator;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class TransitionsTargetNumberComparator implements
        Comparator<Transition> {

    private final Map<Expression, Integer> stateNumbers;


    public TransitionsTargetNumberComparator(Map<Expression, Integer> stateNumbers) {
        this.stateNumbers = stateNumbers;
    }

    public int compare(Transition t1, Transition t2) {
        assert stateNumbers.containsKey(t1.getTarget())
            && stateNumbers.containsKey(t2.getTarget());

        return stateNumbers.get(t1.getTarget()).compareTo(stateNumbers.get(t2.getTarget()));
    }

}
