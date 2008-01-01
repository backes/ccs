package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public interface Expression extends Cloneable {
    List<Transition> evaluate();

    Collection<Expression> getChildren();

    public Expression clone();

    /**
     * Replaces every {@link UnknownString} either by a {@link PrefixExpr} and
     * a {@link StopExpr}, or by a {@link RecursiveExpr}.
     * @return either itself (children may have changed) or a new created Expression
     * @throws ParseException 
     */
    Expression replaceRecursion(List<Declaration> declarations) throws ParseException;
    
}
