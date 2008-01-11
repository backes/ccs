package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpr;


public class Declaration {

    private final String name;
    private final int paramNr;
    private Expression value;

    public Declaration(String name, int paramNr, Expression readyValue) {
        super();
        this.name = name;
        this.paramNr = paramNr;
        this.value = readyValue;
    }

    public Declaration(String name, List<Value> parameters, Expression value) {
        this(name, parameters.size(), value.insertParameters(parameters));
    }

    /**
     * A Declaration is regular iff it does not contain a cycle of recursions
     * back to itself that contains parallel or restriction operators (so called
     * "static" operators).
     * Besides, the value of the declaration must not be the recursion variable
     * itself.
     */
    public boolean isRegular() {
        // check the second condition first (easier)
        if (value instanceof RecursiveExpr) {
            final RecursiveExpr recExpr = (RecursiveExpr) value;
            if (recExpr.getReferencedDeclaration().equals(this))
                return false;
        }

        // then, check for a recursive loop back to this declaration, that
        // contains parallel or restriction operator(s)

        // every expression has to be checked only once
        final Set<Expression> checked = new HashSet<Expression>();
        // a queue of expressions to check
        final Queue<Expression> queue = new ArrayDeque<Expression>();
        // queue of expressions that occured after static operators
        final Queue<Expression> afterStaticQueue = new ArrayDeque<Expression>();
        queue.add(value);

        // first, search for all expressions that occure after static operators
        while (!queue.isEmpty()) {
            final Expression expr = queue.poll();
            if (checked.add(expr)) {
                // not checked before...
                if (expr instanceof ParallelExpr || expr instanceof RestrictExpr) {
                    afterStaticQueue.addAll(expr.getChildren());
                } else {
                    queue.addAll(expr.getChildren());
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
                } else {
                    afterStaticQueue.addAll(expr.getChildren());
                }
            }
        }

        // nothing bad found...
        return true;
    }

    public String getName() {
        return name;
    }

    public int getParamNr() {
        return paramNr;
    }

    public Expression getValue() {
        return value;
    }

    public void replaceRecursion(List<Declaration> declarations) throws ParseException {
        value = value.replaceRecursion(declarations);
    }

    @Override
    public String toString() {
        return name + "[" + paramNr + "] = " + value;
    }

    // TODO really?
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
