package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.TauChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;

public class TauAction extends Action {

	// TODO TAU evaluate syncLeft,syncRight informations
	
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
    public String toString() {
    	StringBuilder strb = new StringBuilder();
    	for( Boolean b : syncedLeft.getLRTrace() ) {
    		strb.append(b ? "r" : "l" );
    	}
    	String left = " ("+syncedLeft.toString()+")" + strb.toString();
    	
    	strb = new StringBuilder();
    	for( Boolean b : syncedRight.getLRTrace() ) {
    		strb.append(b ? "r" : "l" );
    	}
    	String right = " ("+syncedRight.toString()+")" + strb.toString();
    	
    	strb = new StringBuilder();
    	for( Boolean b : getLRTrace() ) {
    		strb.append(b ? "r" : "l" );
    	}
    	
        return "i" + (Expression.getVisibleTau() ? left + right + "-" + strb.toString()
				: "");
    }

	@Override
	protected Action copySubAction() {
		return new TauAction(syncedLeft,syncedRight);
	}    
}
