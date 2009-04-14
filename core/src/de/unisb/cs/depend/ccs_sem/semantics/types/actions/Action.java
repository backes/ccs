package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.LinkedList;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.LazyCreatedMap;

public abstract class Action implements Comparable<Action> {

	private LinkedList<Boolean> leftRightTrace;
	
	/**
	 * Important for the white-box tau semantics
	 * @param b - the added direction, false = left; true = right
	 */
	public void addToLRTrace(boolean b) {
		leftRightTrace.addLast(b);
	}
	
	public void resetLRTrace() {
		leftRightTrace = null;
	}
	
	public String getLRTrace() {
		StringBuilder strB = new StringBuilder();
		for(int i=leftRightTrace.size()-1; i>=0; i--) {
			strB.append(
					leftRightTrace.get(i) ? "r" : "l"
					);
		}
		
		return strB.toString();
	}
	
	public Action() {
		leftRightTrace = new LinkedList<Boolean> ();	
	}
	
    public abstract String getLabel();

    public abstract Channel getChannel();

    public abstract Value getValue();

    public abstract Action instantiate(Map<Parameter, Value> parameters) throws ArithmeticError;

    // toString() has to be overwritten in subclasses!
    @Override
    public abstract String toString();

    /**
     * See {@link Transition#synchronizeWith(Action)}
     * @param otherAction the Action that we want to synchronize with
     * @param target Expression before synchronizing
     * @return <code>null</code> if we can't synchronize, otherwise either the
     *         Expression target or a new one that's instantiated using otherAction
     */
    public abstract Expression synchronizeWith(Action otherAction, Expression target);

    public int compareTo(Action o) {
        return getLabel().compareTo(o.getLabel());
    }

    @Override
    public final boolean equals(Object obj) {
        return equals(obj, new LazyCreatedMap<ParameterOrProcessEqualsWrapper, Integer>(4));
    }

    public abstract boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences);

    @Override
    public final int hashCode() {
        return hashCode(new LazyCreatedMap<ParameterOrProcessEqualsWrapper, Integer>(4));
    }

    public abstract int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences);

    public Action copy() {
    	Action a = copySubAction();
    	
    	// copy trace
    	try {
    		a.leftRightTrace = new LinkedList<Boolean> ();
    		a.leftRightTrace.addAll(leftRightTrace);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return a;
    }
    
    protected abstract Action copySubAction();
}
