package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
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
        List<Expression> children = new ArrayList<Expression>(2);
        children.add(left);
        children.add(right);
        return children;
    }

    @Override
    protected List<Transition> evaluate0() {
        List<Transition> leftTransitions = left.evaluate();
        List<Transition> rightTransitions = right.evaluate();
        
        List<Transition> transitions = new ArrayList<Transition>((leftTransitions.size() + rightTransitions.size())*3/2);
        
        // either left alone
        for (Transition trans: leftTransitions) {
            Expression newExpr = new ParallelExpr(trans.getTarget(), right);
            // search if this expression is already known
            newExpr = Expression.getExpression(newExpr);
            // search if this transition is already known (otherwise create it)
            Transition newTrans = Transition.getTransition(trans.getAction(), newExpr);
            transitions.add(newTrans);
        }

        // or right alone
        for (Transition trans: rightTransitions) {
            Expression newExpr = new ParallelExpr(left, trans.getTarget());
            // search if this expression is already known
            newExpr = Expression.getExpression(newExpr);
            // search if this transition is already known (otherwise create it)
            Transition newTrans = Transition.getTransition(trans.getAction(), newExpr);
            transitions.add(newTrans);
        }
        
        // or synchronized
        for (Transition leftTrans: leftTransitions) {
            for (Transition rightTrans: rightTransitions) {
                if (leftTrans.getAction().isCounterTransition(rightTrans.getAction())) {
                    Expression newExpr = new ParallelExpr(leftTrans.getTarget(), rightTrans.getTarget());
                    // search if this expression is already known
                    newExpr = Expression.getExpression(newExpr);
                    // search if this transition is already known (otherwise create it)
                    Transition newTrans = Transition.getTransition(TauAction.get(), newExpr);
                    transitions.add(newTrans);
                }
            }
        }

        return transitions;
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        left = left.replaceRecursion(declarations);
        right = right.replaceRecursion(declarations);
        return this;
    }

    @Override
    public Expression replaceParameters(List<Value> parameters) {
        left = left.replaceParameters(parameters);
        right = right.replaceParameters(parameters);

        return this;
    }

    @Override
    public Expression insertParameters(List<Value> parameters) {
        left = left.insertParameters(parameters);
        right = right.insertParameters(parameters);
        
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
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
        ParallelExpr cloned = (ParallelExpr) super.clone();
        cloned.left = left.clone();
        cloned.right = right.clone();
        
        return cloned;
    }

}
