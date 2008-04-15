package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.InputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.OutputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


public class ParallelExpression extends Expression {

    private final Expression left;
    private final Expression right;

    protected ParallelExpression(Expression left, Expression right) {
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
        return ExpressionRepository.getExpression(new ParallelExpression(left, right));
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

        if (leftTransitions.isEmpty() && rightTransitions.isEmpty())
            return Collections.emptyList();

        // we have to use a set here so that we don't add the same transition twice
        final Set<Transition> transitions = new HashSet<Transition>(
                (leftTransitions.size() + rightTransitions.size()) * 4);

        // either left alone:
        for (final Transition trans: leftTransitions) {
            final Expression newExpr = create(trans.getTarget(), right);
            // search if this transition is already known (otherwise create it)
            final Transition newTrans = new Transition(trans.getAction(), newExpr);
            transitions.add(newTrans);
        }

        // or right alone:
        for (final Transition trans: rightTransitions) {
            final Expression newExpr = create(left, trans.getTarget());
            // search if this transition is already known (otherwise create it)
            final Transition newTrans = new Transition(trans.getAction(), newExpr);
            transitions.add(newTrans);
        }

        boolean useComplexWay = leftTransitions.size() > 5
                && rightTransitions.size() > 5;

        // in debug mode switch between the two modes
        assert (useComplexWay = new Random().nextBoolean()) || !useComplexWay;

        if (useComplexWay) {
            combineUsingComplexWay(leftTransitions, rightTransitions, transitions);
        } else {
            combineUsingNaiveWay(leftTransitions, rightTransitions, transitions);
        }

        return new ArrayList<Transition>(transitions);
    }

    private void combineUsingNaiveWay(final List<Transition> leftTransitions,
            final List<Transition> rightTransitions,
            final Set<Transition> transitions) {
        for (final Transition leftTrans: leftTransitions)
            for (final Transition rightTrans: rightTransitions) {
                Expression newFromLeft = null;
                Expression newFromRight = null;
                if (leftTrans.getAction() instanceof InputAction &&
                        rightTrans.getAction() instanceof OutputAction)
                    newFromLeft = leftTrans.synchronizeWith(rightTrans.getAction());
                if (rightTrans.getAction() instanceof InputAction &&
                        leftTrans.getAction() instanceof OutputAction)
                    newFromRight = rightTrans.synchronizeWith(leftTrans.getAction());

                // at most one of them can be not-null
                assert newFromLeft == null || newFromRight == null;

                if (newFromLeft != null) {
                    final Expression newTarget = create(newFromLeft, rightTrans.getTarget());
                    final Transition newTransition = new Transition(TauAction.get(), newTarget);
                    transitions.add(newTransition);
                } else if (newFromRight != null) {
                    final Expression newTarget = create(leftTrans.getTarget(), newFromRight);
                    final Transition newTransition = new Transition(TauAction.get(), newTarget);
                    transitions.add(newTransition);
                }
            }
    }

    private void combineUsingComplexWay(final List<Transition> leftTransitions,
            final List<Transition> rightTransitions,
            final Set<Transition> transitions) {
        // we use maps that list for each channel the correspoding input actions
        final Map<String, List<Transition>> leftInput =
            new HashMap<String, List<Transition>>(leftTransitions.size());
        final Map<String, List<Transition>> rightInput =
            new HashMap<String, List<Transition>>(rightTransitions.size());

        // fill the map leftInput
        for (final Transition leftTrans: leftTransitions) {
            if (leftTrans.getAction() instanceof InputAction) {
                final String channelString = leftTrans.getAction().getChannel().getStringValue();
                List<Transition> list = leftInput.get(channelString);
                if (list == null)
                    leftInput.put(channelString, list = new ArrayList<Transition>(2));
                list.add(leftTrans);
            }
        }
        // fill the map rightInput and check for matches with leftInput
        for (final Transition rightTrans: rightTransitions) {
            if (rightTrans.getAction() instanceof InputAction) {
                final String channelString = rightTrans.getAction().getChannel().getStringValue();
                List<Transition> list = rightInput.get(channelString);
                if (list == null)
                    rightInput.put(channelString, list = new ArrayList<Transition>(2));
                list.add(rightTrans);
            }
            if (rightTrans.getAction() instanceof OutputAction) {
                // search for corresponding input action
                final List<Transition> inputTransitions = leftInput.get(
                    rightTrans.getAction().getChannel().getStringValue());
                if (inputTransitions != null) {
                    for (final Transition inputTrans: inputTransitions) {
                        final Expression newLeftTarget = inputTrans.synchronizeWith(rightTrans.getAction());
                        if (newLeftTarget != null) {
                            // i.e. there was a match
                            final Expression newTarget = create(newLeftTarget, rightTrans.getTarget());
                            final Transition newTrans = new Transition(TauAction.get(), newTarget);
                            transitions.add(newTrans);
                        }
                    }
                }
            }
        }
        // search for matching pairs vice-versa
        for (final Transition leftTrans: leftTransitions) {
            if (!(leftTrans.getAction() instanceof OutputAction))
                continue;
            // search for corresponding input action
            final List<Transition> inputTransitions = rightInput.get(
                leftTrans.getAction().getChannel().getStringValue());
            if (inputTransitions != null) {
                for (final Transition inputTrans: inputTransitions) {
                    final Expression newRightTarget = inputTrans.synchronizeWith(leftTrans.getAction());
                    if (newRightTarget != null) {
                        final Expression newTarget = create(leftTrans.getTarget(), newRightTarget);
                        final Transition newTrans = new Transition(TauAction.get(), newTarget);
                        transitions.add(newTrans);
                    }
                }
            }
        }
    }

    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables)
            throws ParseException {
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
    public Set<Action> getAlphabet(Set<ProcessVariable> alreadyIncluded) {
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
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (left instanceof RestrictExpression) {
            sb.append('(').append(left).append(')');
        } else {
            sb.append(left);
        }
        sb.append(" | ");
        if (right instanceof RestrictExpression) {
            sb.append('(').append(right).append(')');
        } else {
            sb.append(right);
        }

        return sb.toString();
    }

    @Override
    public int hashCode(Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
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
        final ParallelExpression other = (ParallelExpression) obj;
        if (!left.equals(other.left, parameterOccurences))
            return false;
        if (!right.equals(other.right, parameterOccurences))
            return false;
        return true;
    }

}
