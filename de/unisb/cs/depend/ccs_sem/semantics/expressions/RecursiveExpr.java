package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class RecursiveExpr extends AbstractExpression {
    
    private Declaration referencedDeclaration;
    private List<String> parameters;

    public RecursiveExpr(Declaration referencedDeclaration, List<String> parameters) {
        super();
        this.referencedDeclaration = referencedDeclaration;
        this.parameters = parameters;
    }

    /**
     * Note: The returned list must not be changed!
     * @return
     */
    public List<String> getParameters() {
        return parameters;
    }
    
    public Declaration getReferencedDeclaration() {
        return referencedDeclaration;
    }

    public Collection<Expression> getChildren() {
        return Collections.emptySet();
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
            return referencedDeclaration.getName();
        
        StringBuilder sb = new StringBuilder(referencedDeclaration.getName());
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

}
