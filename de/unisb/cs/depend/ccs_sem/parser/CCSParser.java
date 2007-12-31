package de.unisb.cs.depend.ccs_sem.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Assignment;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Semicolon;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpr;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class CCSParser implements Parser {

    public Program parse(List<Token> tokens) throws ParseException {
        List<Declaration> declarations = new ArrayList<Declaration>();
        
        int index = 0;
        
        // first, read the declarations
        while (index + 2 < tokens.size()) {
            ListIterator<Token> it = tokens.listIterator(index);
            Token token1 = it.next();
            Token token2 = it.next();
            Identifier identifier;
            List<String> parameters;
            Expression expr;
            if (!(token1 instanceof Identifier))
                break;
            identifier = (Identifier) token1;

            if ((token1 instanceof Identifier) && (token2 instanceof Assignment)) {
                expr = readExpression(it);
                parameters = Collections.emptyList();
            } else if ((token1 instanceof Identifier) && (token2 instanceof LBracket)) {
                parameters = readParameters(it);
                if (parameters == null || !(it.next() instanceof RBracket)
                    || !(it.next() instanceof Assignment))
                    break;
                expr = readExpression(it);
            } else {
                break;
            }
            
            if (it.hasNext()) {
                Token next = it.next();
                if (!(next instanceof Semicolon)) {
                    // TODO Exception
                }
            }
            index = it.nextIndex();
            declarations.add(new Declaration(identifier.getName(), parameters, expr));
        }
        
        // then, read the ccs expression
        ListIterator<Token> it = tokens.listIterator(index);
        Expression expr = readExpression(it);
        
        Program program = new Program(declarations, expr);
        
        if (!program.isRegular()) {
            // TODO Exception
        }
        
        return program;
    }

    private List<String> readParameters(ListIterator<Token> it) {
        // TODO read parameters up to the next RBracket
        return null;
    }

    private Expression readExpression(ListIterator<Token> name) {
        // TODO Auto-generated method stub
        return null;
    }

}
