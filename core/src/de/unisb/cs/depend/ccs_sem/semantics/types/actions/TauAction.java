package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.TauChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;

public class TauAction extends Action {
	
    private static TauAction instance = new TauAction();

    private final Channel channel;
    private final Action syncedLeft;
    private final Action syncedRight;
    
    private TauAction() {
    	channel = TauChannel.get();
    	syncedLeft = null;
    	syncedRight = null;
    }
    
    public TauAction(Action syncedLeft, Action syncedRight) {
    	if( syncedLeft == null || syncedRight == null )
    		throw new IllegalArgumentException("Tau Action mit null Argument aufgerufen!");
    	channel = TauChannel.get();
    	this.syncedLeft = syncedLeft;
    	this.syncedRight = syncedRight;
    }

    @Override
    public String getLabel() {
        return "i"; // TODO TAU deny if semantic is switched to white-box semantic
    }

    public static TauAction get(Action syncedLeft, Action syncedRight) {
        return Expression.getVisibleTau() ? (new TauAction(syncedLeft,syncedRight)) : instance;
    }

    @Override
    public Action instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        return null;
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        return 11;
    }

    @Override
    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
    	return (syncedLeft == null || syncedRight == null || obj == null) ?
    		obj == this :
    		(obj.getClass() == TauAction.class) ?
    				syncedLeft == ((TauAction) obj).syncedLeft &&
    					syncedRight == ((TauAction) obj).syncedRight
    				: false;
    }

    @Override
    public String toString() { // TODO TAU implement
    	if( syncedLeft == null || syncedRight==null || !Expression.getVisibleTau()) {
    		return "i";
    	}
    	
    	if( !Expression.isLeftRightMapGenerated() )
    		throw new IllegalStateException("Left-right map not initialized.");
    	
    	int left  = Expression.getProcessNumber(getLeftLRTrace());
    	int right = Expression.getProcessNumber(getRightLRTrace());
    	if( left == -1 || right == -1 ) {
    		throw new IllegalStateException("Neither left nor right is allowed to be -1");
    	}
    	String label = "";
    	OutputAction outA = null;
    	if( syncedLeft instanceof OutputAction ) {
    		outA = (OutputAction) syncedLeft;
    	} else {
    		outA = (OutputAction) syncedRight;
    	}
    	label = outA.getLabel();
    	
        return "{P"+ left + ",P" + right + "} i ( "+label+" )";
    }

	@Override
	protected Action copySubAction() {
		return new TauAction(syncedLeft,syncedRight);
	}
	
	public String getLeftLRTrace() {
		return super.getLRTrace()+syncedLeft.getLRTrace();
	}
	
	public String getRightLRTrace() {
		return super.getLRTrace()+syncedRight.getLRTrace();
	}
}
