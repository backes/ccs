package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpression.RecursiveExpressionAlphabetWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


public class ChoiceExpression extends Expression {

    private final Expression left;
    private final Expression right;

    private ChoiceExpression(Expression left, Expression right) {
        super();
        this.left = left;
        this.right = right;
    }

    public static Expression create(Expression left, Expression right) {
        if (Globals.isMinimizeExpressions()) {
            if (left instanceof StopExpression)
                return right;
            if (right instanceof StopExpression)
                return left;
        }
        return ExpressionRepository.getExpression(new ChoiceExpression(left, right));
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

        if (leftTrans.isEmpty())
            return rightTrans;
        if (rightTrans.isEmpty())
            return leftTrans;

        // decide if we use a clever way to combine the transitions or not
        final boolean useHashSetToCompare = leftTrans.size() > 5 && rightTrans.size() > 5;
        final List<Transition> transitions =
            new ArrayList<Transition>(leftTrans.size() + rightTrans.size());
        transitions.addAll(leftTrans);

        final Collection<Transition> leftTransToCompare = useHashSetToCompare
            ? new HashSet<Transition>(leftTrans) : leftTrans;

        for (final Transition trans: rightTrans) {
            if (!leftTransToCompare.contains(trans))
                transitions.add(trans);
        }

        return transitions;
    }

    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables) throws ParseException {
        final Expression newLeft = left.replaceRecursion(processVariables);
        final Expression newRight = right.replaceRecursion(processVariables);

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
    protected boolean isError0() {
        return left.isError() || right.isError();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (left instanceof RestrictExpression || left instanceof ParallelExpression) {
            sb.append('(').append(left).append(')');
        } else {
            sb.append(left);
        }
        sb.append(" + ");
        if (right instanceof RestrictExpression || right instanceof ParallelExpression) {
            sb.append('(').append(right).append(')');
        } else {
            sb.append(right);
        }

        return sb.toString();
    }

    @Override
    public Set<Action> getAlphabet(Set<RecursiveExpressionAlphabetWrapper> alreadyIncluded) {
        final Set<Action> leftAlphabet = left.getAlphabet(alreadyIncluded);
        final Set<Action> rightAlphabet = right.getAlphabet(alreadyIncluded);

        if (leftAlphabet.size() < rightAlphabet.size()) {
            rightAlphabet.addAll(leftAlphabet);
            return rightAlphabet;
        } else {
            leftAlphabet.addAll(rightAlphabet);
            return leftAlphabet;
        }
    }

    @Override
    public int hashCode(
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final boolean empty = parameterOccurences.isEmpty();
        if (empty && hash != 0)
            return hash;
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + left.hashCode(parameterOccurences);
        result = PRIME * result + right.hashCode(parameterOccurences);
        if (empty) {
            assert hash == 0 || hash == result;
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj,
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ChoiceExpression other = (ChoiceExpression) obj;
        if (!left.equals(other.left, parameterOccurences))
            return false;
        if (!right.equals(other.right, parameterOccurences))
            return false;
        return true;
    }

}
