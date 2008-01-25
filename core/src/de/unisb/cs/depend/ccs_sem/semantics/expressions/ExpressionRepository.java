package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.concurrent.ConcurrentHashMap;


public class ExpressionRepository {

    private final static ConcurrentHashMap<Expression, Expression> repository =
        new ConcurrentHashMap<Expression, Expression>();

    public static Expression getExpression(Expression expr) {
        final Expression foundExpr = repository.putIfAbsent(expr, expr);
        return foundExpr == null ? expr : foundExpr;
    }

    public ExpressionRepository() {
        super();
    }

    public static void reset() {
        repository.clear();
    }

}
