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

}
