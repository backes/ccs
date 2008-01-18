package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.SimpleAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


/**
 * Is later (by replaceRecursion()) replaced by a PrefixExpression+StopExpression,
 * or by a RecursiveExpression.
 *
 * @author Clemens Hammacher
 */
public class UnknownString extends Expression {

    private final String name;
    private final List<Value> parameters;

    public UnknownString(String name, List<Value> parameters) {
        super();
        this.name = name;
        this.parameters = parameters;
    }

    public UnknownString(String name) {
        super();
        this.name = name;
        this.parameters = Collections.emptyList();
    }

    @Override
    public Collection<Expression> getChildren() {
        final StackTraceElement topmostStackTraceElement = Thread.currentThread().getStackTrace()[0];
        throw new InternalSystemException(topmostStackTraceElement.getClassName()
            + "." + topmostStackTraceElement.getMethodName()
            + " should never be called. Did you forget to call replaceRecursion?");
    }

    @Override
    protected List<Transition> evaluate0() {
        final StackTraceElement topmostStackTraceElement = Thread.currentThread().getStackTrace()[0];
        throw new InternalSystemException(topmostStackTraceElement.getClassName()
            + "." + topmostStackTraceElement.getMethodName()
            + " should never be called. Did you forget to call replaceRecursion?");
    }

    /**
     * Replaces this {@link UnknownString} by either a {@link RecursiveExpr} or
     * a {@link PrefixExpr} with a {@link StopExpr} on the right hand side.
     */
    @Override
    public Expression replaceRecursion(List<Declaration> declarations) throws ParseException {
        for (final Declaration decl: declarations) {
            if (decl.getName().equals(name) && decl.getParamNr() == parameters.size()) {
                // check if parameters match
                // this possibly throws a ParseException
                decl.checkMatch(parameters);
                final RecursiveExpr newExpr = new RecursiveExpr(decl, parameters);
                return Expression.getExpression(newExpr);
            }
        }

        // no match: take the string as prefix and add a "stop"
        // (error if parameters are given)
        if (parameters.size() > 0) {
            // search for possible matches
            final List<Declaration> proposals = new ArrayList<Declaration>();
            for (final Declaration decl: declarations)
                if (decl.getName().equalsIgnoreCase(name))
                    proposals.add(decl);
            final StringBuilder sb = new StringBuilder("Unknown recursion identifier ");
            sb.append(this);
            if (proposals.size() > 0) {
                sb.append(". Did you mean");
                for (final Declaration prop: proposals)
                    sb.append(Globals.getNewline()).append("  - ").append(prop);
            }

            throw new ParseException(sb.toString());
        }
        final Action prefix = new SimpleAction(new ConstChannel(name));
        final Expression stopExpression = Expression.getExpression(new StopExpr());
        return Expression.getExpression(new PrefixExpr(prefix, stopExpression));
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> params) {
        final List<Value> newParameters = new ArrayList<Value>(parameters.size());
        boolean changed = false;
        for (final Value param: parameters) {
            final Value newParam = param.instantiate(params);
            if (!changed && !newParam.equals(param))
                changed = true;
            newParameters.add(newParam);
        }

        if (!changed)
            return this;

        return Expression.getExpression(new UnknownString(name, newParameters));
    }

    @Override
    public Expression insertParameters(List<Parameter> params) throws ParseException {
        final List<Value> newParameters = new ArrayList<Value>(parameters.size());
        boolean changed = false;
        for (final Value param: parameters) {
            final Value newParam = param.insertParameters(params);
            if (!changed && !newParam.equals(param))
                changed = true;
            newParameters.add(newParam);
        }

        if (!changed)
            return this;

        return Expression.getExpression(new UnknownString(name, newParameters));
    }

    @Override
    public String toString() {
        if (parameters.size() == 0)
            return name;

        final StringBuilder sb = new StringBuilder(name);
        sb.append(parameters);

        return sb.toString();
    }

    @Override
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 8;
        result = PRIME * result + ((name == null) ? 0 : name.hashCode());
        result = PRIME * result + ((parameters == null) ? 0 : parameters.hashCode());
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
        final UnknownString other = (UnknownString) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        return true;
    }

}
