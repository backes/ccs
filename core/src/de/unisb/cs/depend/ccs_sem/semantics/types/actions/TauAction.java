package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.TauChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class TauAction extends Action {

    private static TauAction instance = null;

    private final Channel channel;

    private TauAction() {
        channel = TauChannel.get();
    }

    @Override
    public String getLabel() {
        return "i";
    }

    public static TauAction get() {
        if (instance == null)
            instance = new TauAction();

        return instance;
    }

    @Override
    public Action instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    @Override
    public Action insertParameters(List<Parameter> parameters) {
        return this;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public Value getMessage() {
        return null;
    }

    @Override
    public boolean isInputAction() {
        return true;
    }

    @Override
    public boolean isOutputAction() {
        return true;
    }

    @Override
    public boolean restricts(Action actionToCheck) {
        // this method should not be called because TauActions cannot be used in
        // RestrictExpressions
        assert false;

        return false;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        if (otherAction instanceof TauAction)
            return target;

        return null;
    }

    @Override
    public Expression manipulateTarget(Expression target) {
        return target;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TauAction;
    }

}
