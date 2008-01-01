package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;


public class ParameterRefValue implements Value {
    
    private int paramNr;
    
    public ParameterRefValue(int paramNr) {
        this.paramNr = paramNr;
    }

    public String getValue() {
        StackTraceElement topmostStackTraceElement = Thread.currentThread().getStackTrace()[0];
        throw new InternalSystemException(topmostStackTraceElement.getClassName()
            + "." + topmostStackTraceElement.getMethodName()
            + " should never be called.");
    }

    public Value replaceParameters(List<Value> parameters) {
        assert parameters.size() > paramNr;
        
        return parameters.get(paramNr).clone();
    }
    
    public Value insertParameters(List<Value> parameters) {
        return this;
    }

    @Override
    public String toString() {
        return "param#"+paramNr;
    }
    
    @Override
    public Value clone() {
        Value cloned;
        try {
            cloned = (Value) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalSystemException(getClass().getName() + " could not be cloned");
        }
        
        return cloned;
    }

}
