package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public class PrefixExpr extends Expression {
    
    private Action prefix;
    private Expression postfix;

    public PrefixExpr(Action prefix, Expression postfix) {
        super();
        this.prefix = prefix;
        this.postfix = postfix;
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(postfix);
    }

    @Override
    protected List<Transition> evaluate0() {
        return Collections.singletonList(Transition.getTransition(prefix, postfix));
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        Expression newPostfix = postfix.replaceRecursion(declarations);
        
        if (newPostfix.equals(postfix))
            return this;
        
        return Expression.getExpression(new PrefixExpr(prefix, newPostfix));
    }

    @Override
    public Expression replaceParameters(List<Value> parameters) {
        Action newPrefix = prefix.replaceParameters(parameters);
        Expression newPostfix = postfix.replaceParameters(parameters);
        
        if (newPrefix.equals(prefix) && newPostfix.equals(postfix))
            return this;

        return Expression.getExpression(new PrefixExpr(newPrefix, newPostfix));
    }

    @Override
    public Expression insertParameters(List<Value> parameters) {
        prefix = prefix.insertParameters(parameters);
        postfix = postfix.insertParameters(parameters);

        return this;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append('.');
        if (postfix instanceof ChoiceExpr || postfix instanceof ParallelExpr
                || postfix instanceof RestrictExpr)
            sb.append('(').append(postfix).append(')');
        else
            sb.append(postfix);

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((postfix == null) ? 0 : postfix.hashCode());
        result = PRIME * result + ((prefix == null) ? 0 : prefix.hashCode());
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
        final PrefixExpr other = (PrefixExpr) obj;
        if (postfix == null) {
            if (other.postfix != null)
                return false;
        } else if (!postfix.equals(other.postfix))
            return false;
        if (prefix == null) {
            if (other.prefix != null)
                return false;
        } else if (!prefix.equals(other.prefix))
            return false;
        return true;
    }
    
    @Override
    public Expression clone() {
        PrefixExpr cloned = (PrefixExpr) super.clone();
        
        // the prefix doesn't have to be cloned (actions are "immutable" (not really))

        cloned.postfix = postfix.clone();

        return cloned;
    }

}
