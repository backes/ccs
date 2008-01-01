package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public class StopExpr extends Expression {
    
    public StopExpr() {
        super();
    }
    
    @Override
    public Collection<Expression> getChildren() {
        return Collections.emptySet();
    }

    @Override
    protected List<Transition> evaluate0() {
        return Collections.emptyList();
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) {
        return this;
    }

    @Override
    public Expression replaceParameters(List<Value> parameters) {
        return this;
    }
    
    @Override
    public Expression insertParameters(List<Value> parameters) {
        return this;
    }

    @Override
    public String toString() {
        return "0";
    }
    
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StopExpr;
    }

}
