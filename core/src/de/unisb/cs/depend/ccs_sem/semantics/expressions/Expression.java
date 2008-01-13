package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.ParameterRefValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.Value;


public abstract class Expression {

    private static Map<Expression, Expression> repository
        = new HashMap<Expression, Expression>();

    private volatile List<Transition> transitions = null;

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
        return transitions != null;
    }

    // precondition: children have been evaluated
    protected abstract List<Transition> evaluate0();

    // returns the children that have to be evaluated before calling evaluate()
    public abstract Collection<Expression> getChildren();

    // returns all subterms occuring in this expression. In general, it is the same
    // as the children, but in some Expressions, it must be overwritten.
    public Collection<Expression> getSubTerms() {
        return getChildren();
    }

    public List<Transition> getTransitions() {
        assert transitions != null;

        return transitions;
    }

    /**
     * Replaces every {@link UnknownString} either by a {@link PrefixExpr} and
     * a {@link StopExpr}, or by a {@link RecursiveExpr}, if a corresponding
     * Declaration has been found.
     * Typically delegates to it's subterms.
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
     * Typically delegates to it's subterms.
     * @param parameters
     * @return
     */
    public abstract Expression instantiate(List<Value> parameters);

    /**
     * Is called in the constructor of a {@link Declaration}.
     * Replaces all {@link Value}s of this expression that also occure in the
     * parameter list by corresponding {@link ParameterRefValue}s.
     * Typically delegates to it's subterms.
     * @param parameters
     * @return
     */
    public abstract Expression insertParameters(List<Parameter> parameters);

    // TODO this is not good, as it changed the transitions of this expression.
    // Idea: new parameter "minimized"
    public void minimize() {
        assert isEvaluated();

        for (final ListIterator<Transition> it = getTransitions().listIterator(); it.hasNext(); ) {
            final Transition oldTrans = it.next();
            final Transition newTrans = oldTrans.minimize();
            if (oldTrans != newTrans)
                it.set(newTrans);
        }
    }

    /**
     * Instantiates this Expression with some Value that is read from an input Action.
     * Typically delegates to it's subterms.
     * @param value
     * @return
     */
    public abstract Expression instantiateInputValue(Value value);

    // TODO store hashCode
    // TODO minimizeExpression (e.g. push down restriction)
}
