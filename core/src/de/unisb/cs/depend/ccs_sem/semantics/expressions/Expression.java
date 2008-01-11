package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterRefValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public abstract class Expression implements Cloneable {

    private static Map<Expression, Expression> repository
        = new HashMap<Expression, Expression>();

    private List<Transition> transitions = null;

    protected Expression() {
        // nothing to do
    }

    // precondition: children have been evaluated
    public void evaluate() {
        if (transitions != null)
            return;

        transitions = evaluate0();

        assert transitions != null;

        // save memory
        if (transitions instanceof ArrayList) {
            final ArrayList<Transition> list = (ArrayList<Transition>) transitions;
            list.trimToSize();
        }
    }

    public boolean isEvaluated() {
        return transitions == null;
    }

    // precondition: children have been evaluated
    protected abstract List<Transition> evaluate0();

    // returns the children that have to be evaluated before calling evaluate()
    public abstract Collection<Expression> getChildren();

    public List<Transition> getTransitions() {
        assert transitions != null;

        return transitions;
    }

    @Override
    public Expression clone() {
        // TODO remove clone method?
        try {
            final Expression cloned = (Expression) super.clone();
            // the clone is typically changed afterwards
            cloned.transitions = null;
            return cloned;
        } catch (final CloneNotSupportedException e) {
            throw new InternalSystemException("Expression cannot be cloned", e);
        }
    }

    /**
     * Replaces every {@link UnknownString} either by a {@link PrefixExpr} and
     * a {@link StopExpr}, or by a {@link RecursiveExpr}.
     * @return either itself or a new created Expression, if something changed
     */
    public abstract Expression replaceRecursion(List<Declaration> declarations) throws ParseException;

    public static Expression getExpression(Expression expr) {
        final Expression foundExpr = repository.get(expr);
        if (foundExpr != null)
            return foundExpr;

        repository.put(expr, expr);

        return expr;
    }

    /**
     * Is called in the constructor of a {@link RecursiveExpr}.
     * Replaces all {@link ParameterRefValue}s that occure in the expression by
     * the corresponding {@link Value} from the parameter list.
     * @param parameters
     * @return
     */
    public abstract Expression instantiate(List<Value> parameters);

    /**
     * Is called in the constructor of a {@link Declaration}.
     * Replaces all {@link Value}s of this expression that also occure in the
     * parameter list by corresponding {@link ParameterRefValue}s.
     * @param parameters
     * @return
     */
    public abstract Expression insertParameters(List<Value> parameters);

    // TODO store hashCode
}
