package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.PrefixExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;
import de.unisb.cs.depend.ccs_sem.utils.UniqueQueue;


public class ProcessVariable {

    private final String name;
    private final List<Parameter> parameters;
    private Expression value;

    public ProcessVariable(String name, List<Parameter> parameters,
            Expression value) {
        super();
        this.name = name;
        this.parameters = parameters;
        this.value = value;
    }

    /**
     * A ProcessVariable is guarded if there is at least one prefix before all
     * occurences of the ProcessVariable itself in the related expression.
     */
    public boolean isGuarded() {
        // a queue of expressions to check
        final Queue<Expression> queue = new UniqueQueue<Expression>();
        queue.add(value);

        Expression expr;
        while ((expr = queue.poll()) != null) {
            if (expr instanceof PrefixExpression)
                // then, it is guarded
                continue;
            // every RecursiveExpr has to be checked only once
            if (expr instanceof RecursiveExpression &&
                    ((RecursiveExpression)expr).getReferencedProcessVariable().equals(this))
                return false;

            queue.addAll(expr.getSubTerms());
        }

        // nothing bad found...
        return true;
    }

    /**
     * A ProcessVariable is regular if it does not contain a cycle of recursions
     * back to itself that contains parallel or restriction operators (so called
     * "static" operators).
     */
    public boolean isRegular() {
        // a queue of expressions to check
        final Queue<Expression> queue = new UniqueQueue<Expression>();
        queue.add(value);
        // queue of expressions that occured after static operators
        final Queue<Expression> afterStaticQueue = new UniqueQueue<Expression>();

        // first, search for all expressions that occure after static operators
        Expression expr;
        while ((expr = queue.poll()) != null) {
            if (expr instanceof ParallelExpression || expr instanceof RestrictExpression)
                afterStaticQueue.addAll(expr.getSubTerms());
            else
                queue.addAll(expr.getSubTerms());
        }

        // then, check these expressions for occurences of the current process
        // variable (recursive loop)
        while ((expr = afterStaticQueue.poll()) != null) {
            if (expr instanceof RecursiveExpression &&
                    ((RecursiveExpression) expr).getReferencedProcessVariable().equals(this))
                return false;
            afterStaticQueue.addAll(expr.getSubTerms());
        }

        // nothing bad found...
        return true;
    }

    /**
     * @return the name of the recursion variable
     */
    public String getName() {
        return name;
    }

    /**
     * @return the number of parameters of this recursion variable
     */
    public int getParamCount() {
        return parameters.size();
    }

    /**
     * @return the expression that this recursion variable represents
     */
    public Expression getValue() {
        return value;
    }

    public void replaceRecursion(List<ProcessVariable> processVariables) throws ParseException {
        value = value.replaceRecursion(processVariables);
    }

    /**
     * Checks if the value list matches the parameters of this process variable.
     *
     * @param values the list of values to check the parameters against
     * @throws ParseException if the values does not suit the parameters
     */
    public void checkMatch(List<Value> values) throws ParseException {
        // this method is only called if the parameter length matches
        assert parameters.size() == values.size();

        for (int i = 0; i < parameters.size(); ++i) {
            try {
                parameters.get(i).match(values.get(i));
            } catch (final ParseException e) {
                throw new ParseException("The type of parameter " + i + " does not fit: " + e.getMessage());
            }
        }
    }

    private boolean checkMatch0(List<Value> values) {
        try {
            checkMatch(values);
            return true;
        } catch (final ParseException e) {
            return false;
        }
    }

    public Expression instantiate(List<Value> values) {
        // first, assert that the values fit into the parameters
        assert checkMatch0(values);
        assert parameters.size() == values.size();

        // create the mapping from parameters to values
        Map<Parameter, Value> map = null;
        if (parameters.size() == 1) {
            map = Collections.singletonMap(parameters.get(0), values.get(0));
        } else {
            map = new HashMap<Parameter, Value>(values.size() * 3 / 2);
            for (int i = 0; i < parameters.size(); ++i) {
                map.put(parameters.get(i), values.get(i));
            }
        }

        return value.instantiate(map);
    }

    @Override
    public String toString() {
        if (parameters.size() == 0)
            return name + " = " + value;

        return name + parameters + " = " + value;
    }

    /**
     * @return the name together with the parameters
     */
    public String getFullName() {
        return parameters.isEmpty() ? name : name + parameters;
    }

    /**
     * Checks whether the given parameter values fit into the parameter ranges
     * of this process variable's parameters.
     *
     * @param parameterValues the values to check
     * @return <code>true</code> if the values fit into the parameter ranges,
     *         <code>false</code> otherwise
     */
    public boolean checkRanges(List<Value> parameterValues) {
        assert parameters.size() == parameterValues.size();

        for (int i = 0; i < parameters.size(); ++i) {
            assert parameterValues.get(i) instanceof ConstantValue;
            final Range range = parameters.get(i).getRange();
            if (range != null && !range.contains(parameterValues.get(i)))
                return false;
        }

        return true;
    }

    // NO HASHCODE COMPUTATION HERE. ONLY THE SAME DECLARATIONS ARE EQUAL!!
    // TODO really? then caching doesn't make much sense
}
