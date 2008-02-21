package de.unisb.cs.depend.ccs_sem.semantics.expressions.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ExpressionRepository;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


/**
 * This is an adapter for an expression that minimizes all outgoing transitions,
 * i.e. it substitutes all chains of tau transitions (labeled by "i") by just
 * one tau transition to the corresponding target.
 *
 * This implementation is very fast, but only does some simple minimisations.
 * If you want to have the smallest weak bisimilar lts, you should use
 * {@link MinimisingExpression}.
 *
 * @author Clemens Hammacher
 */
public class FastMinimisingExpression extends Expression {

    Expression myExpr;

    /**
     * Before calling this constructor, the given expression must be fully
     * evaluated.
     *
     * @param myExpr the main expression of the program to minimize
     */
    public FastMinimisingExpression(Expression myExpr) {
        super();
        this.myExpr = myExpr;
        assert myExpr.isEvaluated();
    }

    @Override
    protected List<Transition> evaluate0() {
        List<Transition> transitions = myExpr.getTransitions();

        boolean onlyTauOutgoing = true;
        final Queue<Transition> tauTransitions = new LinkedList<Transition>();

        // if we have just one outgoing tau transition, we just move forward
        // to the target of this transition
        while (transitions.size() == 1) {
            final Transition firstTrans = transitions.get(0);
            // check for cycle
            if (firstTrans.getTarget() == myExpr)
                break;
            if (firstTrans.getAction() instanceof TauAction)
                transitions = firstTrans.getTarget().getTransitions();
            else {
                onlyTauOutgoing = false;
                break;
            }
        }

        if (onlyTauOutgoing)
            for (final Transition trans: transitions) {
                if (trans.getAction() instanceof TauAction) {
                    tauTransitions.add(trans);
                } else {
                    onlyTauOutgoing = false;
                    break;
                }
            }

        if (!onlyTauOutgoing) {
            // minimize the successors and return the transitions
            final Set<Transition> newTransitions = new HashSet<Transition>(transitions.size() * 3 / 2);
            for (final Transition trans: transitions) {
                FastMinimisingExpression expr = new FastMinimisingExpression(trans.getTarget());
                expr = (FastMinimisingExpression)ExpressionRepository.getExpression(expr);
                newTransitions.add(new Transition(trans.getAction(), expr));
            }
            return new ArrayList<Transition>(newTransitions);
        }

        final Set<Transition> newTransitions = new HashSet<Transition>(transitions.size() * 2);

        Transition trans = null;
        while ((trans = tauTransitions.poll()) != null) {
            onlyTauOutgoing = true;
            final List<Transition> targetTransitions = trans.getTarget().getTransitions();
            for (final Transition trans2: targetTransitions)
                if (!(trans2.getAction() instanceof TauAction)) {
                    onlyTauOutgoing = false;
                    break;
                }
            if (onlyTauOutgoing)
                tauTransitions.addAll(targetTransitions);
            else {
                FastMinimisingExpression expr = new FastMinimisingExpression(trans.getTarget());
                expr = (FastMinimisingExpression)ExpressionRepository.getExpression(expr);
                newTransitions.add(new Transition(trans.getAction(), expr));
            }
        }

        return new ArrayList<Transition>(newTransitions);
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(myExpr);
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        throw new UnsupportedOperationException("An expression cannot be instantiated after minimization.");
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isError0() {
        return myExpr.isError();
    }

    @Override
    public String toString() {
        return myExpr.toString();
    }

    @Override
    protected int hashCode0() {
        return myExpr.hashCode() + 37;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FastMinimisingExpression other = (FastMinimisingExpression) obj;
        return myExpr.equals(other.myExpr);
    }

}
