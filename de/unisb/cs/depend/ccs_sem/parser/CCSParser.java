package de.unisb.cs.depend.ccs_sem.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Assignment;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Comma;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Dot;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBrace;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Parallel;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Choice;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RBrace;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Restrict;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Semicolon;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Stop;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ChoiceExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.PrefixExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.StopExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.UnknownString;
import de.unisb.cs.depend.ccs_sem.semantics.types.Action;
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
                if (!(it.next() instanceof Assignment))
                    break;
                expr = readExpression(it);
            } else {
                break;
            }
            
            if (it.hasNext()) {
                Token next = it.next();
                if (!(next instanceof Semicolon))
                    throw new ParseException("Expected ';' after this declaration");
            }
            index = it.nextIndex();
            declarations.add(new Declaration(identifier.getName(), parameters, expr));
        }
        
        // then, read the ccs expression
        ListIterator<Token> it = tokens.listIterator(index);
        Expression expr = readExpression(it);
        
        if (it.hasNext())
            throw new ParseException("Syntax error: Unexpected '" + it.next() + "'");
        
        Program program = new Program(declarations, expr);
        
        if (!program.isRegular()) {
            // TODO Exception (or move this code to somewhere else??)
        }
        
        return program;
    }

    /**
     * Read all parameters up to the next RBracket (this token is read too).
     */
    private List<String> readParameters(ListIterator<Token> tokens) throws ParseException {
        List<String> parameters = new ArrayList<String>();
        
        if (tokens.hasNext() && tokens.next() instanceof RBracket)
            return Collections.emptyList();

        tokens.previous();
        
        while (tokens.hasNext()) {
            Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                Identifier identifier = (Identifier)nextToken;
                String name = identifier.getName();
                if (!identifier.isValidParameter())
                    throw new ParseException("Invalid parameter: " + name);
                parameters.add(name);

                if (!tokens.hasNext())
                    throw new ParseException("Expected ']'");
                nextToken = tokens.next();
                
                if (nextToken instanceof RBracket)
                    return parameters;
                
                if (!(nextToken instanceof Comma))
                    throw new ParseException("Expected ',' or ']'");
            } else
                throw new ParseException("Expected identifier");
        }

        throw new ParseException("Expected ']'");
    }

    /**
     * Read one "main expression".
     */
    private Expression readExpression(ListIterator<Token> tokens) throws ParseException {
        // the topmost operator is restriction:
        return readRestrictExpression(tokens);
    }

    /**
     * Read one restriction expression.
     */
    private Expression readRestrictExpression(ListIterator<Token> tokens) throws ParseException {
        Expression expr = readParallelExpression(tokens);
        while (tokens.hasNext()) {
            if (tokens.next() instanceof Restrict) {
                if (!(tokens.next() instanceof LBrace))
                    throw new ParseException("Expected '{'");
                Set<Action> restricted = readActionSet(tokens);
                expr = new RestrictExpr(expr, restricted);
            } else
                tokens.previous();
        }

        return expr;
    }

    /**
     * Read all actions up to the next RBrace (this token is read too).
     */
    private Set<Action> readActionSet(ListIterator<Token> tokens) throws ParseException {
        Set<Action> actions = new HashSet<Action>();
        
        if (tokens.hasNext() && tokens.next() instanceof RBrace)
            return Collections.emptySet();

        tokens.previous();
        
        while (tokens.hasNext()) {
            Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                Identifier identifier = (Identifier)nextToken;
                actions.add(Action.newAction(identifier.getName()));

                if (!tokens.hasNext())
                    throw new ParseException("Expected '}'");
                nextToken = tokens.next();
                
                if (nextToken instanceof RBrace)
                    return actions;
                
                if (!(nextToken instanceof Comma))
                    throw new ParseException("Expected ',' or '}'");
            } else
                throw new ParseException("Expected action identifier");
        }

        throw new ParseException("Expected '}'");
    }

    /**
     * Read one parallel expression.
     */
    private Expression readParallelExpression(ListIterator<Token> tokens) throws ParseException {
        Expression expr = readChoiceExpression(tokens);
        while (tokens.hasNext()) {
            if (tokens.next() instanceof Parallel) {
                Expression newExpr = readChoiceExpression(tokens);
                expr = new ParallelExpr(expr, newExpr);
            } else
                tokens.previous();
        }

        return expr;
    }

    /**
     * Read one choice expression.
     */
    private Expression readChoiceExpression(ListIterator<Token> tokens) throws ParseException {
        Expression expr = readPrefixExpression(tokens);
        while (tokens.hasNext()) {
            if (tokens.next() instanceof Choice) {
                Expression newExpr = readPrefixExpression(tokens);
                expr = new ChoiceExpr(expr, newExpr);
            } else
                tokens.previous();
        }

        return expr;
    }

    /**
     * Read one prefix expression.
     */
    private Expression readPrefixExpression(ListIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            Token nextToken = tokens.next();
            if (nextToken instanceof Identifier && tokens.hasNext()) {
                Identifier identifier = (Identifier) nextToken;
                if (tokens.next() instanceof Dot) {
                    Expression postfix = readPrefixExpression(tokens);
                    Action action = Action.newAction(identifier.getName());
                    return new PrefixExpr(action, postfix);
                }
                // else put back the read tokens and let readBaseExpression() do the work
                tokens.previous();
            }
            tokens.previous();
        }
        
        return readBaseExpression(tokens);
    }

    /**
     * Read one base expression (stop, expression in parentheses, or an identifier).
     */
    private Expression readBaseExpression(ListIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            Token nextToken = tokens.next();
            
            if (nextToken instanceof Stop)
                return new StopExpr();
            
            if (nextToken instanceof LParenthesis) {
                Expression expr = readRestrictExpression(tokens);
                if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                    throw new ParseException("Expected ')'");
                return expr;
            }
            
            if (nextToken instanceof Identifier) {
                Identifier identifier = (Identifier) nextToken;
                if (tokens.hasNext()) {
                    if (tokens.next() instanceof LBracket) {
                        List<String> parameters = readParameters(tokens);
                        return new UnknownString(identifier.getName(), parameters);
                    }
                    tokens.previous();
                }
                return new UnknownString(identifier.getName());
            }
            
            throw new ParseException("Syntax error. Unexpected '" + nextToken + "'");
        }
        
        throw new ParseException("Unexpected end of file");
    }

}
