package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class RecursiveExpr extends AbstractExpression {
    
    private Declaration decl;
    private List<String> parameters;

    public RecursiveExpr(Declaration decl, List<String> parameters) {
        super();
        this.decl = decl;
        this.parameters = parameters;
    }

    public Collection<Expression> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<Transition> evaluate0() {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression replaceRecursion(List<Declaration> declarations) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String toString() {
        if (parameters.size() == 0)
            return decl.getName();
        
        StringBuilder sb = new StringBuilder(decl.getName());
        sb.append('[');
        for (int i = 0; i < parameters.size(); ++i)
            sb.append(i==0 ? "," : "").append(parameters.get(i));
        sb.append(']');

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((decl == null) ? 0 : decl.hashCode());
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
        if (decl == null) {
            if (other.decl != null)
                return false;
        } else if (!decl.equals(other.decl))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        return true;
    }

}
