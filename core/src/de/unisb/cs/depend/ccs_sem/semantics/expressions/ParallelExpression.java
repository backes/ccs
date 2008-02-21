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
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
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
                (leftTransitions.size() + rightTransitions.size()) * 3);

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

        boolean useComplexWay = leftTransitions.size() > 3
                && rightTransitions.size() > 3;

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
                if (leftTrans.getAction().isInputAction()
                        && rightTrans.getAction().isOutputAction())
                    newFromLeft = leftTrans.synchronizeWith(rightTrans.getAction());
                if (rightTrans.getAction().isInputAction()
                        && leftTrans.getAction().isOutputAction())
                    newFromRight = rightTrans.synchronizeWith(leftTrans.getAction());

                if (newFromLeft != null) {
                    if (newFromRight != null) {
                        // take care that we don't add the same transition twice
                        if (!newFromLeft.equals(leftTrans.getTarget())
                                || !newFromRight.equals(rightTrans.getTarget())) {
                            // in this case, we have to add this new transition too
                            final Expression newTarget = create(leftTrans.getTarget(), newFromRight);
                            final Transition newTransition = new Transition(TauAction.get(), newTarget);
                            transitions.add(newTransition);
                        }
                    }
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
        final Map<Channel, List<Transition>> leftInput =
            new HashMap<Channel, List<Transition>>(leftTransitions.size());
        final Map<Channel, List<Transition>> rightInput =
            new HashMap<Channel, List<Transition>>(rightTransitions.size());

        // fill the map leftInput
        for (final Transition leftTrans: leftTransitions) {
            if (leftTrans.getAction().isInputAction()) {
                final Channel channel = leftTrans.getAction().getChannel();
                List<Transition> list = leftInput.get(channel);
                if (list == null)
                    leftInput.put(channel, list = new ArrayList<Transition>(2));
                list.add(leftTrans);
            }
        }
        // fill the map rightInput and check for matches with leftInput
        for (final Transition rightTrans: rightTransitions) {
            if (rightTrans.getAction().isInputAction()) {
                final Channel channel = rightTrans.getAction().getChannel();
                List<Transition> list = rightInput.get(channel);
                if (list == null)
                    rightInput.put(channel, list = new ArrayList<Transition>(2));
                list.add(rightTrans);
            }
            if (rightTrans.getAction().isOutputAction()) {
                // search for corresponding input action
                final Channel channel = rightTrans.getAction().getChannel();
                final List<Transition> inputTransitions = leftInput.get(channel);
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
            if (!leftTrans.getAction().isOutputAction())
                continue;
            // search for corresponding input action
            final Channel channel = leftTrans.getAction().getChannel();
            final List<Transition> inputTransitions = rightInput.get(channel);
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
    public Expression replaceRecursion(List<Declaration> declarations)
            throws ParseException {
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
    protected boolean isError0() {
        return left.isError() || right.isError();
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
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 3;
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
        final ParallelExpression other = (ParallelExpression) obj;
        // hashCode is cached, so we compare it first (it's cheap)
        if (hashCode() != other.hashCode())
            return false;
        if (!left.equals(other.left))
            return false;
        if (!right.equals(other.right))
            return false;
        return true;
    }

}
