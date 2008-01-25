package de.unisb.cs.depend.ccs_sem.semantics.expressions.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ExpressionRepository;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.InputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class TopMostExpression extends Expression {

    Expression myExpr;

    public TopMostExpression(Expression myExpr) {
        super();
        this.myExpr = myExpr;
    }

    @Override
    protected List<Transition> evaluate0() {
        final List<Transition> transitions = myExpr.getTransitions();

        final List<Transition> newTransitions = new ArrayList<Transition>(transitions.size());

        for (final Transition trans: transitions) {
            final Action act = trans.getAction();
            if (act instanceof InputAction) {
                final Parameter param = ((InputAction)act).getParameter();
                if (param != null) {
                    final Range range = param.getRange();
                    if (range == null) {
                        // TODO
                        System.err.println("Unbound input parameter at top level removed (action \"" + act + "\").");
                        continue;
                    }
                    for (final Value val: range.getPossibleValues()) {
                        final Map<Parameter, Value> map = Collections.singletonMap(param, val);
                        Expression newTarget = trans.getTarget().instantiate(map);
                        newTarget = ExpressionRepository.getExpression(new TopMostExpression(newTarget));
                        final Action newAction = new InputAction(act.getChannel(), val);
                        final Transition newTrans = new Transition(newAction, newTarget);
                        newTransitions.add(newTrans);
                    }
                    continue;
                }
            }
            final Expression newTarget = ExpressionRepository.getExpression(
                new TopMostExpression(trans.getTarget()));
            newTransitions.add(new Transition(trans.getAction(), newTarget));
        }

        return newTransitions;
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(myExpr);
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        final Expression newExpr = myExpr.instantiate(parameters);
        if (myExpr.equals(newExpr))
            return this;
        return ExpressionRepository.getExpression(new TopMostExpression(newExpr));
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations)
            throws ParseException {
        final Expression newExpr = myExpr.replaceRecursion(declarations);
        if (myExpr.equals(newExpr))
            return this;
        return ExpressionRepository.getExpression(new TopMostExpression(newExpr));
    }

    @Override
    public String toString() {
        return myExpr.toString();
    }

    @Override
    protected int hashCode0() {
        return myExpr.hashCode() + 89;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TopMostExpression other = (TopMostExpression) obj;
        return myExpr.equals(other.myExpr);
    }

}
