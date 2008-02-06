package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterReference;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public abstract class Expression {

    private volatile List<Transition> transitions = null;

    // stores the hashcode of this expression
    private volatile int hash = 0;

    protected Expression() {
        // nothing to do
    }

    // precondition: children have been evaluated
    public void evaluate() {
        if (transitions != null)
            return;

        List<Transition> transitions0 = evaluate0();

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

    /**
     * Is called in the constructor of a {@link RecursiveExpr}.
     * Replaces all {@link ParameterReference}s that occure in the expression by
     * the corresponding {@link Value} from the parameter list.
     * Typically delegates to it's subterms.
     * @param parameters the parameters to replace by concrete values
     * @return either <code>this</code> or a new created Expression
     */
    public abstract Expression instantiate(Map<Parameter, Value> parameters);

    // we store the hashCode so that we only compute it once
    @Override
    public final int hashCode() {
        int h = this.hash;
        if (h == 0) {
            synchronized (this) {
                if ((h = this.hash) == 0)
                    h = hashCode0();
                // we don't allow "0" as hashCode
                this.hash = h == 0 ? 1 : h;
            }
        }

        assert h == hashCode0() || (h == 0 && hashCode0() == 1);

        return h;
    }

    protected abstract int hashCode0();

}
