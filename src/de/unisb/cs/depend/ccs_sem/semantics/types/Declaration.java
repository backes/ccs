package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpr;


public class Declaration {
    
    private String name;
    private List<Value> parameters;
    private Expression value;
    
    public Declaration(String name, List<Value> parameters, Expression value) {
        super();
        this.name = name;
        this.parameters = parameters;
        this.value = Expression.getExpression(value.clone().insertParameters(parameters));
    }

    /**
     * A Declaration is regular iff it does not contain a cycle of recursions
     * back to itself that contains parallel or restriction operators (so called
     * "static" operators).
     * Besides, the value of the declaration must not be the recursion variable
     * itself.
     */
    public boolean isRegular() {
        // check the second condition first (easier)
        if (value instanceof RecursiveExpr) {
            RecursiveExpr recExpr = (RecursiveExpr) value;
            if (recExpr.getReferencedDeclaration().equals(this))
                return false;
        }
        
        // then, check for a recursive loop back to this declaration, that
        // contains parallel or restriction operator(s)
        
        // every expression has to be checked only once
        Set<Expression> checked = new HashSet<Expression>();
        // a queue of expressions to check
        Queue<Expression> queue = new ArrayDeque<Expression>();
        // queue of expressions that occured after static operators
        Queue<Expression> afterStaticQueue = new ArrayDeque<Expression>();
        queue.add(value);
        
        // first, search for all expressions that occure after static operators
        while (!queue.isEmpty()) {
            Expression expr = queue.poll();
            if (checked.add(expr)) {
                // not checked before...
                if (expr instanceof ParallelExpr || expr instanceof RestrictExpr) {
                    afterStaticQueue.addAll(expr.getChildren());
                } else {
                    queue.addAll(expr.getChildren());
                }
            }
        }
        checked.clear();

        // then, check these expressions for occurences of the current declaration (recursive loop)
        while (!afterStaticQueue.isEmpty()) {
            Expression expr = afterStaticQueue.poll();
            if (checked.add(expr)) {
                // not checked before...
                if (expr instanceof RecursiveExpr) {
                    RecursiveExpr recExpr = (RecursiveExpr) expr;
                    if (recExpr.getReferencedDeclaration().equals(this))
                        return false;
                } else {
                    afterStaticQueue.addAll(expr.getChildren());
                }
            }
        }
        
        // nothing bad found...
        return true;
    }

    public String getName() {
        return name;
    }

    public List<Value> getParameters() {
        return parameters;
    }

    public Expression getValue() {
        return value;
    }

    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        return value.replaceRecursion(declarations);
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
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
