package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class PrefixExpr extends AbstractExpression {
    
    private Action prefix;
    private Expression postfix;

    public PrefixExpr(Action prefix, Expression postfix) {
        super();
        this.prefix = prefix;
        this.postfix = postfix;
    }

    public Collection<Expression> getChildren() {
        return Collections.singleton(postfix);
    }

    @Override
    protected List<Transition> evaluate0() {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        postfix = postfix.replaceRecursion(declarations);
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

}
