package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public class Declaration {
    private String name;
    private List<String> parameters;
    private Expression value;
    
    public Declaration(String name, List<String> parameters, Expression value) {
        super();
        this.name = name;
        this.parameters = parameters;
        this.value = value;
    }

    /**
     * A Declaration is regular iff its value is regular.
     * See {@link Expression#isRegular()}. 
     */
    public boolean isRegular() {
        return value.isRegular();
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public Expression getValue() {
        return value;
    }

}
