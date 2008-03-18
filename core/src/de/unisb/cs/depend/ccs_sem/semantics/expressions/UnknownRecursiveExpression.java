package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


/**
 * Is later (by replaceRecursion()) replaced by a RecursiveExpression.
 *
 * @author Clemens Hammacher
 */
public class UnknownRecursiveExpression extends Expression {

    private final String name;
    private final List<Value> parameters;

    public UnknownRecursiveExpression(String name, List<Value> parameters) {
        super();
        this.name = name;
        this.parameters = parameters;
    }

    public UnknownRecursiveExpression(String name) {
        super();
        this.name = name;
        this.parameters = Collections.emptyList();
    }

    @Override
    public Collection<Expression> getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<Transition> evaluate0() {
        throw new UnsupportedOperationException();
    }

    /**
     * Replaces this {@link UnknownRecursiveExpression} by either a {@link RecursiveExpression} or
     * throws a {@link ParseException}.
     */
    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables) throws ParseException {
        for (final ProcessVariable proc: processVariables) {
            if (proc.getName().equals(name) && proc.getParamCount() == parameters.size()) {
                // check if parameters match
                // this possibly throws a ParseException
                proc.checkMatch(parameters);
                final RecursiveExpression newExpr = new RecursiveExpression(proc, parameters);
                return ExpressionRepository.getExpression(newExpr);
            }
        }

        // search for possible matches
        final List<ProcessVariable> proposals = new ArrayList<ProcessVariable>();
        for (final ProcessVariable proc: processVariables)
            if (proc.getName().equalsIgnoreCase(name))
                proposals.add(proc);
        final StringBuilder sb = new StringBuilder("Unknown recursion identifier ");
        sb.append(this);
        if (proposals.size() > 1) {
            sb.append(". Did you mean");
            if (proposals.size() == 1)
                sb.append(' ').append(proposals.get(0).getFullName()).append('?');
            else
                for (final ProcessVariable prop: proposals)
                    sb.append(Globals.getNewline()).append("  - ").append(prop.getFullName());
        }

        throw new ParseException(sb.toString());
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> params) {
        // TODO check
        assert false;


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

        return ExpressionRepository.getExpression(new UnknownRecursiveExpression(name, newParameters));
    }

    @Override
    protected boolean isError0() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        if (parameters.size() == 0)
            return name;

        return name+parameters;
    }

    @Override
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + name.hashCode();
        result = PRIME * result + parameters.hashCode();
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
        final UnknownRecursiveExpression other = (UnknownRecursiveExpression) obj;
        // hashCode is cached, so we compare it first (it's cheap)
        if (hashCode() != other.hashCode())
            return false;
        if (!name.equals(other.name))
            return false;
        if (!parameters.equals(other.parameters))
            return false;
        return true;
    }

}
