package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;


public class ConstantValue implements Value {
    
    private String value;

    public ConstantValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Value replaceParameters(List<Value> parameters) {
        return this;
    }

}
