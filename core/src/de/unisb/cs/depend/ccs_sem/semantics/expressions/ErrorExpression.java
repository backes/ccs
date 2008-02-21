package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class ErrorExpression extends Expression {

    private static ErrorExpression instance;

    private ErrorExpression() {
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
    public Expression instantiate(Map<Parameter, Value> parameters) {
        return this;
    }

    @Override
    protected boolean isError0() {
        return true;
    }

    @Override
    public String toString() {
        return "ERROR";
    }

    @Override
    protected int hashCode0() {
        return 11;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ErrorExpression;
    }

    public static ErrorExpression get() {
        if (instance == null)
            instance = new ErrorExpression();
        return instance;
    }

}
