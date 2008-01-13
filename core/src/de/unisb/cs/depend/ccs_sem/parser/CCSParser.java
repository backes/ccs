package de.unisb.cs.depend.ccs_sem.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Assignment;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Choice;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Comma;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Dot;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBrace;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Parallel;
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
import de.unisb.cs.depend.ccs_sem.semantics.types.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.Value;


public class CCSParser implements Parser {

    public Program parse(List<Token> tokens) throws ParseException {
        final ArrayList<Declaration> declarations = new ArrayList<Declaration>();

        int index = 0;

        // first, read the declarations
        while (index + 2 < tokens.size()) {
            final ListIterator<Token> it = tokens.listIterator(index);
            final Token token1 = it.next();
            final Token token2 = it.next();
            Identifier identifier;
            List<Value> parameters;
            Expression expr;
            if (!(token1 instanceof Identifier))
                break;
            identifier = (Identifier) token1;

            if (token2 instanceof Assignment) {
                expr = readExpression(it);
                parameters = Collections.emptyList();
            } else if (token2 instanceof LBracket) {
                parameters = readParameters(it);
                if (!it.hasNext() || !(it.next() instanceof Assignment))
                    break;
                expr = readExpression(it);
            } else {
                break;
            }

            if (!it.hasNext() || !(it.next() instanceof Semicolon))
                throw new ParseException("Expected ';' after this declaration");

            index = it.nextIndex();

            // check if a declaration with the same name and number of parameters is already known
            for (final Declaration decl: declarations)
                if (decl.getName().equals(identifier.getName()) && decl.getParamNr() == parameters.size())
                    throw new ParseException("Dublicate recursion variable definition ("
                        + identifier.getName() + "[" + parameters.size() + "]");

            declarations.add(new Declaration(identifier.getName(), parameters, expr));
        }

        declarations.trimToSize();

        // then, read the ccs expression
        final ListIterator<Token> it = tokens.listIterator(index);
        final Expression expr = readExpression(it);

        if (it.hasNext())
            throw new ParseException("Syntax error: Unexpected '" + it.next() + "'");

        final Program program = new Program(declarations, expr);

        if (!program.isRegular()) {
            throw new ParseException("Your recursive definitions are not regular");
        }

        return program;
    }

    /**
     * Read all parameters up to the next RBracket (this token is read too).
     */
    private List<Value> readParameters(ListIterator<Token> tokens) throws ParseException {
        final ArrayList<Value> parameters = new ArrayList<Value>();

        if (tokens.hasNext() && tokens.next() instanceof RBracket)
            return Collections.emptyList();

        tokens.previous();

        while (tokens.hasNext()) {
            Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                final String name = identifier.getName();
                if (!identifier.isValidParameter())
                    throw new ParseException("Invalid parameter: " + name);
                parameters.add(new ConstantValue(name));

                if (!tokens.hasNext())
                    throw new ParseException("Expected ']'");
                nextToken = tokens.next();

                if (nextToken instanceof RBracket) {
                    // save memory
                    parameters.trimToSize();
                    return parameters;
                }

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
                final Set<Action> restricted = readActionSet(tokens);
                expr = Expression.getExpression(new RestrictExpr(expr, restricted));
            } else {
                tokens.previous();
                break;
            }
        }

        return expr;
    }

    /**
     * Read all actions up to the next RBrace (this token is read too).
     */
    private Set<Action> readActionSet(ListIterator<Token> tokens) throws ParseException {
        final Set<Action> actions = new HashSet<Action>();

        if (tokens.hasNext() && tokens.next() instanceof RBrace)
            return Collections.emptySet();

        tokens.previous();

        while (tokens.hasNext()) {
            Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                final Action newAction = Action.newAction(identifier.getName());
                if (newAction instanceof TauAction)
                    throw new ParseException("Cannot hide tau action");
                actions.add(newAction);

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
                final Expression newExpr = readChoiceExpression(tokens);
                expr = Expression.getExpression(new ParallelExpr(expr, newExpr));
            } else {
                tokens.previous();
                break;
            }
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
                final Expression newExpr = readPrefixExpression(tokens);
                expr = Expression.getExpression(new ChoiceExpr(expr, newExpr));
            } else {
                tokens.previous();
                break;
            }
        }

        return expr;
    }

    /**
     * Read one prefix expression.
     */
    private Expression readPrefixExpression(ListIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            final Token nextToken = tokens.next();
            if (nextToken instanceof Identifier && tokens.hasNext()) {
                final Identifier identifier = (Identifier) nextToken;
                if (tokens.next() instanceof Dot) {
                    final Expression postfix = readPrefixExpression(tokens);
                    final Action action = Action.newAction(identifier.getName());
                    return Expression.getExpression(new PrefixExpr(action, postfix));
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
            final Token nextToken = tokens.next();

            if (nextToken instanceof Stop)
                return Expression.getExpression(new StopExpr());

            if (nextToken instanceof LParenthesis) {
                final Expression expr = readRestrictExpression(tokens);
                if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                    throw new ParseException("Expected ')'");
                return expr;
            }

            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier) nextToken;
                if (tokens.hasNext()) {
                    if (tokens.next() instanceof LBracket) {
                        final List<Value> parameters = readParameters(tokens);
                        final UnknownString newExpr = new UnknownString(identifier.getName(), parameters);
                        return Expression.getExpression(newExpr);
                    }
                    tokens.previous();
                }
                return Expression.getExpression(new UnknownString(identifier.getName()));
            }

            throw new ParseException("Syntax error. Unexpected '" + nextToken + "'");
        }

        throw new ParseException("Unexpected end of file");
    }

}
