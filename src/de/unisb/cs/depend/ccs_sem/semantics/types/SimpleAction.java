package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;


public class SimpleAction extends Action {

    private Value name;

    public SimpleAction(Value name) {
        super();
        this.name = name;
    }

    @Override
    public String getLabel() {
        return name.getValue();
    }
    
    @Override
    public boolean isCounterTransition(Action action) {
        if (!(action instanceof SimpleAction))
            return false;
        
        SimpleAction simAct = (SimpleAction) action;
        
        return simAct.name.equals(name);
    }

    @Override
    public Action replaceParameters(List<Value> parameters) {
        name = name.replaceParameters(parameters);

        return this;
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
    
    @Override
    public Action clone() {
        SimpleAction cloned = (SimpleAction) super.clone();
        cloned.name = name.clone();

        return cloned;
    }

    @Override
    public Action insertParameters(List<Value> parameters) {
        name = name.insertParameters(parameters);
        
        return this;
    }

}
