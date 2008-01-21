package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class RecursiveExpr extends Expression {

    private final Declaration referencedDeclaration;
    private final List<Value> parameters;
    private Expression instantiatedExpression = null;

    public RecursiveExpr(Declaration referencedDeclaration, List<Value> parameters) {
        super();
        this.referencedDeclaration = referencedDeclaration;
        this.parameters = parameters;
    }

    /**
     * Note: The returned list must not be changed!
     */
    public List<Value> getParameters() {
        return parameters;
    }

    public Declaration getReferencedDeclaration() {
        return referencedDeclaration;
    }

    public Expression getInstantiatedExpression() {
        if (instantiatedExpression == null)
            instantiatedExpression = referencedDeclaration.instantiate(parameters);

        return instantiatedExpression;
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(getInstantiatedExpression());
    }

    @Override
    protected List<Transition> evaluate0() {
        return getInstantiatedExpression().getTransitions();
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) {
        // nothing to do here
        return this;
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> params) {
        final List<Value> newParameters = new ArrayList<Value>(parameters.size());
        boolean changed = false;
        for (final Value param: parameters) {
            final Value newParam = param.instantiate(params);
            if (!changed && !newParam.equals(param))
                changed = true;
            newParameters.add(newParam);
        }

        if (!changed)
            return this;

        return Expression.getExpression(new RecursiveExpr(referencedDeclaration, newParameters));
    }

    @Override
    public String toString() {
        if (parameters.size() == 0)
            return referencedDeclaration.getName();

        return referencedDeclaration.getName() + parameters;
    }

    @Override
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 5;
        result = PRIME * result + referencedDeclaration.hashCode();
        result = PRIME * result + parameters.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RecursiveExpr other = (RecursiveExpr) obj;
        if (!referencedDeclaration.equals(other.referencedDeclaration))
            return false;
        if (!parameters.equals(other.parameters))
            return false;
        return true;
    }

}
