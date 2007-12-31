package de.unisb.cs.depend.ccs_sem.semantics.expressions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class RecursiveExpr extends AbstractExpression {
    
    private Declaration decl;
    private List<String> parameters;

    public RecursiveExpr(Declaration decl, List<String> parameters) {
        super();
        this.decl = decl;
        this.parameters = parameters;
    }

    public Collection<Expression> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<Transition> evaluate0() {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression replaceRecursion(List<Declaration> declarations) {
        // TODO Auto-generated method stub
        return null;
    }

}
