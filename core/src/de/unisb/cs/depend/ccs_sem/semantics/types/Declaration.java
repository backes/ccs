package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.PrefixExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpr;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;


public class Declaration {

    private final String name;
    private final List<Parameter> parameters;
    private Expression value;

    public Declaration(String name, List<Parameter> parameters,
            Expression value) {
        super();
        this.name = name;
        this.parameters = parameters;
        this.value = value;
    }

    /**
     * A Declaration is guarded if there is at least one prefix before all
     * occurences of the Declaration itself in the related expression.
     */
    public boolean isGuarded() {
        // every expression has to be checked only once
        final Set<Expression> checked = new HashSet<Expression>();
        // every declaration has to be checked only once, even if it occures
        // with different parameters
        final Set<Declaration> checkedDeclarations = new HashSet<Declaration>();
        checkedDeclarations.add(this);
        // a queue of expressions to check
        final Queue<Expression> queue = new ArrayDeque<Expression>();
        queue.add(value);

        while (!queue.isEmpty()) {
            final Expression expr = queue.poll();
            if (checked.add(expr)) {
                // not checked before...
                if (expr instanceof PrefixExpr)
                    // then, it is guarded
                    continue;
                // every RecursiveExpr has to be checked only once
                if (expr instanceof RecursiveExpr) {
                    final Declaration referencedDeclaration = ((RecursiveExpr)expr).getReferencedDeclaration();
                    if (referencedDeclaration.equals(this))
                        return false;
                    if (!checkedDeclarations.add(referencedDeclaration))
                        continue;
                }
                queue.addAll(expr.getSubTerms());
            }
        }

        // nothing bad found...
        return true;
    }

    /**
     * A Declaration is regular if it does not contain a cycle of recursions
     * back to itself that contains parallel or restriction operators (so called
     * "static" operators).
     */
    public boolean isRegular() {
        // every expression has to be checked only once
        final Set<Expression> checked = new HashSet<Expression>();
        // every declaration has to be checked only once, even if it occures
        // with different parameters
        final Set<Declaration> checkedDeclarations = new HashSet<Declaration>();
        checkedDeclarations.add(this);
        // a queue of expressions to check
        final Queue<Expression> queue = new ArrayDeque<Expression>();
        queue.add(value);
        // queue of expressions that occured after static operators
        final Queue<Expression> afterStaticQueue = new ArrayDeque<Expression>();

        // first, search for all expressions that occure after static operators
        while (!queue.isEmpty()) {
            final Expression expr = queue.poll();
            if (checked.add(expr)) {
                // not checked before...
                if (expr instanceof ParallelExpr || expr instanceof RestrictExpr) {
                    afterStaticQueue.addAll(expr.getSubTerms());
                } else {
                    // every RecursiveExpr has to be checked only once
                    if (expr instanceof RecursiveExpr)
                        if (!checkedDeclarations.add(((RecursiveExpr)expr).getReferencedDeclaration()))
                            continue;
                    queue.addAll(expr.getSubTerms());
                }
            }
        }
        checked.clear();

        // then, check these expressions for occurences of the current declaration (recursive loop)
        while (!afterStaticQueue.isEmpty()) {
            final Expression expr = afterStaticQueue.poll();
            if (checked.add(expr)) {
                // not checked before...
                if (expr instanceof RecursiveExpr) {
                    final RecursiveExpr recExpr = (RecursiveExpr) expr;
                    if (recExpr.getReferencedDeclaration().equals(this))
                        return false;
                }
                afterStaticQueue.addAll(expr.getSubTerms());
            }
        }

        // nothing bad found...
        return true;
    }

    public String getName() {
        return name;
    }

    public int getParamNr() {
        return parameters.size();
    }

    public Expression getValue() {
        return value;
    }

    public void replaceRecursion(List<Declaration> declarations) throws ParseException {
        value = value.replaceRecursion(declarations);
    }

    /**
     * Checks if the value list matches the parameters of this declaration.
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
    public Object getFullName() {
        return name + parameters;
    }

    public boolean checkRanges(List<Value> parameterValues) {
        assert parameters.size() == parameterValues.size();

        for (int i = 0; i < parameters.size(); ++i) {
            final Range range = parameters.get(i).getRange();
            if (range != null && !range.contains(parameterValues.get(i)))
                return false;
        }

        return true;
    }

    // TODO really? then caching doesn't make much sense
    // NO HASHCODE COMPUTATION HERE. ONLY THE SAME DECLARATIONS ARE EQUAL!!
    /*
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Declaration other = (Declaration) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (paramNr != other.paramNr)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
    */

}
