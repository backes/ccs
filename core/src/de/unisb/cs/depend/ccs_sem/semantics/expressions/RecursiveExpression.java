package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class RecursiveExpression extends Expression {

    private final Declaration referencedDeclaration;
    private final List<Value> parameterValues;
    private Expression instantiatedExpression = null;

    public RecursiveExpression(Declaration referencedDeclaration, List<Value> parameters) {
        super();
        this.referencedDeclaration = referencedDeclaration;
        this.parameterValues = parameters;
    }

    /**
     * Note: The returned list must not be changed!
     */
    public List<Value> getParameters() {
        return parameterValues;
    }

    public Declaration getReferencedDeclaration() {
        return referencedDeclaration;
    }

    /**
     * Creates the instantiated {@link Expression} of this {@link RecursiveExpression},
     * i.e. the {@link Expression} of the referenced {@link Declaration},
     * instantiated by the parameters of this {@link RecursiveExpression}.
     * If the parameters are not in the valid {@link Range} of the
     * {@link Declaration}'s {@link Parameter}s, an {@link ErrorExpression} is
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
                ? referencedDeclaration.checkRanges(parameterValues) : true;
            instantiatedExpression = rangesOK
                ? referencedDeclaration.instantiate(parameterValues)
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
        return Collections.singleton(referencedDeclaration.getValue());
    }

    @Override
    protected List<Transition> evaluate0() {
        return getInstantiatedExpression().getTransitions();
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations) {
        // nothing to do here
        return this;
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> params) {
        final List<Value> newParameters = new ArrayList<Value>(parameterValues.size());
        boolean changed = false;
        for (final Value param: parameterValues) {
            final Value newParam = param.instantiate(params);
            if (!changed && !newParam.equals(param))
                changed = true;
            newParameters.add(newParam);
        }

        if (!changed)
            return this;

        return ExpressionRepository.getExpression(new RecursiveExpression(referencedDeclaration, newParameters));
    }

    @Override
    protected boolean isError0() {
        return getInstantiatedExpression().isError();
    }

    @Override
    public String toString() {
        if (parameterValues.size() == 0)
            return referencedDeclaration.getName();

        return referencedDeclaration.getName() + parameterValues;
    }

    @Override
    protected int hashCode0() {
        final int PRIME = 31;
        int result = 5;
        result = PRIME * result + referencedDeclaration.hashCode();
        result = PRIME * result + parameterValues.hashCode();
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
        final RecursiveExpression other = (RecursiveExpression) obj;
        // hashCode is cached, so we compare it first (it's cheap)
        if (hashCode() != other.hashCode())
            return false;
        if (!referencedDeclaration.equals(other.referencedDeclaration))
            return false;
        if (!parameterValues.equals(other.parameterValues))
            return false;
        return true;
    }

}
