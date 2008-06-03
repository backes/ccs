package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterOrProcessEqualsWrapper;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.ValueList;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class RecursiveExpression extends Expression {

    private final ProcessVariable referencedProcessVariable;
    protected final ValueList parameterValues;
    private Expression instantiatedExpression = null;

    public RecursiveExpression(ProcessVariable referencedProcessVariable, ValueList parameters) {
        super();
        this.referencedProcessVariable = referencedProcessVariable;
        this.parameterValues = parameters;
    }

    /**
     * Note: The returned list must not be changed!
     */
    public List<Value> getParameters() {
        return parameterValues;
    }

    public ProcessVariable getReferencedProcessVariable() {
        return referencedProcessVariable;
    }

    /**
     * Creates the instantiated {@link Expression} of this {@link RecursiveExpression},
     * i.e. the {@link Expression} of the referenced {@link ProcessVariable},
     * instantiated by the parameters of this {@link RecursiveExpression}.
     * If the parameters are not in the valid {@link Range} of the
     * {@link ProcessVariable}'s {@link Parameter}s, an {@link ErrorExpression} is
     * generated.
     *
     * @return the generated {@link Expression}
     */
    public Expression getInstantiatedExpression() {
        if (instantiatedExpression == null) {
            // if all parameters are fully instantiated, check if the parameters
            // are in the correct range. if not, we just do no tests, they are
            // done later, when the expression is further instantiated
            boolean readyForCheck = true;
            for (final Value value: parameterValues)
                if (!(value instanceof ConstantValue)) {
                    readyForCheck = false;
                    break;
                }

            final boolean rangesOK = readyForCheck
                ? referencedProcessVariable.checkRanges(parameterValues) : true;
            instantiatedExpression = rangesOK
                ? referencedProcessVariable.instantiate(parameterValues)
                : ErrorExpression.get();
        }

        return instantiatedExpression;
    }

    @Override
    public Collection<Expression> getChildren() {
        return Collections.singleton(getInstantiatedExpression());
    }

    @Override
    public Collection<Expression> getSubTerms() {
        return Collections.singleton(referencedProcessVariable.getValue());
    }

    @Override
    protected List<Transition> evaluate0() {
        return getInstantiatedExpression().getTransitions();
    }

    @Override
    public Expression replaceRecursion(List<ProcessVariable> processVariables) {
        // nothing to do here
        return this;
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> params) {
        final ValueList newParameters = new ValueList(parameterValues.size());
        boolean changed = false;
        for (final Value param: parameterValues) {
            Value newParam;
            try {
                newParam = param.instantiate(params);
            } catch (final ArithmeticError e) {
                return ErrorExpression.get();
            }
            if (!changed && !newParam.equals(param))
                changed = true;
            newParameters.add(newParam);
        }

        if (!changed)
            return this;

        return ExpressionRepository.getExpression(new RecursiveExpression(referencedProcessVariable, newParameters));
    }

    @Override
    public Set<Action> getAlphabet(Set<RecursiveExpressionAlphabetWrapper> alreadyIncluded) {
        final RecursiveExpressionAlphabetWrapper myWrapper = new RecursiveExpressionAlphabetWrapper(this);
        if (!alreadyIncluded.add(myWrapper))
            // no Collections.emptySet() here because it could be modified by the caller
            return new HashSet<Action>(0);
        final Set<Action> alphabet = getInstantiatedExpression().getAlphabet(alreadyIncluded);
        // we have to remove it afterwards, so that other branches evaluate the full alphabet
        alreadyIncluded.remove(myWrapper);
        return alphabet;
    }

    @Override
    protected boolean isError0() {
        return getInstantiatedExpression().isError();
    }

    @Override
    public String toString() {
        if (parameterValues.size() == 0)
            return referencedProcessVariable.getName();

        return referencedProcessVariable.getName() + parameterValues;
    }

    @Override
    public int hashCode(
            Map<ParameterOrProcessEqualsWrapper, Integer> parameterOccurences) {
        final boolean empty = parameterOccurences.isEmpty();
        if (empty && hash != 0)
            return hash;
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + referencedProcessVariable.hashCode(parameterOccurences);
        result = PRIME * result + parameterValues.hashCode(parameterOccurences);
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
        final RecursiveExpression other = (RecursiveExpression) obj;
        if (!referencedProcessVariable.equals(other.referencedProcessVariable, parameterOccurences))
            return false;
        if (!parameterValues.equals(other.parameterValues, parameterOccurences))
            return false;
        return true;
    }

    public static class RecursiveExpressionAlphabetWrapper {

        private final RecursiveExpression expression;

        public RecursiveExpressionAlphabetWrapper(
                RecursiveExpression recursiveExpression) {
            this.expression = recursiveExpression;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = expression.getReferencedProcessVariable().hashCode();
            for (final Value val: expression.parameterValues) {
                if (val instanceof Channel)
                    result = prime*result + val.hashCode();
            }
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
            final RecursiveExpressionAlphabetWrapper other =
                    (RecursiveExpressionAlphabetWrapper) obj;
            if (!expression.getReferencedProcessVariable().equals(other.expression.getReferencedProcessVariable()))
                return false;
            final Iterator<Value> it1 = expression.parameterValues.iterator();
            final Iterator<Value> it2 = other.expression.parameterValues.iterator();
            while (it1.hasNext()) {
                if (!it2.hasNext())
                    return false;
                final Value v1 = it1.next();
                final Value v2 = it2.next();
                if (v1 instanceof Channel && !v1.equals(v2))
                    return false;
            }
            if (it2.hasNext())
                return false;
            return true;
        }

        @Override
        public String toString() {
            return expression.toString();
        }

    }

}
