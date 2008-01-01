package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class RestrictExpr extends AbstractExpression {
    
    private Expression expr;
    private Set<Action> restricted;

    public RestrictExpr(Expression expr, Set<Action> restricted) {
        super();
        this.expr = expr;
        this.restricted = restricted;
    }

    public Collection<Expression> getChildren() {
        return Collections.singleton(expr);
    }

    @Override
    protected List<Transition> evaluate0() {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        expr = expr.replaceRecursion(declarations);
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expr).append(" \\ {");
        boolean first = true;
        for (Action restr: restricted) {
            if (first)
                first = false;
            else
                sb.append(',');
            sb.append(restr);
        }
        sb.append('}');

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((expr == null) ? 0 : expr.hashCode());
        result = PRIME * result + ((restricted == null) ? 0 : restricted.hashCode());
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
        final RestrictExpr other = (RestrictExpr) obj;
        if (expr == null) {
            if (other.expr != null)
                return false;
        } else if (!expr.equals(other.expr))
            return false;
        if (restricted == null) {
            if (other.restricted != null)
                return false;
        } else if (!restricted.equals(other.restricted))
            return false;
        return true;
    }

}
