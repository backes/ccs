package de.unisb.cs.depend.ccs_sem.parser;

import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.And;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Colon;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Comma;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Division;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Dot;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Else;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Equals;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Exclamation;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.False;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Geq;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Greater;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.IntegerToken;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBrace;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.LeftShift;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Leq;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Less;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Minus;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Modulo;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Multiplication;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Neq;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Or;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Parallel;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Plus;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.QuestionMark;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RBrace;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RBracket;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RParenthesis;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Restrict;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.RightShift;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Semicolon;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Stop;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Then;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.True;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.When;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ChoiceExpr;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ConditionalExpression;
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
import de.unisb.cs.depend.ccs_sem.semantics.types.values.AddValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.AndValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.BooleanValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Channel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.CompValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConditionalValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstBooleanValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstIntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstStringValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.EqValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.IntegerValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.MultValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.NotValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.OrValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterRefChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ParameterRefValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ShiftValue;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.TauChannel;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.Value;

/**
 * This Parser parses the following grammar:
 *
 * program            --> (declaration ";")*  expression
 * declaration        --> recursionVariable = expression
 * recursionVariable  --> identifier ( "[" ( ( value "," )* value)? "]"  )?
 *
 * expression          --> restrictExpression
 * restrictExpression  --> parallelExpression
 *                          | restrictExpression "\" "{" ( ( identidier "," )* identifier )? "}"
 * parallelExpression  --> whenExpression
 *                          | parallelExpression "|" whenExpression
 * whenExpression      --> choiceExpression | "when" arithExpression whenExpression
 * choiceExpression    --> prefixExpression
 *                          | choiceExpression "+" prefixExpression
 * prefixExpression    --> baseExpression
 *                          | action "." prefixExpression
 * baseExpression      --> "0"
 *                          | "(" expression ")"
 *                          | recursionVariable
 *                          | action
 *
 * action              --> identifier ( "?" inputValue | "!" outputValue )?
 * identifier          --> character ( digit | character ) *
 * character           --> "a" | ... | "z" | "A" | ... | "Z" | "_"
 * digit               --> "0" | ... | "9"
 * inputValue          --> identifier | digit+
 * value               --> arithExpression
 * outputValue         --> identifier | "(" arithExpression ")"
 *
 * arithExpression     --> arithCond
 * arithCond           --> arithOr | arithOr "?" arithCond ":" arithCond
 * arithOr             --> arithAnd | arithOr "||" arithAnd
 * arithAnd            --> arithEq | arithAnd "&&" arithEq
 * arithEq             --> arithComp | arithEq ("==" | "!=" | "=") arithComp
 * arithComp           --> arithShift | arithShift ("<" | "<=" | ">" | ">=") arithShift
 * arithShift          --> arithAdd | arithShift (">>" | "<<") arithAdd
 * arithAdd            --> arithMult | arithAdd ("+" | "-") arithMult
 * arithMult           --> arithNot | arithMult ("*" | "/" | "%" | "mod") arithNot
 * arithNot            --> arithBase | "!" arithNot
 * arithBase           --> digit+ | "true" | "false" | "(" arithExpression ")" | identifier
 *
 *
 * @author Clemens Hammacher
 */
public class CCSParser implements Parser {

    // List of the currently read parameters, it's increased and decreased by
    // the read... methods. When a string is read, it is tried to match it
    // with one of these parameters *in ascending order*, that means from first
    // to last.
    private Deque<Parameter> parameters;

    public Program parse(Reader input) throws ParseException, LexException {
        return parse(new CCSLexer().lex(input));
    }

    public Program parse(String input) throws ParseException, LexException {
        return parse(new CCSLexer().lex(input));
    }

    // synchronized to make sure that this method is only called once at a time
    public synchronized Program parse(List<Token> tokens) throws ParseException {
        final ArrayList<Declaration> declarations = new ArrayList<Declaration>();
        parameters = new ArrayDeque<Parameter>();

        final ExtendedIterator<Token> it = new ExtendedIterator<Token>(tokens);

        try {
            // first, read the declarations
            int index = 0;
            while (it.hasNext()) {

                final Declaration nextDeclaration = readDeclaration(it);
                if (nextDeclaration == null)
                    break;

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
            if (i != from)
                environment.append(' ');
            environment.append(tokens.get(i));
        }

        return environment.toString();
    }

    /**
     * @return <code>null</code>, if there is no more declaration
     */
    private Declaration readDeclaration(ExtendedIterator<Token> tokens) throws ParseException {
        Token token1 = null;
        Token token2 = null;
        if (tokens.hasNext())
            token1 = tokens.next();
        if (tokens.hasNext())
            token2 = tokens.next();
        if (token1 == null || token2 == null)
            // there is no declaration
            return null;

        Identifier identifier;
        List<Parameter> myParameters;
        Expression expr;
        if (!(token1 instanceof Identifier))
            return null;
        identifier = (Identifier) token1;

        if (token2 instanceof Equals) {
            expr = readExpression(tokens);
            myParameters = Collections.emptyList();
        } else if (token2 instanceof LBracket) {
            myParameters = readParameters(tokens);
            if (!tokens.hasNext() || !(tokens.peek() instanceof Equals) || ((Equals)tokens.next()).isComp())
                throw new ParseException("Expected declaration");
            final Deque<Parameter> oldParameters = parameters;
            parameters = new ArrayDeque<Parameter>(myParameters);
            try {
                expr = readExpression(tokens);
            } finally {
                parameters = oldParameters;
            }
        } else
            return null;
        return new Declaration(identifier.getName(), myParameters, expr);
    }

    /**
     * Read all parameters up to the next RBracket (this token is read too).
     * @return <code>null</code> if there was an error
     */
    private List<Parameter> readParameters(ExtendedIterator<Token> tokens) {
        final ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        final Set<String> readParameters = new HashSet<String>();

        if (tokens.hasNext() && tokens.peek() instanceof RBracket) {
            tokens.next();
            return Collections.emptyList();
        }

        while (tokens.hasNext()) {

            // read one parameter
            Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                final String name = identifier.getName();
                if ("i".equals(name))
                    return null;
                if (!readParameters.add(name))
                    // duplicate parameter name
                    return null;
                final Parameter nextParameter = new Parameter(name);

                parameters.add(nextParameter);
            } else
                return null;

            if (!tokens.hasNext())
                return null;

            nextToken = tokens.next();

            if (nextToken instanceof RBracket) {
                parameters.trimToSize();
                return parameters;
            }
            if (!(nextToken instanceof Comma))
                return null;
        }
        return null;
    }

    /**
     * Read all parameter values up to the next RBracket (this token is read too).
     */
    private List<Value> readParameterValues(ExtendedIterator<Token> tokens) throws ParseException {
        final ArrayList<Value> parameters = new ArrayList<Value>();

        if (tokens.hasNext() && tokens.peek() instanceof RBracket) {
            tokens.next();
            return Collections.emptyList();
        }

        while (tokens.hasNext()) {

            if (tokens.peek() instanceof RBracket) {
                tokens.next();
                parameters.trimToSize();
                return parameters;
            }

            final Value nextValue = readArithmeticExpression(tokens);

            parameters.add(nextValue);

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
            final List<Action> restricted = readRestrictionActionSet(tokens);
            expr = Expression.getExpression(new RestrictExpr(expr, restricted));
        }

        return expr;
    }

    /**
     * Read all actions up to the next RBrace (this token is read too).
     */
    private List<Action> readRestrictionActionSet(ExtendedIterator<Token> tokens) throws ParseException {
        final ArrayList<Action> actions = new ArrayList<Action>();

        if (tokens.hasNext() && tokens.peek() instanceof RBrace) {
            tokens.next();
            return Collections.emptyList();
        }

        while (tokens.hasNext()) {

            final Action newAction = readAction(tokens, false);

            actions.add(newAction);

            if (!tokens.hasNext())
                throw new ParseException("Expected '}'");
            final Token nextToken = tokens.next();

            if (nextToken instanceof RBrace) {
                actions.trimToSize();
                return actions;
            }

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
                if (nextToken instanceof QuestionMark) {
                    // read the input value
                    if (tokens.hasNext()) {
                        if (tokens.peek() instanceof Identifier) {
                            final Identifier identifier = (Identifier)tokens.next();
                            return new InputAction(channel, new Parameter(identifier.getName()));
                        } else {
                            // save the old position
                            final int oldPosition = tokens.nextIndex();
                            try {
                                final Value value = readArithmeticBaseExpression(tokens);
                                return new InputAction(channel, value);
                            } catch (final ParseException e) {
                                tokens.setPosition(oldPosition);
                            }
                        }
                    }
                    return new InputAction(channel, (Value)null);
                } else if (nextToken instanceof Exclamation) {
                    final Value value = readOutputValue(tokens, true, true);
                    return new OutputAction(channel, value);
                }
                tokens.previous();
            }
            return new SimpleAction(channel);
        }
        throw new ParseException("Expected action identifier.");
    }

    private Value readOutputValue(ExtendedIterator<Token> tokens, boolean allowNull,
            boolean needsParenthesisAroundExpressions) throws ParseException {
        if (allowNull) {
            final int oldPosition = tokens.nextIndex();
            try {
                final Value value = needsParenthesisAroundExpressions
                    ? readArithmeticBaseExpression(tokens) : readArithmeticExpression(tokens);
                return value;
            } catch (final ParseException e) {
                tokens.setPosition(oldPosition);
                return null;
            }
        }

        final Value value = needsParenthesisAroundExpressions
            ? readArithmeticBaseExpression(tokens) : readArithmeticExpression(tokens);
        return value;
    }

    private Channel readChannel(ExtendedIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            final Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                if ("i".equals(identifier.getName()))
                    return TauChannel.get();
                for (final Parameter param: parameters) {
                    if (param.getName().equals(identifier.getName())) {
                        param.setType(Parameter.Type.CHANNEL);
                        return new ParameterRefChannel(param);
                    }
                }
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
        Expression expr = readWhenExpression(tokens);
        while (tokens.hasNext() && tokens.peek() instanceof Parallel) {
            tokens.next();
            final Expression newExpr = readWhenExpression(tokens);
            expr = ParallelExpr.create(expr, newExpr);
        }

        return expr;
    }

    private Expression readWhenExpression(ExtendedIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext() && tokens.peek() instanceof When) {
            tokens.next();
            final Value condition = readArithmeticExpression(tokens);
            ensureBoolean(condition, "Expected boolean expression after 'when'");

            // if there is a "then" now, ignore it
            if (tokens.hasNext() && tokens.peek() instanceof Then)
                tokens.next();
            final Expression consequence = readWhenExpression(tokens);

            Expression condExpr = ConditionalExpression.create(condition, consequence);

            // we allow an "else" here to declare an alternative, but internally,
            // it is mapped to a "(when (x) <consequence>) + (when (!x) <alternative>)"
            if (tokens.hasNext() && tokens.peek() instanceof Else) {
                tokens.next();
                Expression alternative = readWhenExpression(tokens);
                // build negated condition
                final Value negatedCondition = condition instanceof NotValue
                    ? ((NotValue)condition).getNegatedValue()
                    : NotValue.create(condition);
                alternative = ConditionalExpression.create(negatedCondition, alternative);
                condExpr = ChoiceExpr.create(condExpr, alternative);
            }
            return condExpr;
        }
        return readChoiceExpression(tokens);
    }

    /**
     * Read one choice expression.
     */
    private Expression readChoiceExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Expression expr = readPrefixExpression(tokens);
        while (tokens.hasNext() && tokens.peek() instanceof Plus) {
            tokens.next();
            final Expression newExpr = readPrefixExpression(tokens);
            expr = ChoiceExpr.create(expr, newExpr);
        }

        return expr;
    }

    /**
     * Read one prefix expression.
     */
    private Expression readPrefixExpression(ExtendedIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            // this is not very nice: we have to save the iterator position to
            // (possibly) reset it
            final int oldPosition = tokens.nextIndex();
            try {
                final Action action = readAction(tokens, true);
                if (tokens.hasNext() && tokens.peek() instanceof Dot) {
                    tokens.next();
                    // if the read action is an InputAction with a parameter, we
                    // have to add this parameter to the list of parameters
                    Parameter newParam = null;
                    if (action instanceof InputAction) {
                        newParam = ((InputAction)action).getParameter();
                        if (newParam != null) {
                            // add the new parameter to the front of the deque
                            parameters.addFirst(newParam);
                        }
                    }
                    final Expression target = readPrefixExpression(tokens);
                    if (newParam != null) {
                        final Parameter removedParam = parameters.pollFirst();
                        assert removedParam == newParam;
                    }
                    return Expression.getExpression(new PrefixExpr(action, target));
                }
                // if it was not a SimpleAction, it must be a
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
                final Expression expr = readExpression(tokens);
                if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                    throw new ParseException("Expected ')'");
                return expr;
            }

            throw new ParseException("Syntax error. Unexpected '" + nextToken + "'");
        }

        throw new ParseException("Unexpected end of file");
    }

    private Value readArithmeticExpression(ExtendedIterator<Token> tokens) throws ParseException {
        return readArithmeticConditionalExpression(tokens);
    }

    private Value readArithmeticConditionalExpression(ExtendedIterator<Token> tokens) throws ParseException {
        final Value orValue = readArithmeticOrExpression(tokens);
        if (tokens.hasNext() && tokens.peek() instanceof QuestionMark) {
            tokens.next();
            ensureBoolean(orValue, "Boolean expression required before '?:' construct.");
            final Value thenValue = readArithmeticConditionalExpression(tokens);
            if (!tokens.hasNext() || !(tokens.next() instanceof Colon))
                throw new ParseException("Expected ':'");
            final Value elseValue = readArithmeticConditionalExpression(tokens);
            ensureEqualTypes(thenValue, elseValue, "Expression in '?:' construct must have the same type.");
            return ConditionalValue.create(orValue, thenValue, elseValue);
        }

        return orValue;
    }

    private Value readArithmeticOrExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Value value = readArithmeticAndExpression(tokens);
        while (tokens.hasNext() && tokens.peek() instanceof Or) {
            tokens.next();
            ensureBoolean(value, "Boolean expression required before '||'.");
            final Value secondValue = readArithmeticAndExpression(tokens);
            ensureBoolean(secondValue, "Boolean expression required after '||'.");
            value = OrValue.create(value, secondValue);
        }

        return value;
    }

    private Value readArithmeticAndExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Value value = readArithmeticEqExpression(tokens);
        while (tokens.hasNext() && tokens.peek() instanceof And) {
            tokens.next();
            ensureBoolean(value, "Boolean expression required before '&&'.");
            final Value secondValue = readArithmeticEqExpression(tokens);
            ensureBoolean(secondValue, "Boolean expression required after '&&'.");
            value = AndValue.create(value, secondValue);
        }

        return value;
    }

    private Value readArithmeticEqExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Value value = readArithmeticCompExpression(tokens);
        while (tokens.hasNext() && tokens.peek() instanceof Equals || tokens.peek() instanceof Neq) {
            final boolean isNeq = tokens.next() instanceof Neq;
            final Value secondValue = readArithmeticCompExpression(tokens);
            ensureEqualTypes(value, secondValue, "Values to compare must have the same type.");
            value = EqValue.create(value, secondValue, isNeq);
        }

        return value;
    }

    private Value readArithmeticCompExpression(ExtendedIterator<Token> tokens) throws ParseException {
        final Value value = readArithmeticShiftExpression(tokens);
        if (tokens.hasNext()) {
            final Token nextToken = tokens.next();
            CompValue.Type type = null;
            if (nextToken instanceof Less)
                type = CompValue.Type.LESS;
            else if (nextToken instanceof Leq)
                type = CompValue.Type.LEQ;
            else if (nextToken instanceof Geq)
                type = CompValue.Type.GEQ;
            else if (nextToken instanceof Greater)
                type = CompValue.Type.GREATER;

            if (type != null) {
                ensureInteger(value, "Only integer values can be compared.");
                final Value secondValue = readArithmeticShiftExpression(tokens);
                ensureInteger(secondValue, "Only integer values can be compared.");
                return CompValue.create(value, secondValue, type);
            }
            tokens.previous();
        }

        return value;
    }

    private Value readArithmeticShiftExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Value value = readArithmeticAddExpression(tokens);
        while (tokens.hasNext() && (tokens.peek() instanceof LeftShift
                || tokens.peek() instanceof RightShift)) {
            ensureInteger(value, "Only integer values can be shifted.");
            final boolean shiftRight = tokens.next() instanceof RightShift;
            final Value secondValue = readArithmeticAddExpression(tokens);
            ensureInteger(secondValue, "Shifting width must be an integer.");
            value = ShiftValue.create(value, secondValue, shiftRight);
        }

        return value;
    }

    private Value readArithmeticAddExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Value value = readArithmeticMultExpression(tokens);
        while (tokens.hasNext() && (tokens.peek() instanceof Plus
                || tokens.peek() instanceof Minus)) {
            ensureInteger(value, "Both sides of an addition must be integers.");
            final boolean isSubtraction = tokens.next() instanceof Minus;
            final Value secondValue = readArithmeticMultExpression(tokens);
            ensureInteger(secondValue, "Both sides of an addition must be integers.");
            value = AddValue.create(value, secondValue, isSubtraction);
        }

        return value;
    }

    private Value readArithmeticMultExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Value value = readArithmeticNotExpression(tokens);
        while (tokens.hasNext()) {
            final Token nextToken = tokens.next();
            MultValue.Type type = null;
            if (nextToken instanceof Multiplication)
                type = MultValue.Type.MULT;
            else if (nextToken instanceof Division)
                type = MultValue.Type.DIV;
            else if (nextToken instanceof Modulo)
                type = MultValue.Type.MOD;

            if (type == null) {
                tokens.previous();
                break;
            }
            ensureInteger(value, "Both sides of a multiplication/division must be integer expressions.");
            final Value secondValue = readArithmeticNotExpression(tokens);
            ensureInteger(secondValue, "Both sides of a multiplication/division must be integer expressions.");
            value = MultValue.create(value, secondValue, type);
        }

        return value;
    }

    private Value readArithmeticNotExpression(ExtendedIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext() && tokens.peek() instanceof Exclamation) {
            final Value negatedValue = readArithmeticNotExpression(tokens);
            ensureBoolean(negatedValue, "The negated value must be a boolean expression.");
            return NotValue.create(negatedValue);
        }
        return readArithmeticBaseExpression(tokens);
    }

    private Value readArithmeticBaseExpression(ExtendedIterator<Token> tokens) throws ParseException {
        if (!tokens.hasNext())
            throw new ParseException("Unexpected EOF.");
        final Token nextToken = tokens.next();
        if (nextToken instanceof IntegerToken)
            return new ConstIntegerValue(((IntegerToken)nextToken).getValue());
        // a stop is the integer "0" here...
        if (nextToken instanceof Stop)
            return new ConstIntegerValue(0);
        if (nextToken instanceof True)
            return ConstBooleanValue.get(true);
        if (nextToken instanceof False)
            return ConstBooleanValue.get(false);
        if (nextToken instanceof Identifier) {
            // search if this identifier is a parameter
            final String name = ((Identifier)nextToken).getName();
            for (final Parameter param: parameters)
                if (param.getName().equals(name))
                    return new ParameterRefValue(param);
            return new ConstStringValue(name);
        }
        if (nextToken instanceof LParenthesis) {
            final Value value = readArithmeticExpression(tokens);
            if (!tokens.hasNext() || !(tokens.peek() instanceof RParenthesis))
                throw new ParseException("Expected ')'.");
            return value;
        }

        throw new ParseException("Expected some expression here.");
    }

    private void ensureEqualTypes(Value value1, Value value2, String message) throws ParseException {
        if (value1 instanceof IntegerValue && value2 instanceof IntegerValue)
            return;
        if (value1 instanceof BooleanValue && value2 instanceof BooleanValue)
            return;
        if (value1 instanceof ConstStringValue && value2 instanceof ConstStringValue)
            return;
        if (value1 instanceof ParameterRefValue) {
            ((ParameterRefValue)value1).getParam().match(value2);
            return;
        }
        if (value2 instanceof ParameterRefValue) {
            ((ParameterRefValue)value2).getParam().match(value1);
            return;
        }
        if (value1 instanceof ConditionalValue) {
            ensureEqualTypes(((ConditionalValue)value1).getThenValue(), value2, message);
            ensureEqualTypes(((ConditionalValue)value1).getElseValue(), value2, message);
        } else if (value2 instanceof ConditionalValue) {
            ensureEqualTypes(value1, ((ConditionalValue)value2).getThenValue(), message);
            ensureEqualTypes(value2, ((ConditionalValue)value2).getElseValue(), message);
        }
        throw new ParseException(message + " (the values \"" + value1 + "\" and \"" + value2 + "\" have different types.");
    }

    private void ensureBoolean(Value value, String message) throws ParseException {
        if (value instanceof BooleanValue)
            return;
        if (value instanceof IntegerValue)
            throw new ParseException(message + " (the value \"" + value + "\" has type integer.");
        if (value instanceof ConstStringValue)
            throw new ParseException(message + " (the value \"" + value + "\" has type string.");
        if (value instanceof ParameterRefValue) {
            ((ParameterRefValue)value).getParam().setType(Parameter.Type.BOOLEANVALUE);
            return;
        }
        if (value instanceof ConditionalValue) {
            ensureBoolean(((ConditionalValue)value).getThenValue(), message);
            ensureBoolean(((ConditionalValue)value).getElseValue(), message);
        }
        assert false;
        throw new ParseException(message);
    }

    private void ensureInteger(Value value, String message) throws ParseException {
        if (value instanceof IntegerValue)
            return;
        if (value instanceof BooleanValue)
            throw new ParseException(message + " (the value \"" + value + "\" has type boolean.");
        if (value instanceof ConstStringValue)
            throw new ParseException(message + " (the value \"" + value + "\" has type string.");
        if (value instanceof ParameterRefValue) {
            ((ParameterRefValue)value).getParam().setType(Parameter.Type.INTEGERVALUE);
            return;
        }
        if (value instanceof ConditionalValue) {
            ensureBoolean(((ConditionalValue)value).getThenValue(), message);
            ensureBoolean(((ConditionalValue)value).getElseValue(), message);
        }
        assert false;
        throw new ParseException(message);
    }
}
