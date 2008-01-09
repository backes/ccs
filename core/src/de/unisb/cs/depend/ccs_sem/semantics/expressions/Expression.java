package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public abstract class Expression implements Cloneable {

    private static Map<Expression, Expression> repository
        = new HashMap<Expression, Expression>();

    private List<Transition> transitions = null;
    
    protected Expression() {
        // nothing to do
    }

    // TODO synchronized?? costs time, most propably not necessary
    public List<Transition> evaluate() {
        if (transitions == null) {
            transitions = evaluate0();
            assert transitions != null;
            
            // save memory
            if (transitions instanceof ArrayList) {
                ArrayList<Transition> list = (ArrayList<Transition>) transitions;
                list.trimToSize();
            }
                
        }
        
        return transitions;
    }

    protected abstract List<Transition> evaluate0();

    public abstract Collection<Expression> getChildren();

    @Override
    public Expression clone() {
        try {
            Expression cloned = (Expression) super.clone();
            // the clone is typically changed afterwards
            cloned.transitions = null;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new InternalSystemException("Expression cannot be cloned", e);
        }
    }

    /**
     * Replaces every {@link UnknownString} either by a {@link PrefixExpr} and
     * a {@link StopExpr}, or by a {@link RecursiveExpr}.
     * @return either itself (children may have changed) or a new created Expression
     * @throws ParseException 
     */
    public abstract Expression replaceRecursion(List<Declaration> declarations) throws ParseException;
    
    public static Expression getExpression(Expression expr) {
        Expression foundExpr = repository.get(expr);
        if (foundExpr != null)
            return foundExpr;
        
        // TODO deep search for known expressions
        
        repository.put(expr, expr);

        return expr;
    }

    public abstract Expression replaceParameters(List<Value> parameters);

    public abstract Expression insertParameters(List<Value> parameters);

}
