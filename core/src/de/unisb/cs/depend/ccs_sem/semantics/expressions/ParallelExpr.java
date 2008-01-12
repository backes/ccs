package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public class ParallelExpr extends Expression {

    private Expression left;
    private Expression right;

    public ParallelExpr(Expression left, Expression right) {
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
        final List<Transition> leftTransitions = left.getTransitions();
        final List<Transition> rightTransitions = right.getTransitions();

        final List<Transition> transitions =
                new ArrayList<Transition>(
                        (leftTransitions.size() + rightTransitions.size()) * 3 / 2);

        // either left alone:
        for (final Transition trans: leftTransitions) {
            Expression newExpr = new ParallelExpr(trans.getTarget(), right);
            // search if this expression is already known
            newExpr = Expression.getExpression(newExpr);
            // search if this transition is already known (otherwise create it)
            final Transition newTrans =
                    Transition.getTransition(trans.getAction(), newExpr);
            transitions.add(newTrans);
        }

        // or right alone:
        for (final Transition trans: rightTransitions) {
            Expression newExpr = new ParallelExpr(left, trans.getTarget());
            // search if this expression is already known
            newExpr = Expression.getExpression(newExpr);
            // search if this transition is already known (otherwise create it)
            final Transition newTrans =
                    Transition.getTransition(trans.getAction(), newExpr);
            transitions.add(newTrans);
        }

        // or synchronized:
        // this is one of the hardest tasks, so it might be usefull to use a more
        // clever way than just iterate nestedly through both transition lists
        // TODO try other values here
        final boolean useCleverWay =
                leftTransitions.size() > 3
                        && rightTransitions.size() > 3
                        && (leftTransitions.size() + rightTransitions.size()) > 10;
        if (useCleverWay) {
            final boolean leftSmaller =
                    leftTransitions.size() < rightTransitions.size();
            final List<Transition> smaller =
                    leftSmaller ? leftTransitions : rightTransitions;
            final List<Transition> bigger =
                    leftSmaller ? rightTransitions : leftTransitions;

            final Map<Action, List<Expression>> actions =
                    new HashMap<Action, List<Expression>>(bigger.size() * 3 / 2);
            for (final Transition trans: bigger) {
                List<Expression> list = actions.get(trans.getAction());
                if (list == null)
                    actions.put(trans.getAction(), list =
                            new ArrayList<Expression>(2));
                list.add(trans.getTarget());
            }
            for (final Transition trans: smaller) {
                final Action counterAct = trans.getAction().getCounterAction();
                final List<Expression> list = actions.get(counterAct);
                if (list != null)
                    for (final Expression expr: list) {
                        final ParallelExpr newExpr =
                                leftSmaller ? new ParallelExpr(trans
                                    .getTarget(), expr) : new ParallelExpr(
                                        expr, trans.getTarget());
                        transitions.add(Transition.getTransition(TauAction
                            .get(), Expression.getExpression(newExpr)));
                    }
            }
        } else {
            for (final Transition leftTrans: leftTransitions)
                for (final Transition rightTrans: rightTransitions)
                    if (leftTrans.getAction().isCounterAction(
                        rightTrans.getAction()))
                        transitions.add(Transition
                            .getTransition(TauAction.get(), Expression
                                .getExpression(new ParallelExpr(leftTrans
                                    .getTarget(), rightTrans.getTarget()))));
        }

        return transitions;
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations)
            throws ParseException {
        final Expression newLeft = left.replaceRecursion(declarations);
        final Expression newRight = right.replaceRecursion(declarations);

        if (newLeft.equals(left) && newRight.equals(right))
            return this;

        return Expression.getExpression(new ParallelExpr(newLeft, newRight));
    }

    @Override
    public Expression instantiate(List<Value> parameters) {
        final Expression newLeft = left.instantiate(parameters);
        final Expression newRight = right.instantiate(parameters);

        if (newLeft.equals(left) && newRight.equals(right))
            return this;

        return Expression.getExpression(new ParallelExpr(newLeft, newRight));
    }

    @Override
    public Expression insertParameters(List<Value> parameters) {
        final Expression newLeft = left.insertParameters(parameters);
        final Expression newRight = right.insertParameters(parameters);

        if (newLeft.equals(left) && newRight.equals(right))
            return this;

        return Expression.getExpression(new ParallelExpr(newLeft, newRight));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (left instanceof RestrictExpr) {
            sb.append('(').append(left).append(')');
        } else {
            sb.append(left);
        }
        sb.append(" | ");
        if (right instanceof RestrictExpr) {
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
        final ParallelExpr other = (ParallelExpr) obj;
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
        final ParallelExpr cloned = (ParallelExpr) super.clone();
        cloned.left = left.clone();
        cloned.right = right.clone();

        return cloned;
    }

}
