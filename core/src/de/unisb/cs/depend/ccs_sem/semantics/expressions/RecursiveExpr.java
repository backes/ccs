package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public class RecursiveExpr extends Expression {
    
    private Declaration referencedDeclaration;
    private List<Value> parameters;

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

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(referencedDeclaration.getValue());
    }

    @Override
    protected List<Transition> evaluate0() {
        // generate the new expression to evaluate, then delegate to it
        Expression newExpr = referencedDeclaration.getValue().clone();
        newExpr = newExpr.replaceParameters(parameters);
        newExpr = Expression.getExpression(newExpr);

        return newExpr.evaluate();
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) {
        // nothing to replace here
        return this;
    }
    
    @Override
    public Expression replaceParameters(List<Value> params) {
        List<Value> newParameters = new ArrayList<Value>(parameters.size());
        boolean changed = false;
        for (Value param: parameters) {
            Value newParam = param.replaceParameters(parameters);
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
        List<Value> newParameters = new ArrayList<Value>(parameters.size());
        boolean changed = false;
        for (Value param: parameters) {
            Value newParam = param.replaceParameters(parameters);
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
        
        StringBuilder sb = new StringBuilder(referencedDeclaration.getName());
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
        RecursiveExpr cloned = (RecursiveExpr) super.clone();
        cloned.parameters = new ArrayList<Value>(parameters);

        // referencedDeclaration doesn't have to be cloned

        return cloned;
    }

}
