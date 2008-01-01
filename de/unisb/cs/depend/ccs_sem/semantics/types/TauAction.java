package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;


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
    public boolean isCounterTransition(Action action) {
        return action instanceof TauAction;
    }
    
    @Override
    public Action replaceParameters(List<Value> parameters) {
        return this;
    }

    @Override
    public Action insertParameters(List<Value> parameters) {
        // TODO Auto-generated method stub
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
