package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public interface Expression extends Cloneable {
    List<Transition> evaluate();

    Collection<Expression> getChildren();

    /**
     * An expression is regular iff <br />
     * - it does not contain parallel expressions or restriction <br />
     * OR <br />
     * - it does not contain recursion
     * 
     * <br /><br />
     * BETTER??: 
     * iff it does not contain recursion of parallel or restrictive expressions
     */
    // TODO too strong?
    boolean isRegular();
    
    public Expression clone();

    /**
     * Replaces every {@link UnknownString} either by a {@link PrefixExpr} and
     * a {@link StopExpr}, or by a {@link RecursiveExpr}.
     * @return either itself (children may have changed) or a new created Expression
     * @throws ParseException 
     */
    Expression replaceRecursion(List<Declaration> declarations) throws ParseException;
    
}
