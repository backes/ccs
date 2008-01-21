package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class ChoiceExpr extends Expression {

    private final Expression left;
    private final Expression right;

    private ChoiceExpr(Expression left, Expression right) {
        super();
        this.left = left;
        this.right = right;
    }

    public static Expression create(Expression left, Expression right) {
        if (left instanceof StopExpr)
            return right;
        if (right instanceof StopExpr)
            return left;
        return Expression.getExpression(new ChoiceExpr(left, right));
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

        // decide if we use a clever way to combine the transitions or not
        final boolean useCleverWay = leftTrans.size() * rightTrans.size() > 20;
        final List<Transition> transitions =
            new ArrayList<Transition>(leftTrans.size() + rightTrans.size());
        transitions.addAll(leftTrans);

        final Collection<Transition> leftTransToCompare = useCleverWay
            ? new HashSet<Transition>(leftTrans) : leftTrans;

        for (final Transition trans: rightTrans) {
            if (!leftTransToCompare.contains(trans))
                transitions.add(trans);
        }

        return transitions;
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        final Expression newLeft = left.replaceRecursion(declarations);
        final Expression newRight = right.replaceRecursion(declarations);

        if (newLeft.equals(left) && newRight.equals(right))
            return this;

        return create(newLeft, newRight);
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        final Expression newLeft = left.instantiate(parameters);
        final Expression newRight = right.instantiate(parameters);
        if (newLeft.equals(left) && newRight.equals(right))
            return this;
        return create(newLeft, newRight);
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
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 2;
        result = PRIME * result + left.hashCode();
        result = PRIME * result + right.hashCode();
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
        if (!left.equals(other.left))
            return false;
        if (!right.equals(other.right))
            return false;
        return true;
    }

}
