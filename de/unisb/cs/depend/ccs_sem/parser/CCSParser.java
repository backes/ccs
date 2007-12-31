package de.unisb.cs.depend.ccs_sem.parser;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.Assignment;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Semicolon;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.semantics.Program;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpr;


public class CCSParser implements Parser {

    public Program parse(List<Token> tokens) {
        Map<Identifier, Expression> declarations = new HashMap<Identifier, Expression>();
        
        int index = 0;
        
        // first, read the declarations
        while (index + 2 < tokens.size()) {
            if ((tokens.get(index) instanceof Identifier) && (tokens.get(index+1) instanceof Assignment)) {
                Identifier identifier = (Identifier) tokens.get(index);
                ListIterator<Token> it = tokens.listIterator(index+2);
                Expression expr = readExpression(it);
                if (it.hasNext()) {
                    Token next = it.next();
                    if (!(next instanceof Semicolon)) {
                        // TODO Exception
                    }
                }
                if (!isRegularExpression(expr)) {
                    // TODO Exception
                }
                declarations.put(identifier, expr);
            }
        }
        
        // then, read the ccs expression
        ListIterator<Token> it = tokens.listIterator();
        Expression expr = readExpression(it);
        
        Program program = new Program(declarations, expr);
        
        return program;
    }

    private boolean isRegularExpression(Expression expr) {
        if ((expr instanceof ParallelExpr) || (expr instanceof RestrictExpr))
            return false;
        
        for (Expression expr2: expr.getChildren())
            if (!isRegularExpression(expr2))
                return false;
        
        return true;
    }

    private Expression readExpression(ListIterator<Token> name) {
        // TODO Auto-generated method stub
        return null;
    }

}
