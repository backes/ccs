package de.unisb.cs.depend.ccs_sem.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Assignment;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Choice;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Comma;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Dot;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Exclamation;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.IntegerToken;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBrace;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Parallel;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.QuestionMark;
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
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.InputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.OutputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.SimpleAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstantValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.IntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.TauChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;

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
 *                          | action
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

        final ExtendedIterator<Token> it = new ExtendedIterator<Token>(tokens);

        try {
            // first, read the declarations
            int index = 0;
            try {
                while (it.hasNext()) {

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
            it.setPosition(index);
            final Expression expr = readExpression(it);

            if (it.hasNext())
                throw new ParseException("Syntax error: Unexpected '" + it.next() + "'");

            final Program program = new Program(declarations, expr);

            if (!program.isRegular()) {
                throw new ParseException("Your recursive definitions are not regular");
            }

            return program;
        } catch (final ParseException e) {
            e.setEnvironment(getEnvironment(tokens, it.previousIndex(), 5));
            throw e;
        }
    }

    private String getEnvironment(List<Token> tokens, int position, int width) {
        final int from = position < width ? 0 : position - width;
        final int to = position > tokens.size() - width ? tokens.size(): position + width;

        final StringBuilder environment = new StringBuilder();
        for (int i = from; i < to; ++i) {
            if (i == from)
                environment.append(' ');
            environment.append(tokens.get(i));
        }

        return environment.toString();
    }

    private Declaration readDeclaration(ExtendedIterator<Token> tokens) throws ParseException {
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
    private List<Parameter> readParameters(ExtendedIterator<Token> tokens) throws ParseException {
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

    private Parameter readParameter(ExtendedIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            final Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                final String name = identifier.getName();
                if ("i".equals(name))
                    throw new ParseException("'i' is no valid Parameter name.");
                return new Parameter(name);
            }
        }
        throw new ParseException("Expected a value here.");
    }

    /**
     * Read all parameter values up to the next RBracket (this token is read too).
     */
    private List<Value> readParameterValues(ExtendedIterator<Token> tokens) throws ParseException {
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

    private Value readValue(Token nextToken, ExtendedIterator<Token> tokens) throws ParseException {
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
    private Expression readExpression(ExtendedIterator<Token> tokens) throws ParseException {
        // the topmost operator is restriction:
        return readRestrictExpression(tokens);
    }

    /**
     * Read one restriction expression.
     */
    private Expression readRestrictExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Expression expr = readParallelExpression(tokens);
        while (tokens.hasNext() && tokens.peek() instanceof Restrict) {
            tokens.next();
            if (!tokens.hasNext() || !(tokens.next() instanceof LBrace))
                throw new ParseException("Expected '{'");
            final Set<Action> restricted = readActionSet(tokens);
            expr = Expression.getExpression(new RestrictExpr(expr, restricted));
        }

        return expr;
    }

    /**
     * Read all actions up to the next RBrace (this token is read too).
     */
    private Set<Action> readActionSet(ExtendedIterator<Token> tokens) throws ParseException {
        final Set<Action> actions = new HashSet<Action>();

        if (tokens.peek() instanceof RBrace) {
            tokens.next();
            return Collections.emptySet();
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

    private Action readAction(ExtendedIterator<Token> tokens, boolean tauAllowed) throws ParseException {
        if (tokens.hasNext()) {
            final Channel channel = readChannel(tokens);
            if (channel instanceof TauChannel) {
                if (!tauAllowed)
                    throw new ParseException("Tau action not allowed here");
                return TauAction.get();
            }
            if (tokens.hasNext()) {
                final Token nextToken = tokens.next();
                if (!tokens.hasNext()) {
                    tokens.previous();
                } else if (nextToken instanceof QuestionMark) {
                    if (tokens.peek() instanceof Identifier) {
                        final Parameter param = readParameter(tokens);
                        return new InputAction(channel, param);
                    }
                    final Value value = readInputValue(tokens, true);
                    return Action.getAction(new InputAction(channel, value));
                } else if (nextToken instanceof Exclamation) {
                    final Value value = readValue(tokens, true);
                    return Action.getAction(new OutputAction(channel, value));
                }
                tokens.previous();
            }
            return Action.getAction(new SimpleAction(channel));
        }
        throw new ParseException("Expected action identifier.");
    }

    private Value readInputValue(ExtendedIterator<Token> tokens, boolean allowNull) throws ParseException {
        if (tokens.peek() instanceof IntegerToken) {
            final IntegerToken intToken = (IntegerToken) tokens.next();
            return new IntegerValue(intToken.getValue());
        }
        if (allowNull)
            return null;
        throw new ParseException("Expected input value.");
    }

    private Value readValue(ExtendedIterator<Token> tokens, boolean allowNull) throws ParseException {
        if (tokens.hasNext()) {
            final Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                return new ConstantValue(identifier.getName());
            } else if (nextToken instanceof IntegerToken) {
                final IntegerToken intToken = (IntegerToken) nextToken;
                return new IntegerValue(intToken.getValue());
            }
            tokens.previous();
        }
        if (allowNull)
            return null;
        throw new ParseException("Expected value.");
    }

    private Channel readChannel(ExtendedIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            final Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                if ("i".equals(identifier.getName()))
                    return TauChannel.get();
                return new ConstChannel(identifier.getName());
            }
            tokens.previous();
        }
        throw new ParseException("Expected channel identifier.");
    }

    /**
     * Read one parallel expression.
     */
    private Expression readParallelExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Expression expr = readChoiceExpression(tokens);
        while (tokens.hasNext() && tokens.peek() instanceof Parallel) {
            tokens.next();
            final Expression newExpr = readChoiceExpression(tokens);
            expr = Expression.getExpression(new ParallelExpr(expr, newExpr));
        }

        return expr;
    }

    /**
     * Read one choice expression.
     */
    private Expression readChoiceExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Expression expr = readPrefixExpression(tokens);
        while (tokens.hasNext() && tokens.peek() instanceof Choice) {
            tokens.next();
            final Expression newExpr = readPrefixExpression(tokens);
            expr = Expression.getExpression(new ChoiceExpr(expr, newExpr));
        }

        return expr;
    }

    /**
     * Read one prefix expression.
     */
    private Expression readPrefixExpression(ExtendedIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            // this is not very nice: we have to save the iterator position to
            // (possibly) reset it (and that's not very nice too :'( )
            final int oldPosition = tokens.nextIndex();
            try {
                final Action action = readAction(tokens, true);
                if (tokens.hasNext() && tokens.peek() instanceof Dot) {
                    tokens.next();
                    Expression target = readPrefixExpression(tokens);
                    // allow the action to manipulate the target
                    target = action.manipulateTarget(target);
                    return Expression.getExpression(new PrefixExpr(action, target));
                }
                // if it was an InputAction or an OutputAction, it must be a
                // PrefixExpression (followed by Stop)
                // otherwise try to read the parameters
                if (action instanceof SimpleAction) {
                    List<Value> parameters = Collections.emptyList();
                    if (tokens.hasNext() && tokens.peek() instanceof LBracket) {
                        tokens.next();
                        parameters = readParameterValues(tokens);
                    }
                    return Expression.getExpression(new UnknownString(action.getLabel(), parameters));
                } else {
                    return Expression.getExpression(new PrefixExpr(action, StopExpr.get()));
                }
            } catch (final ParseException e) {
                // ignore this
            }

            // reset to old position
            tokens.setPosition(oldPosition);
        }

        return readBaseExpression(tokens);
    }

    /**
     * Read one base expression (stop, expression in parentheses, or an identifier/action).
     */
    private Expression readBaseExpression(ExtendedIterator<Token> tokens) throws ParseException {
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

            throw new ParseException("Syntax error. Unexpected '" + nextToken + "'");
        }

        throw new ParseException("Unexpected end of file");
    }

}
