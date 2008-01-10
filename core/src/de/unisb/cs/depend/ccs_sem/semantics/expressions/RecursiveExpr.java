package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public class RecursiveExpr extends Expression {

    private final Declaration referencedDeclaration;
    private List<Value> parameters;
    private final Expression newExpression;

    public RecursiveExpr(Declaration referencedDeclaration, List<Value> parameters) {
        super();
        this.referencedDeclaration = referencedDeclaration;
        this.parameters = parameters;
        newExpression = referencedDeclaration.getValue().instantiate(parameters);
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

    public Expression getNewExpression() {
        return newExpression;
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(newExpression);
    }

    @Override
    protected List<Transition> evaluate0() {
        return newExpression.getTransitions();
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) {
        // nothing to replace here
        return this;
    }

    @Override
    public Expression instantiate(List<Value> params) {
        final List<Value> newParameters = new ArrayList<Value>(parameters.size());
        boolean changed = false;
        for (final Value param: parameters) {
            final Value newParam = param.instantiate(parameters);
            if (!changed && !newParam.equals(param))
                changed = true;
            newParameters.add(newParam);
        }

        if (!changed)
            return this;

        return Expression.getExpression(new RecursiveExpr(referencedDeclaration, newParameters));
    }

    @Override
    public Expression insertParameters(List<Value> params) {
        final List<Value> newParameters = new ArrayList<Value>(parameters.size());
        boolean changed = false;
        for (final Value param: parameters) {
            final Value newParam = param.insertParameters(parameters);
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

        final StringBuilder sb = new StringBuilder(referencedDeclaration.getName());
        sb.append('[');
        for (int i = 0; i < parameters.size(); ++i)
            sb.append(i>0 ? "," : "").append(parameters.get(i));
        sb.append(']');

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((referencedDeclaration == null) ? 0 : referencedDeclaration.hashCode());
        result = PRIME * result + ((parameters == null) ? 0 : parameters.hashCode());
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
        if (referencedDeclaration == null) {
            if (other.referencedDeclaration != null)
                return false;
        } else if (!referencedDeclaration.equals(other.referencedDeclaration))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        return true;
    }

    @Override
    public Expression clone() {
        final RecursiveExpr cloned = (RecursiveExpr) super.clone();
        cloned.parameters = new ArrayList<Value>(parameters);

        // referencedDeclaration doesn't have to be cloned

        return cloned;
    }

}
