package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpr;


public class Declaration {
    
    private String name;
    private List<String> parameters;
    private Expression value;
    private List<Declaration> declarationsReachableThroughStaticOperators;
    
    public Declaration(String name, List<String> parameters, Expression value) {
        super();
        this.name = name;
        this.parameters = parameters;
        this.value = value;
    }

    /**
     * A Declaration is regular iff it does not contain a cycle of recursions
     * that contains parallel or restriction operators (so called "static" operators).
     */
    public boolean isRegular() {
        Set<Declaration> checked = new HashSet<Declaration>();
        Set<Declaration> emptySet = Collections.singleton(this);
        return checkRegularity(value, emptySet, false);
    }

    private boolean checkRegularity(Expression expr, Set<Declaration> evilDeclarations, boolean passedStaticOperator) {
        if (!passedStaticOperator && (expr instanceof ParallelExpr || expr instanceof RestrictExpr)) {
            passedStaticOperator = true;
        }
        if (expr instanceof RecursiveExpr) {
            RecursiveExpr recExpr = (RecursiveExpr) expr;
            if (passedStaticOperator)
        }
        // TODO Auto-generated method stub
        return false;
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

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((declarationsReachableThroughStaticOperators == null) ? 0 : declarationsReachableThroughStaticOperators.hashCode());
        result = PRIME * result + ((name == null) ? 0 : name.hashCode());
        result = PRIME * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = PRIME * result + ((value == null) ? 0 : value.hashCode());
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
        final Declaration other = (Declaration) obj;
        if (declarationsReachableThroughStaticOperators == null) {
            if (other.declarationsReachableThroughStaticOperators != null)
                return false;
        } else if (!declarationsReachableThroughStaticOperators.equals(other.declarationsReachableThroughStaticOperators))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
