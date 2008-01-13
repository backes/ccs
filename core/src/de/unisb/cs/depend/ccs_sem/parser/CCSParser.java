package de.unisb.cs.depend.ccs_sem.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Assignment;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Choice;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Comma;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Dot;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.IntegerToken;
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
import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.IntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.value.Value;

/**
 * This Parser parses the following grammar:
 *
 * program            --> (declaration ";")*  expression
 * declaration        --> recursionVariable = expression
 * recursionVariable  --> identifier ( "[" ( ( identifier "," )* identifier)? "]"  )?
 * expression         --> restrictExpression
 * restrictExpression --> parallelExpression
 *                          | restrictExpression "\" "{" ( ( identidier "," )* identifier )? "}"
 * parallelExpression --> choiceExpression
 *                          | parallelExpression "|" choiceExpression
 * choiceExpression   --> prefixExpression
 *                          | choiceExpression "+" prefixExpression
 * prefixExpression   --> baseExpression
 *                          | action "." prefixExpression
 * baseExpression     --> "0"
 *                          | "(" expression ")"
 *                          | recursionVariable
 * action             --> identifier ( ("?" | "!") value )?
 * identifier         --> character ( digit | character ) *
 * character          --> "a" | ... | "z" | "A" | ... | "Z" | "_"
 * digit              --> "0" | ... | "9"
 * value              --> digit+ | identifier
 *
 *
 * @author Clemens Hammacher
 */
public class CCSParser implements Parser {

    public Program parse(Reader input) throws ParseException, LexException {
        return parse(new CCSLexer().lex(input));
    }

    public Program parse(String input) throws ParseException, LexException {
        return parse(new CCSLexer().lex(input));
    }
    public Program parse(List<Token> tokens) throws ParseException {
        final ArrayList<Declaration> declarations = new ArrayList<Declaration>();

        int index = 0;

        // first, read the declarations
        try {
            while (index < tokens.size()) {

                final ListIterator<Token> it = tokens.listIterator(index);
                final Declaration nextDeclaration = readDeclaration(it);

                if (!it.hasNext() || !(it.next() instanceof Semicolon))
                    throw new ParseException("Expected ';' after this declaration");

                index = it.nextIndex();

                // check if a declaration with the same name and number of parameters is already known
                for (final Declaration decl: declarations)
                    if (decl.getName().equals(nextDeclaration.getName())
                            && decl.getParamNr() == nextDeclaration.getParamNr())
                        throw new ParseException("Duplicate recursion variable definition ("
                            + nextDeclaration.getName() + "[" + nextDeclaration.getParamNr() + "]");

                declarations.add(nextDeclaration);
            }
        } catch (final ParseException e) {
            // abort reading declarations, start reading the main expression
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

    private Declaration readDeclaration(ListIterator<Token> tokens) throws ParseException {
        Token token1 = null;
        Token token2 = null;
        if (tokens.hasNext())
            token1 = tokens.next();
        if (tokens.hasNext())
            token2 = tokens.next();
        if (token1 == null || token2 == null)
            throw new ParseException("Expected declaration");

        Identifier identifier;
        List<Parameter> parameters;
        Expression expr;
        if (!(token1 instanceof Identifier))
            throw new ParseException("Expected declaration");
        identifier = (Identifier) token1;

        if (token2 instanceof Assignment) {
            expr = readExpression(tokens);
            parameters = Collections.emptyList();
        } else if (token2 instanceof LBracket) {
            parameters = readParameters(tokens);
            if (!tokens.hasNext() || !(tokens.next() instanceof Assignment))
                throw new ParseException("Expected declaration");
            expr = readExpression(tokens);
        } else {
            throw new ParseException("Expected declaration");
        }
        return new Declaration(identifier.getName(), parameters, expr);
    }

    /**
     * Read all parameters up to the next RBracket (this token is read too).
     */
    private List<Parameter> readParameters(ListIterator<Token> tokens) throws ParseException {
        final ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        final Set<String> readParameters = new HashSet<String>();

        if (tokens.hasNext()) {
            if (tokens.next() instanceof RBracket)
                return Collections.emptyList();
            tokens.previous();
        }

        while (tokens.hasNext()) {

            final Parameter nextParameter = readParameter(tokens);

            if (!readParameters.add(nextParameter.getName()))
                throw new ParseException("Duplicated parameter name: " + nextParameter.getName());
            parameters.add(nextParameter);

            if (!tokens.hasNext())
                throw new ParseException("Expected ']'");

            final Token nextToken = tokens.next();

            if (nextToken instanceof RBracket) {
                parameters.trimToSize();
                return parameters;
            }
            if (!(nextToken instanceof Comma))
                throw new ParseException("Expected ',' or ']'");
        }
        throw new ParseException("Expected ']'");
    }

    private Parameter readParameter(ListIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            final Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                final String name = identifier.getName();
                return new Parameter(name);
            }
        }
        throw new ParseException("Expected a value here.");
    }

    /**
     * Read all parameter values up to the next RBracket (this token is read too).
     */
    private List<Value> readParameterValues(ListIterator<Token> tokens) throws ParseException {
        final ArrayList<Value> parameters = new ArrayList<Value>();

        if (tokens.hasNext() && tokens.next() instanceof RBracket)
            return Collections.emptyList();

        tokens.previous();

        while (tokens.hasNext()) {

            Token nextToken = tokens.next();

            if (nextToken instanceof RBracket) {
                parameters.trimToSize();
                return parameters;
            }

            final Value nextValue = readValue(nextToken, tokens);

            parameters.add(nextValue);

            if (!tokens.hasNext())
                throw new ParseException("Expected ']'");

            nextToken = tokens.next();

            if (nextToken instanceof RBracket) {
                parameters.trimToSize();
                return parameters;
            }
            if (!(nextToken instanceof Comma))
                throw new ParseException("Expected ',' or ']'");
        }
        throw new ParseException("Expected ']'");
    }

    private Value readValue(Token nextToken, ListIterator<Token> tokens) throws ParseException {
        if (nextToken instanceof Identifier) {
            final Identifier identifier = (Identifier)nextToken;
            final String name = identifier.getName();
            return new ConstantValue(name);
        }
        if (nextToken instanceof IntegerToken) {
            final IntegerToken intToken = (IntegerToken) nextToken;
            return new IntegerValue(intToken.getValue());
        }
        throw new ParseException("Expected a value here.");
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

        if (tokens.hasNext()) {
            if (tokens.next() instanceof RBrace)
                return Collections.emptySet();
            tokens.previous();
        }

        while (tokens.hasNext()) {

            final Action newAction = readAction(tokens, false);

            actions.add(newAction);

            if (!tokens.hasNext())
                throw new ParseException("Expected '}'");
            final Token nextToken = tokens.next();

            if (nextToken instanceof RBrace)
                return actions;

            if (!(nextToken instanceof Comma))
                throw new ParseException("Expected ',' or '}'");
        }

        throw new ParseException("Expected '}'");
    }

    // TODO handle all action stuff. we should introduce tokens for '?' and '!'.
    private Action readAction(ListIterator<Token> tokens, boolean tauAllowed) throws ParseException {
        if (tokens.hasNext()) {
            final Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                final Action newAction = Action.newAction(identifier.getName());
                if (!tauAllowed && newAction instanceof TauAction)
                    throw new ParseException("Tau action not allowed here");
                return newAction;
            }
        }
        throw new ParseException("Expected action identifier");
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
                        final List<Value> parameters = readParameterValues(tokens);
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
