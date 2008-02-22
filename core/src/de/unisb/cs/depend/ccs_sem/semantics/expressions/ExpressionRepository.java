package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.concurrent.ConcurrentHashMap;

import de.unisb.cs.depend.ccs_sem.utils.Globals;


public class ExpressionRepository {

    private final static ConcurrentHashMap<Expression, Expression> repository =
        new ConcurrentHashMap<Expression, Expression>(128, 0.75f, Globals.getConcurrencyLevel());

    public static Expression getExpression(Expression expr) {
        Expression foundExpr = repository.get(expr);
        if (foundExpr == null)
            foundExpr = repository.putIfAbsent(expr, expr);
        return foundExpr == null ? expr : foundExpr;
    }

    public ExpressionRepository() {
        super();
    }

    public static void reset() {
        repository.clear();
    }

}
