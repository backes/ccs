package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        List<Expression> children = new ArrayList<Expression>(2);
        children.add(left);
        children.add(right);
        return children;
    }

    @Override
    protected List<Transition> evaluate0() {
        // compute the union of the transitions of the left expressions and
        // the transition of the right expression
        List<Transition> leftTrans = left.evaluate();
        List<Transition> rightTrans = right.evaluate();
        
        List<Transition> myTrans = new ArrayList<Transition>(leftTrans.size() + rightTrans.size());
        myTrans.addAll(leftTrans);
        myTrans.addAll(rightTrans);
        
        return myTrans;
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
        ChoiceExpr cloned = (ChoiceExpr) super.clone();
        cloned.left = left.clone();
        cloned.right = right.clone();

        return cloned;
    }

}
