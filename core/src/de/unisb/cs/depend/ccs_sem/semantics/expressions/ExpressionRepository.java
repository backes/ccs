package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.concurrent.ConcurrentMap;

import de.unisb.cs.depend.ccs_sem.utils.ConcurrentReferenceHashMap;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


public class ExpressionRepository {

    private final static ConcurrentMap<Expression, Expression> repository =
        new ConcurrentReferenceHashMap<Expression, Expression>(128, 0.75f,
                Globals.getConcurrencyLevel(),
                ConcurrentReferenceHashMap.ReferenceType.SOFT,
                ConcurrentReferenceHashMap.ReferenceType.SOFT, null);

    public static Expression getExpression(Expression expr) {
        // get needs no synchonization, so we always try a get first
        Expression foundExpr = repository.get(expr);
        if (foundExpr != null)
            return foundExpr;
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
