package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.Value;


public class TauAction extends Action {

    private static TauAction instance = null;

    private TauAction() {
        // private constructor, nothing to do
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
    public Action instantiate(List<Value> parameters) {
        return this;
    }

    @Override
    public Action insertParameters(List<Parameter> parameters) {
        return this;
    }

    @Override
    public String getChannel() {
        return "i";
    }

    @Override
    public Value getMessage() {
        return null;
    }

    @Override
    public Action instantiateInputValue(Value value) {
        return this;
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
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TauAction;
    }

}
