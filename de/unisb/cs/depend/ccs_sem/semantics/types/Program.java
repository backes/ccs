package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpr;


public class Program {

    private List<Declaration> declarations;
    private Expression mainExpression;

    public Program(List<Declaration> declarations, Expression expr) throws ParseException {
        this.declarations = declarations;
        this.mainExpression = expr.clone().replaceRecursion(declarations);
    }
    
    @Override
    public String toString() {
        String newLine = System.getProperty("line.separator");
        assert newLine != null;
        
        StringBuilder sb = new StringBuilder();
        for (Declaration decl: declarations) {
            sb.append(decl).append(newLine);
        }
        if (declarations.size() > 0)
            sb.append(newLine);
        
        sb.append(mainExpression);
        
        return sb.toString();
    }
    
    /**
     * A program is regular iff every recursive definition is regular.
     * See {@link Declaration#isRegular(List)}.
     */
    public boolean isRegular() {
        for (Declaration decl: declarations)
            if (!decl.isRegular())
                return false;
        
        return true;
    }


}
