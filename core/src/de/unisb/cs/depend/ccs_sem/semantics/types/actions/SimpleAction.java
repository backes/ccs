package de.unisb.cs.depend.ccs_sem.semantics.types.actions;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.Value;


public class SimpleAction extends Action {

    private final Value name;

    public SimpleAction(Value name) {
        super();
        this.name = name;
    }

    @Override
    public String getLabel() {
        return name.getStringValue();
    }

    @Override
    public Action instantiate(List<Value> parameters) {
        final Value newName = name.instantiate(parameters);
        if (name.equals(newName))
            return this;

        return Action.getAction(new SimpleAction(newName));
    }

    @Override
    public Action insertParameters(List<Parameter> parameters) {
        final Value newName = name.insertParameters(parameters);
        if (name.equals(newName))
            return this;

        return Action.getAction(new SimpleAction(newName));
    }

    @Override
    public String getChannel() {
        return name.getStringValue();
    }

    @Override
    public Value getMessage() {
        return null;
    }

    @Override
    public boolean restricts(Action actionToCheck) {
        return false;
    }

    @Override
    public Expression synchronizeWith(Action otherAction, Expression target) {
        // this action cannot synchronize
        return null;
    }

    @Override
    public Action instantiateInputValue(Value value) {
        final Value newValue = name.instantiateInputValue(value);
        if (newValue.equals(value))
            return this;
        return Action.getAction(new SimpleAction(newValue));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SimpleAction other = (SimpleAction) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
