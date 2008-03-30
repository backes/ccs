package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public abstract class Expression {

    private volatile List<Transition> transitions = null;

    // stores the hashcode of this expression
    private volatile int hash = 0;

    // cache for isError()
    private Boolean isError = null;

    protected Expression() {
        // nothing to do
    }

    /**
     * Evaluates this expression, i.e. creates a List of all outgoing {@link Transition}s.
     *
     * Before calling this method, all children (see {@link #getChildren()})
     * must have been evaluated.
     */
    public void evaluate() {
        if (isEvaluated())
            return;

        List<Transition> transitions0;
        if (isError())
            transitions0 = Collections.emptyList();
        else
            transitions0 = evaluate0();

        assert transitions0 != null;

        // some optimisations to save memory
        if (transitions0 instanceof ArrayList) {
            if (transitions0.size() == 0)
                transitions0 = Collections.emptyList();
            else if (transitions0.size() == 1)
                transitions0 = Collections.singletonList(transitions0.get(0));
            else
                ((ArrayList<Transition>)transitions0).trimToSize();
        }

        // volatile write
        transitions = transitions0;
    }

    public boolean isEvaluated() {
        return transitions != null;
    }

    // precondition: children have been evaluated
    protected abstract List<Transition> evaluate0();

    /**
     * @return the children that have to be evaluated before calling evaluate()
     */
    public abstract Collection<Expression> getChildren();

    /**
     * @return all subterms occuring in this expression. In general, it is the
     *         same as the children, but in some Expressions, it must be overwritten.
     */
    public Collection<Expression> getSubTerms() {
        return getChildren();
    }

    /**
     * Precondition: evaluate() has been called before.
     *
     * @return all outgoing transitions of this expression.
     */
    public List<Transition> getTransitions() {
        assert transitions != null;

        return transitions;
    }

    /**
     * Replaces every {@link UnknownRecursiveExpression} by a
     * {@link RecursiveExpression}, if a corresponding {@link ProcessVariable}
     * has been found.
     * Typically delegates to its subterms.
     * @return either itself or a new created Expression, if something changed
     */
    public abstract Expression replaceRecursion(List<ProcessVariable> processVariables) throws ParseException;

    /**
     * Is called in the constructor of a {@link RecursiveExpression}.
     * Replaces all {@link ParameterReference}s that occure in the expression by
     * the corresponding {@link Value} from the parameter list.
     * Typically delegates to its subterms.
     * @param parameters the parameters to replace by concrete values
     * @return either <code>this</code> or a new created Expression
     */
    public abstract Expression instantiate(Map<Parameter, Value> parameters);

    /**
     * @return whether this Expression represents an "error" expression
     */
    public boolean isError() {
        if (isError == null)
            isError = Boolean.valueOf(isError0());
        return isError;
    }

    /**
     * @see #isError()
     */
    protected abstract boolean isError0();

    /**
     * Computes the alphabet of this Expression.
     *
     * @return the alphabet of this Expression
     */
    public final Set<Action> getAlphabet() {
        return getAlphabet(new HashSet<ProcessVariable>());
    }

    public abstract Set<Action> getAlphabet(Set<ProcessVariable> alreadyIncluded);

    // we store the hashCode so that we only compute it once
    @Override
    public final int hashCode() {
        int h = this.hash; // volatile-read
        if (h == 0) {
            synchronized (this) {
                // double-checking (hm...)
                if ((h = this.hash) == 0)
                    h = hashCode0();
                // we don't allow "0" as hashCode
                if (h == 0)
                    h = 1;
                this.hash = h; // volatile-write
            }
        }

        assert h == hashCode0() || (h == 1 && hashCode0() == 0);

        return h;
    }

    protected abstract int hashCode0();

}
