package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class ParallelExpr extends AbstractExpression {
    
    private Expression left;
    private Expression right;
    
    public ParallelExpr(Expression left, Expression right) {
        super();
        this.left = left;
        this.right = right;
    }

    public Collection<Expression> getChildren() {
        List<Expression> children = new ArrayList<Expression>(2);
        children.add(left);
        children.add(right);
        return children;
    }

    @Override
    protected List<Transition> evaluate0() {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        left = left.replaceRecursion(declarations);
        right = right.replaceRecursion(declarations);
        return this;
    }

}
