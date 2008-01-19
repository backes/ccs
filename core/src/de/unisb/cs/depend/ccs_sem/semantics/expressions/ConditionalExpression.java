package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.BooleanValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstBooleanValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterRefValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class ConditionalExpression extends Expression {

    private final Value condition;
    private final Expression consequence;

    public ConditionalExpression(Value condition, Expression consequence) {
        super();
        if (!(condition instanceof BooleanValue || condition instanceof ParameterRefValue))
            throw new IllegalArgumentException("Only BooleanValue or ParameterRefValue allowed");
        this.condition = condition;
        this.consequence = consequence;
    }

    @Override
    protected List<Transition> evaluate0() {
        assert condition instanceof ConstBooleanValue;

        if (((ConstBooleanValue)condition).getValue())
            return consequence.getTransitions();

        return Collections.emptyList();
    }

    @Override
    public Collection<Expression> getChildren() {
        assert condition instanceof ConstBooleanValue;

        if (((ConstBooleanValue)condition).getValue())
            return Collections.singleton(consequence);

        return Collections.emptySet();
    }

    @Override
    public Expression instantiate(Map<Parameter, Value> parameters) {
        final Value newCondition = condition.instantiate(parameters);
        final Expression newConsequence = consequence.instantiate(parameters);
        if (condition.equals(newCondition) && consequence.equals(newConsequence))
            return this;
        return new ConditionalExpression(newCondition, newConsequence);
    }

    @Override
    public Expression replaceRecursion(List<Declaration> declarations)
            throws ParseException {
        final Expression newConsequence = consequence.replaceRecursion(declarations);
        if (consequence.equals(newConsequence))
            return this;
        return new ConditionalExpression(condition, newConsequence);
    }

    @Override
    protected int hashCode0() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + condition.hashCode();
        result = prime * result + consequence.hashCode();
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
        final ConditionalExpression other = (ConditionalExpression) obj;
        if (!condition.equals(other.condition))
            return false;
        if (!consequence.equals(other.consequence))
            return false;
        return true;
    }

}
