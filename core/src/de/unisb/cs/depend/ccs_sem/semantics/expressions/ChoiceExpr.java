package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public class ChoiceExpr extends Expression {

    private Expression left;
    private Expression right;

    public ChoiceExpr(Expression left, Expression right) {
        super();
        this.left = left;
        this.right = right;
    }

    @Override
    public Collection<Expression> getChildren() {
        final List<Expression> children = new ArrayList<Expression>(2);
        children.add(left);
        children.add(right);
        return children;
    }

    @Override
    protected List<Transition> evaluate0() {
        // compute the union of the transitions of the left expressions and
        // the transition of the right expression
        final List<Transition> leftTrans = left.getTransitions();
        final List<Transition> rightTrans = right.getTransitions();

        // the set automatically filters out double transitions
        final Set<Transition> myTrans = new HashSet<Transition>(leftTrans.size() + rightTrans.size());
        myTrans.addAll(leftTrans);
        myTrans.addAll(rightTrans);

        return new ArrayList<Transition>(myTrans);
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        final Expression newLeft = left.replaceRecursion(declarations);
        final Expression newRight = right.replaceRecursion(declarations);

        if (newLeft.equals(left) && newRight.equals(right))
            return this;

        return Expression.getExpression(new ChoiceExpr(newLeft, newRight));
    }

    @Override
    public Expression instantiate(List<Value> parameters) {
        final Expression newLeft = left.instantiate(parameters);
        final Expression newRight = right.instantiate(parameters);

        if (newLeft.equals(left) && newRight.equals(right))
            return this;

        return Expression.getExpression(new ChoiceExpr(newLeft, newRight));
    }

    @Override
    public Expression insertParameters(List<Value> parameters) {
        final Expression newLeft = left.insertParameters(parameters);
        final Expression newRight = right.insertParameters(parameters);

        if (newLeft.equals(left) && newRight.equals(right))
            return this;

        return Expression.getExpression(new ChoiceExpr(newLeft, newRight));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (left instanceof RestrictExpr || left instanceof ParallelExpr) {
            sb.append('(').append(left).append(')');
        } else {
            sb.append(left);
        }
        sb.append(" + ");
        if (right instanceof RestrictExpr || right instanceof ParallelExpr) {
            sb.append('(').append(right).append(')');
        } else {
            sb.append(right);
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((left == null) ? 0 : left.hashCode());
        result = PRIME * result + ((right == null) ? 0 : right.hashCode());
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
        final ChoiceExpr other = (ChoiceExpr) obj;
        if (left == null) {
            if (other.left != null)
                return false;
        } else if (!left.equals(other.left))
            return false;
        if (right == null) {
            if (other.right != null)
                return false;
        } else if (!right.equals(other.right))
            return false;
        return true;
    }

    @Override
    public Expression clone() {
        final ChoiceExpr cloned = (ChoiceExpr) super.clone();
        cloned.left = left.clone();
        cloned.right = right.clone();

        return cloned;
    }

}
