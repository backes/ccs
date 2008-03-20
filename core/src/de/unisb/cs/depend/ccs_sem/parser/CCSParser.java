package de.unisb.cs.depend.ccs_sem.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.*;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ChoiceExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ConditionalExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ErrorExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ExpressionRepository;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.PrefixExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.StopExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.UnknownRecursiveExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.adapters.TopMostExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.InputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.OutputAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.SimpleAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.IntervalRange;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.SetRange;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.*;

/**
 * This Parser parses the following grammar:
 *
 * program            --> (constDecl | rangeDecl | recursiveDecl)*  expression
 * constDecl          --> "const" identifier "=" arithExpression ";"
 * rangeDecl          --> "range" identifier "=" range ";"
 * recursiveDecl      --> recursionVariable = expression ";"
 * recursionVariable  --> ucIdentifier ( "[" ( ( parameter "," )* parameter)? "]"  )?
 *
 * expression          --> restrictExpression
 * restrictExpression  --> parallelExpression
 *                          | restrictExpression "\" "{" ( ( action "," )* action )? "}"
 * parallelExpression  --> choiceExpression
 *                          | parallelExpression "|" choiceExpression
 * choiceExpression    --> prefixExpression
 *                          | choiceExpression "+" prefixExpression
 * prefixExpression    --> whenExpression
 *                          | action "." prefixExpression
 * whenExpression      --> baseExpression | "when" arithExpression prefixExpression
 * baseExpression      --> "0"
 *                          | "ERROR"
 *                          | "(" expression ")"
 *                          | recursionVariable
 *                          | action
 *
 * action              --> lcIdentifier ( "?" inputValue | "!" outputValue )?
 * identifier          --> character ( digit | character ) *
 * lcIdentifier        --> lcCharacter ( digit | character ) *
 * ucIdentifier        --> ucCharacter ( digit | character ) *
 * character           --> "a" | ... | "z" | "A" | ... | "Z" | "_"
 * ucCharacter         --> "A" | ... | "Z" | "_"
 * lcCharacter         --> "a" | ... | "z" | "_"
 * digit               --> "0" | ... | "9"
 * inputValue          --> inputParameter | "" | arithBase
 * outputValue         --> arithBase | ""
 * parameter           --> identifier ( ":" rangeDef)
 * inputParameter      --> identifier ( ":" range)
 *
 * range               --> rangeAdd
 * rangeAdd            --> rangeDef | rangeAdd ( "+" | "-" ) rangeDef
 * rangeDef            --> Identifier | arithExpression ".." arithExpression
 *                          | "{" ( ( arithExpression "," )* arithExpression)? "}"
 * rangeBase           --> Identifier
 * rangeElem           --> integer | Identifier
 *
 * integer             --> ( "+" | "-" )? digit+
 *
 * arithExpression     --> arithCond
 * arithCond           --> arithOr | arithOr "?" arithCond ":" arithCond
 * arithOr             --> arithAnd | arithOr "||" arithAnd
 * arithAnd            --> arithEq | arithAnd "&&" arithEq
 * arithEq             --> arithComp | arithEq ("==" | "!=" | "=") arithComp
 * arithComp           --> arithShift | arithShift ("<" | "<=" | ">" | ">=") arithShift
 * arithShift          --> arithAdd | arithShift (">>" | "<<") arithAdd
 * arithAdd            --> arithMult | arithAdd ("+" | "-") arithMult
 * arithMult           --> arithNot | arithMult ("*" | "/" | "%" | "mod") arithUnary
 * arithUnary          --> arithBase | "!" arithUnary | "+" arithUnary | "-" arithUnary
 * arithBase           --> integer | "true" | "false" | "(" arithExpression ")" | identifier
 *
 *
 * @author Clemens Hammacher
 */
public class CCSParser implements Parser {

    private final List<IParsingProblemListener> listeners
        = new ArrayList<IParsingProblemListener>();

    // "Stack" of the currently read parameters, it's increased and decreased by
    // the read... methods. When a string is read, we try to match it with one
    // of these parameters *from left to right*.
    // i.e. we can "overwrite" parameters by just adding them on the left to this list.
    private LinkedList<Parameter> parameters;

    // a Map of all constants that are defined in the current program
    private Map<String, ConstantValue> constants;

    // a Map of all ranges that are defined in the current program
    private Map<String, Range> ranges;

    /**
     * Parses a CCS program from an input reader.
     *
     * @param input the Reader that provides the input
     * @return the parsed CCS program, or <code>null</code> if there was an error
     *         (use {@link #addProblemListener(IParsingProblemListener)} to fetch the error)
     */
    public Program parse(Reader input) {
        try {
            return parse(getDefaultLexer().lex(input));
        } catch (LexException e) {
            reportProblem(new ParsingProblem(ParsingProblem.ERROR, "Error lexing: " + e.getMessage(), e.getPosition(), e.getPosition()));
            return null;
        }
    }

    protected CCSLexer getDefaultLexer() {
        return new CCSLexer();
    }

    /**
     * Parses a CCS program from an input string.
     *
     * @param input the input source code
     * @return the parsed CCS program, or <code>null</code> if there was an error
     *         (use {@link #addProblemListener(IParsingProblemListener)} to fetch the error)
     */
    public Program parse(String input) {
        try {
            return parse(getDefaultLexer().lex(input));
        } catch (LexException e) {
            reportProblem(new ParsingProblem(ParsingProblem.ERROR, "Error lexing: " + e.getMessage(), e.getPosition(), e.getPosition()));
            return null;
        }
    }

    /**
     * Parses a CCS program from a token list.
     *
     * @param tokens the token list to parse
     * @return the parsed CCS program, or <code>null</code> if there was an error
     *         (use {@link #addProblemListener(IParsingProblemListener)} to fetch the error)
     */
    public synchronized Program parse(List<Token> tokens) {
        final ArrayList<ProcessVariable> processVariables = new ArrayList<ProcessVariable>();
        parameters = new LinkedList<Parameter>();
        constants = new HashMap<String, ConstantValue>();
        ranges = new HashMap<String, Range>();

        final ExtendedListIterator<Token> it = new ExtendedListIterator<Token>(tokens);

        readDeclarations(it, processVariables);

        // then, read the ccs expression
        Expression mainExpr;
        try {
            mainExpr = readMainExpression(it);
        } catch (ParseException e) {
            reportProblem(new ParsingProblem(ParsingProblem.ERROR, e.getMessage(), e.getStartPosition(), e.getEndPosition()));
            return null;
        }

        // now make it a "top most expression"
        mainExpr = ExpressionRepository.getExpression(new TopMostExpression(mainExpr));

        final Token eof = it.next();
        if (!(eof instanceof EOFToken)) {
            reportProblem(new ParsingProblem(ParsingProblem.ERROR, "Unexpected token", eof));
        }

        try {
            return new Program(processVariables, mainExpr);
        } catch (ParseException e) {
            reportProblem(new ParsingProblem(ParsingProblem.ERROR, e.getMessage(), e.getStartPosition(), e.getEndPosition()));
        }

        return null;
    }

    private void readDeclarations(final ExtendedListIterator<Token> tokens,
            final ArrayList<ProcessVariable> processVariables) {

        while (true) {
            try {
                if (tokens.peek() instanceof ConstToken) {
                    tokens.next();
                    final Token nextToken = tokens.next();
                    if (!(nextToken instanceof Identifier))
                        throw new ParseException("Expected an identifier after 'const' keyword.", nextToken);

                    final String constName = ((Identifier)nextToken).getName();

                    // check for double constant name
                    if (constants.get(constName) != null)
                        throw new ParseException("Constant name \"" + constName + "\" already used.", nextToken);

                    if (!tokens.hasNext() || !(tokens.next() instanceof Assign))
                        throw new ParseException("Expected '=' after const identifier.", tokens.peekPrevious());

                    final int constStartPosition = tokens.peek().getStartPosition();
                    final Value constValue = readArithmeticExpression(tokens);

                    if (!(tokens.next() instanceof Semicolon))
                        throw new ParseException("Expected ';' after constant declaration.", tokens.peekPrevious());

                    if (!(constValue instanceof ConstantValue))
                        throw new ParseException("Expected constant value.", constStartPosition, tokens.peekPrevious().getEndPosition());

                    constants.put(constName, (ConstantValue)constValue);
                } else if (tokens.peek() instanceof RangeToken) {
                    tokens.next();
                    final Token nextToken = tokens.next();
                    if (!(nextToken instanceof Identifier))
                        throw new ParseException("Expected an identifier after 'range' keyword.", nextToken);

                    final String rangeName = ((Identifier)nextToken).getName();

                    // check for double range name
                    if (ranges.get(rangeName) != null)
                        throw new ParseException("Range name \"" + rangeName + "\" already used.", nextToken);

                    if (!tokens.hasNext() || !(tokens.next() instanceof Assign))
                        throw new ParseException("Expected '=' after range identifier.", tokens.peekPrevious());

                    final Range range = readRange(tokens);

                    if (!(tokens.next() instanceof Semicolon))
                        throw new ParseException("Expected ';' after constant declaration.", tokens.peekPrevious());

                    ranges.put(rangeName, range);
                } else {
                    final int oldPosition = tokens.nextIndex();
                    final Token nextToken = tokens.peek();
                    final ProcessVariable nextProcessVariable = readProcessDeclaration(tokens);
                    if (nextProcessVariable == null) {
                        tokens.setPosition(oldPosition);
                        break;
                    }

                    // check if a process variable with the same name and number of parameters is already known
                    for (final ProcessVariable proc: processVariables)
                        if (proc.getName().equals(nextProcessVariable.getName())
                                && proc.getParamCount() == nextProcessVariable.getParamCount())
                            reportProblem(new ParsingProblem(ParsingProblem.ERROR,
                                    "Duplicate process variable definition (" + nextProcessVariable.getName()
                                    + "[" + nextProcessVariable.getParamCount() + "]", nextToken));

                    processVariables.add(nextProcessVariable);
                }
            } catch (ParseException e) {
                // TODO
            }
        }
        processVariables.trimToSize();
    }

    /**
     * @return <code>null</code>, if there are no more declarations
     */
    protected ProcessVariable readProcessDeclaration(ExtendedListIterator<Token> tokens) throws ParseException {
        final Token token1 = tokens.hasNext() ? tokens.next() : null;
        final Token token2 = tokens.hasNext() ? tokens.next() : null;
        if (token1 == null || token2 == null)
            // there is no declaration
            return null;

        if (!(token1 instanceof Identifier))
            return null;

        final Identifier identifier = (Identifier) token1;
        if (identifier.isQuoted() || !Character.isUpperCase(identifier.getName().charAt(0)))
            return null;
        List<Parameter> myParameters;
        Expression expr;

        if (token2 instanceof Assign) {
            expr = readExpression(tokens);
            myParameters = Collections.emptyList();
        } else if (token2 instanceof LBracket) {
            myParameters = readParameters(tokens);
            if (myParameters == null || !tokens.hasNext()
                    || !(tokens.next() instanceof Assign))
                return null;
            // save old parameters
            final LinkedList<Parameter> oldParameters = parameters;
            try {
                // set new parameters
                parameters = new LinkedList<Parameter>(myParameters);
                expr = readExpression(tokens);
            } finally {
                // restore old parameters
                parameters = oldParameters;
            }
        } else
            return null;

        if (!(tokens.next() instanceof Semicolon))
            throw new ParseException("Expected ';' after this declaration", tokens.peekPrevious());

        final ProcessVariable proc = new ProcessVariable(identifier.getName(), myParameters, expr);
        // hook for logging:
        identifierParsed(identifier, proc);
        return proc;
    }

    /**
     * Read a Range definition.
     *
     * @param tokens
     * @return a {@link Range} read from the tokens
     * @throws ParseException
     */
    private Range readRange(ExtendedListIterator<Token> tokens) throws ParseException {
        return readRangeAdd(tokens);
    }

    private Range readRangeAdd(ExtendedListIterator<Token> tokens) throws ParseException {
        Range range = readRangeDef(tokens);
        while (tokens.peek() instanceof Plus || tokens.peek() instanceof Minus) {
            final boolean isSub = tokens.next() instanceof Minus;
            final Range secondRange = readRangeDef(tokens);
            range = isSub ? range.subtract(secondRange) : range.add(secondRange);
        }

        return range;
    }

    private Range readRangeDef(ExtendedListIterator<Token> tokens) throws ParseException {
        Token nextToken = tokens.peek();
        // just a range definition in parenthesis?
        if (nextToken instanceof LParenthesis) {
            tokens.next();
            final Range range = readRange(tokens);
            if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                throw new ParseException("Expected ')'.", tokens.peekPrevious());
            return range;
        }

        // or a set of independant values
        if (nextToken instanceof LBrace) {
            tokens.next();
            final Set<Value> rangeValues = readRangeValues(tokens);
            return new SetRange(rangeValues);
        }

        // or a range of integer values
        int posStart = tokens.peek().getStartPosition();
        final Value startValue = readArithmeticExpression(tokens);
        // are there '..'?
        if (tokens.peek() instanceof IntervalDots) {
            ensureInteger(startValue, "Expected constant integer expression before '..'.", posStart, tokens.peekPrevious().getEndPosition());

            tokens.next();

            posStart = tokens.peek().getStartPosition();
            final Value endValue = readArithmeticExpression(tokens);
            ensureInteger(endValue, "Expected constant integer expression after '..'.", posStart, tokens.peekPrevious().getEndPosition());

            return new IntervalRange(startValue, endValue);
        }

        // or another range (if the value was a string value)
        if (startValue instanceof ConstString) {
            final Range referencedRange = ranges.get(((ConstString)startValue).getValue());
            if (referencedRange != null) {
                // hook for logging:
                changedIdentifierMeaning((ConstString)startValue, referencedRange);
                return referencedRange;
            }
        }

        // otherwise, there is an error
        throw new ParseException("No valid range definition.", nextToken);
    }

    private Set<Value> readRangeValues(ExtendedListIterator<Token> it) throws ParseException {
        if (it.peek() instanceof RBrace) {
            it.next();
            return Collections.emptySet();
        }

        final Set<Value> values = new TreeSet<Value>();

        while (true) {
            final Value value = readArithmeticExpression(it);

            values.add(value);

            final Token nextToken = it.next();

            if (nextToken instanceof RBrace)
                return values;

            if (!(nextToken instanceof Comma))
                throw new ParseException("Expected ',' or '}'", nextToken);
        }
    }

    /**
     * Read all parameters up to the next RBracket (this token is read too).
     * @return <code>null</code> if there was no declaration
     * @throws ParseException if there was definitly a declaration, but it had
     *                        syntactical errors
     */
    private List<Parameter> readParameters(ExtendedListIterator<Token> tokens) throws ParseException {
        final ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        final Set<String> readParameters = new HashSet<String>();

        if (tokens.peek() instanceof RBracket) {
            tokens.next();
            return Collections.emptyList();
        }

        while (true) {
            // read one parameter
            Token nextToken = tokens.next();
            if (nextToken instanceof Identifier) {
                final Identifier identifier = (Identifier)nextToken;
                if (identifier.isQuoted())
                    return null;
                final String name = identifier.getName();
                if ("i".equals(name))
                    return null;
                if (!readParameters.add(name))
                    // duplicate parameter name
                    return null;

                // range?
                Range range = null;
                if (tokens.hasNext() && tokens.peek() instanceof Colon) {
                    tokens.next();
                    range = readRange(tokens);
                }
                final Parameter nextParameter = new Parameter(name, range);
                // hook for logging:
                identifierParsed(identifier, nextParameter);
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
    }

    /**
     * Read all parameter values up to the next RBracket (this token is read too).
     */
    private List<Value> readParameterValues(ExtendedListIterator<Token> tokens) throws ParseException {

        if (tokens.hasNext() && tokens.peek() instanceof RBracket) {
            tokens.next();
            return Collections.emptyList();
        }

        final ArrayList<Value> readParameters = new ArrayList<Value>();

        while (true) {
            if (tokens.peek() instanceof RBracket) {
                tokens.next();
                readParameters.trimToSize();
                return readParameters;
            }

            final Value nextValue = readArithmeticExpression(tokens);

            readParameters.add(nextValue);

            final Token nextToken = tokens.next();

            if (nextToken instanceof RBracket) {
                readParameters.trimToSize();
                return readParameters;
            }
            if (!(nextToken instanceof Comma))
                throw new ParseException("Expected ',' or ']'", nextToken);
        }
    }

    /**
     * Read one Expression.
     */
    private Expression readExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        // the topmost operator is restriction:
        return readRestrictExpression(tokens);
    }

    /**
     * Read the "main expression".
     */
    protected Expression readMainExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        return readExpression(tokens);
    }

    /**
     * Read one restriction expression.
     */
    private Expression readRestrictExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        Expression expr = readParallelExpression(tokens);
        while (tokens.peek() instanceof Restrict) {
            tokens.next();
            if (!(tokens.next() instanceof LBrace))
                throw new ParseException("Expected '{'", tokens.peekPrevious());
            final Set<Action> restricted = readRestrictionActionSet(tokens);
            expr = ExpressionRepository.getExpression(new RestrictExpression(expr, restricted));
        }

        return expr;
    }

    /**
     * Read all actions up to the next RBrace (this token is read too).
     */
    private Set<Action> readRestrictionActionSet(ExtendedListIterator<Token> tokens) throws ParseException {
        final Set<Action> actions = new TreeSet<Action>();

        if (tokens.hasNext() && tokens.peek() instanceof RBrace) {
            tokens.next();
            return Collections.emptySet();
        }

        while (true) {

            final Action newAction = readAction(tokens, false);
            if (newAction == null)
                throw new ParseException("Expected an action here", tokens.next());

            actions.add(newAction);

            final Token nextToken = tokens.next();

            if (nextToken instanceof RBrace)
                return actions;

            if (!(nextToken instanceof Comma))
                throw new ParseException("Expected ',' or '}'", nextToken);
        }
    }

    /**
     * Read an Action.
     *
     * @param tokens
     * @param tauAllowed
     * @return the read Action, or <code>null</code> if there is no action in the tokens.
     *         In this case, the iterator is not changed.
     * @throws ParseException
     */
    private Action readAction(ExtendedListIterator<Token> tokens, boolean tauAllowed) throws ParseException {
        final Channel channel = readChannel(tokens);
        if (channel == null)
            return null;
        if (channel instanceof TauChannel) {
            if (!tauAllowed)
                throw new ParseException("Tau action not allowed here", tokens.peekPrevious());
            return TauAction.get();
        }

        if (tokens.peek() instanceof QuestionMark) {
            tokens.next();
            // read the input value

            // either a parameter
            if (tokens.peek() instanceof Identifier) {
                final Identifier identifier = (Identifier)tokens.next();
                if (identifier.isQuoted())
                    tokens.previous();
                else {
                    Range range = null;
                    if (tokens.peek() instanceof Colon) {
                        tokens.next();
                        range = readRangeDef(tokens);
                    }
                    final Parameter parameter = new Parameter(identifier.getName(), range);
                    // hook for logging:
                    identifierParsed(identifier, parameter);
                    return new InputAction(channel, parameter);
                }
            }

            // ELSE:
            // an arithmetic expression (if it is more complex,
            // it must have parenthesis around it)

            int posStart = tokens.peek().getStartPosition();
            final Value value = readArithmeticBaseExpression(tokens); // may return null
            if (value instanceof ParameterReference)
                try {
                        ((ParameterReference)value).getParam().setType(Parameter.Type.VALUE);
                } catch (ParseException e) {
                    throw new ParseException(e.getMessage(), posStart, tokens.peekPrevious().getEndPosition());
                }
            return new InputAction(channel, value);
        } else if (tokens.peek() instanceof Exclamation) {
            tokens.next();
            // we have an output value
            final Value value = readOutputValue(tokens);
            return new OutputAction(channel, value);
        }

        // no tau, no input, no output ==> it's a simple action
        return new SimpleAction(channel);
    }

    // returns null if there is no output value
    private Value readOutputValue(ExtendedListIterator<Token> tokens) throws ParseException {
        int posStart = tokens.peek().getStartPosition();
        final Value value = readArithmeticBaseExpression(tokens); // may return null
        if (value instanceof ParameterReference)
            try {
                ((ParameterReference)value).getParam().setType(Parameter.Type.VALUE);
            } catch (ParseException e) {
                throw new ParseException(e.getMessage(), posStart, tokens.peekPrevious().getEndPosition());
            }
        return value;
    }

    private Channel readChannel(ExtendedListIterator<Token> tokens) throws ParseException {
        if (!(tokens.peek() instanceof Identifier))
            return null;

        final Identifier identifier = (Identifier)tokens.next();
        Channel channel = null;
        if ("i".equals(identifier.getName()))
            channel = TauChannel.get();
        else if (!identifier.isQuoted()) {
            for (final Parameter param: parameters) {
                if (param.getName().equals(identifier.getName())) {
                    try {
                        param.setType(Parameter.Type.CHANNEL);
                    } catch (ParseException e) {
                        throw new ParseException(e.getMessage(), identifier);
                    }
                    channel = new ParameterRefChannel(param);
                    break;
                }
            }
        }
        if (channel == null && Character.isLowerCase(identifier.getName().charAt(0)))
            channel = new ConstStringChannel(identifier.getName());
        if (channel == null) {
            tokens.previous();
            return null;
        }

        // hook for logging:
        identifierParsed(identifier, channel);
        return channel;
    }

    /**
     * Read one parallel expression.
     */
    private Expression readParallelExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        Expression expr = readChoiceExpression(tokens);
        while (tokens.peek() instanceof Parallel) {
            tokens.next();
            final Expression newExpr = readChoiceExpression(tokens);
            expr = ParallelExpression.create(expr, newExpr);
        }

        return expr;
    }

    /**
     * Read one choice expression.
     */
    private Expression readChoiceExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        Expression expr = readPrefixExpression(tokens);
        while (tokens.peek() instanceof Plus) {
            tokens.next();
            final Expression newExpr = readPrefixExpression(tokens);
            expr = ChoiceExpression.create(expr, newExpr);
        }

        return expr;
    }

    /**
     * Read one prefix expression.
     */
    private Expression readPrefixExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        final Action action = readAction(tokens, true);
        if (action == null)
            return readWhenExpression(tokens);

        if (tokens.peek() instanceof Dot) {
            tokens.next();
            // if the read action is an InputAction with a parameter, we
            // have to add this parameter to the list of parameters
            Parameter newParam = null;
            if (action instanceof InputAction) {
                newParam = ((InputAction)action).getParameter();
                if (newParam != null) {
                    // add the new parameter in front of the list
                    parameters.addFirst(newParam);
                }
            }
            final Expression target = readPrefixExpression(tokens);
            if (newParam != null) {
                final Parameter removedParam = parameters.removeFirst();
                assert removedParam == newParam;
            }
            return ExpressionRepository.getExpression(new PrefixExpression(action, target));
        }
        // otherwise, we append ".0" (i.e. we make a PrefixExpression with target = STOP
        return ExpressionRepository.getExpression(new PrefixExpression(action, StopExpression.get()));
    }

    private Expression readWhenExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        if (tokens.peek() instanceof When) {
            tokens.next();
            int startPos = tokens.peek().getStartPosition();
            final Value condition = readArithmeticExpression(tokens);
            ensureBoolean(condition, "Expected boolean expression after 'when'.", startPos, tokens.peekPrevious().getEndPosition());

            // if there is a "then" now, ignore it
            if (tokens.hasNext() && tokens.peek() instanceof Then)
                tokens.next();
            final Expression consequence = readPrefixExpression(tokens);

            Expression condExpr = ConditionalExpression.create(condition, consequence);

            // we allow an "else" here to declare an alternative, but internally,
            // it is mapped to a "(when (x) <consequence>) + (when (!x) <alternative>)"
            if (tokens.hasNext() && tokens.peek() instanceof Else) {
                tokens.next();
                Expression alternative = readPrefixExpression(tokens);
                // build negated condition
                final Value negatedCondition = condition instanceof NotValue
                    ? ((NotValue)condition).getNegatedValue()
                    : NotValue.create(condition);
                alternative = ConditionalExpression.create(negatedCondition, alternative);
                condExpr = ChoiceExpression.create(condExpr, alternative);
            }
            return condExpr;
        }
        return readBaseExpression(tokens);
    }

    /**
     * Read one base expression (stop, error, expression in parentheses, or recursion variable).
     */
    private Expression readBaseExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        final Token nextToken = tokens.next();

        if (nextToken instanceof Stop)
            return ExpressionRepository.getExpression(StopExpression.get());

        if (nextToken instanceof ErrorToken)
            return ExpressionRepository.getExpression(ErrorExpression.get());

        if (nextToken instanceof LParenthesis) {
            final Expression expr = readExpression(tokens);
            if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                throw new ParseException("Expected ')'", tokens.peekPrevious());
            return expr;
        }

        if (nextToken instanceof Identifier) {
            final Identifier id = (Identifier) nextToken;
            if (Character.isUpperCase(id.getName().charAt(0))) {
                List<Value> myParameters = Collections.emptyList();
                if (tokens.hasNext() && tokens.peek() instanceof LBracket) {
                    tokens.next();
                    myParameters = readParameterValues(tokens);
                }
                final Expression expression = ExpressionRepository.getExpression(new UnknownRecursiveExpression(id.getName(), myParameters, id.getStartPosition(), tokens.peekPrevious().getEndPosition()));
                // hook for logging:
                identifierParsed(id, expression);
                return expression;
            }
        }

        throw new ParseException(nextToken instanceof EOFToken ? "Unexcepted end of file" : "Syntax error (unexpected token)", nextToken);
    }

    private Value readArithmeticExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        return readArithmeticConditionalExpression(tokens);
    }

    private Value readArithmeticConditionalExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        int posBefore = tokens.peek().getStartPosition();
        final Value orValue = readArithmeticOrExpression(tokens);
        if (tokens.peek() instanceof QuestionMark) {
            tokens.next();
            ensureBoolean(orValue, "Boolean expression required before '?:' construct.", posBefore, tokens.peekPrevious().getEndPosition());
            final Value thenValue = readArithmeticConditionalExpression(tokens);
            if (!(tokens.next() instanceof Colon))
                throw new ParseException("Expected ':'", tokens.previous());
            final Value elseValue = readArithmeticConditionalExpression(tokens);
            ensureEqualTypes(thenValue, elseValue, "Expression in '?:' construct must have the same type.", posBefore, tokens.peekPrevious().getEndPosition());
            return ConditionalValue.create(orValue, thenValue, elseValue);
        }

        return orValue;
    }

    private Value readArithmeticOrExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        int posBefore = tokens.peek().getStartPosition();
        Value value = readArithmeticAndExpression(tokens);
        while (tokens.peek() instanceof Or) {
            tokens.next();
            ensureBoolean(value, "Boolean expression required before '||'.", posBefore, tokens.peekPrevious().getEndPosition());
            posBefore = tokens.peek().getStartPosition();
            final Value secondValue = readArithmeticAndExpression(tokens);
            ensureBoolean(secondValue, "Boolean expression required after '||'.", posBefore, tokens.peekPrevious().getEndPosition());
            value = OrValue.create(value, secondValue);
        }

        return value;
    }

    private Value readArithmeticAndExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        int posBefore = tokens.peek().getStartPosition();
        Value value = readArithmeticEqExpression(tokens);
        while (tokens.hasNext() && tokens.peek() instanceof And) {
            tokens.next();
            ensureBoolean(value, "Boolean expression required before '&&'.", posBefore, tokens.peekPrevious().getEndPosition());
            posBefore = tokens.peek().getStartPosition();
            final Value secondValue = readArithmeticEqExpression(tokens);
            ensureBoolean(secondValue, "Boolean expression required after '&&'.", posBefore, tokens.peekPrevious().getEndPosition());
            value = AndValue.create(value, secondValue);
        }

        return value;
    }

    private Value readArithmeticEqExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        int posBefore = tokens.peek().getStartPosition();
        Value value = readArithmeticCompExpression(tokens);
        while (tokens.peek() instanceof Equals
                || tokens.peek() instanceof Neq) {
            final boolean isNeq = tokens.next() instanceof Neq;
            final Value secondValue = readArithmeticCompExpression(tokens);
            int posAfter = tokens.peekPrevious().getEndPosition();
            ensureEqualTypes(value, secondValue, "Values to compare must have the same type.", posBefore, posAfter);
            value = EqValue.create(value, secondValue, isNeq);
        }

        return value;
    }

    private Value readArithmeticCompExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        int posStart = tokens.peek().getStartPosition();
        final Value value = readArithmeticShiftExpression(tokens);
        int posEnd = tokens.peekPrevious().getEndPosition();

        final Token nextToken = tokens.peek();
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
            tokens.next();
            ensureInteger(value, "Only integer values can be compared.", posStart, posEnd);
            posStart = tokens.peek().getStartPosition();
            final Value secondValue = readArithmeticShiftExpression(tokens);
            posEnd = tokens.peekPrevious().getEndPosition();
            ensureInteger(secondValue, "Only integer values can be compared.", posStart, posEnd);
            return CompValue.create(value, secondValue, type);
        }

        return value;
    }

    private Value readArithmeticShiftExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        int posStart = tokens.peek().getStartPosition();
        Value value = readArithmeticAddExpression(tokens);
        while (tokens.peek() instanceof LeftShift
                || tokens.peek() instanceof RightShift) {
            ensureInteger(value, "Only integer values can be shifted.", posStart, tokens.peekPrevious().getEndPosition());
            final boolean shiftRight = tokens.next() instanceof RightShift;
            posStart = tokens.peek().getStartPosition();
            final Value secondValue = readArithmeticAddExpression(tokens);
            ensureInteger(secondValue, "Shifting width must be an integer.", posStart, tokens.peekPrevious().getEndPosition());
            value = ShiftValue.create(value, secondValue, shiftRight);
        }

        return value;
    }

    private Value readArithmeticAddExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        int posStart = tokens.peek().getStartPosition();
        Value value = readArithmeticMultExpression(tokens);
        while (tokens.peek() instanceof Plus
                || tokens.peek() instanceof Minus) {
            ensureInteger(value, "Both sides of an addition must be integers.", posStart, tokens.peekPrevious().getEndPosition());
            final boolean isSubtraction = tokens.next() instanceof Minus;
            posStart = tokens.peek().getStartPosition();
            final Value secondValue = readArithmeticMultExpression(tokens);
            ensureInteger(secondValue, "Both sides of an addition must be integers.", posStart, tokens.peekPrevious().getEndPosition());
            value = AddValue.create(value, secondValue, isSubtraction);
        }

        return value;
    }

    private Value readArithmeticMultExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        int posStart = tokens.peek().getStartPosition();
        Value value = readArithmeticUnaryExpression(tokens);
        while (true) {
            final Token nextToken = tokens.peek();
            MultValue.Type type = null;
            if (nextToken instanceof Multiplication)
                type = MultValue.Type.MULT;
            else if (nextToken instanceof Division)
                type = MultValue.Type.DIV;
            else if (nextToken instanceof Modulo)
                type = MultValue.Type.MOD;

            if (type == null)
                break;

            ensureInteger(value, "Both sides of a multiplication/division must be integer expressions.", posStart, tokens.peekPrevious().getEndPosition());
            tokens.next();
            posStart = tokens.peek().getStartPosition();
            final Value secondValue = readArithmeticUnaryExpression(tokens);
            ensureInteger(secondValue, "Both sides of a multiplication/division must be integer expressions.", posStart, tokens.peekPrevious().getEndPosition());
            value = MultValue.create(value, secondValue, type);
        }

        return value;
    }

    private Value readArithmeticUnaryExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        final Token nextToken = tokens.peek();
        if (nextToken instanceof Exclamation) {
            tokens.next();
            int posStart = tokens.peek().getStartPosition();
            final Value negatedValue = readArithmeticUnaryExpression(tokens);
            ensureBoolean(negatedValue, "The negated value must be a boolean expression.", posStart, tokens.peekPrevious().getEndPosition());
            return NotValue.create(negatedValue);
        } else if (nextToken instanceof Plus) {
            tokens.next();
            return readArithmeticUnaryExpression(tokens);
        } else if (nextToken instanceof Minus) {
            tokens.next();
            int posStart = tokens.peek().getStartPosition();
            final Value negativeValue = readArithmeticUnaryExpression(tokens);
            ensureInteger(negativeValue, "The negated value must be an integer expression.", posStart, tokens.peekPrevious().getEndPosition());
            return NegativeValue.create(negativeValue);
        }

        // else:
        return readArithmeticBaseExpression(tokens);
    }

    private Value readArithmeticBaseExpression(ExtendedListIterator<Token> tokens) throws ParseException {
        final Token nextToken = tokens.next();
        if (nextToken instanceof IntegerToken)
            return new ConstIntegerValue(((IntegerToken)nextToken).getValue());
        // a stop is the integer "0" here...
        if (nextToken instanceof Stop) {
            // change the token in the token list (for highlighting etc.)
            tokens.set(tokens.previousIndex(),
                    new IntegerToken(nextToken.getStartPosition(),
                            nextToken.getEndPosition(), 0));
            return new ConstIntegerValue(0);
        }
        if (nextToken instanceof True)
            return ConstBooleanValue.get(true);
        if (nextToken instanceof False)
            return ConstBooleanValue.get(false);
        if (nextToken instanceof Identifier) {
            final Identifier id = (Identifier)nextToken;
            final String name = id.getName();
            if (!id.isQuoted()) {
                // search if this identifier is a parameter
                for (final Parameter param: parameters)
                    if (param.getName().equals(name)) {
                        final ParameterReference parameterReference = new ParameterReference(param);
                        // hook for logging:
                        identifierParsed(id, parameterReference);
                        return parameterReference;
                    }
                // search if it is a constant
                final ConstantValue constant = constants.get(name);
                if (constant != null) {
                    // hook for logging:
                    identifierParsed(id, constant);
                    return constant;
                }
            }
            final ConstString constString = new ConstString(name);
            // hook for logging:
            identifierParsed(id, constString);
            return constString;
        }
        if (nextToken instanceof LParenthesis) {
            final Value value = readArithmeticExpression(tokens);
            if (!(tokens.next() instanceof RParenthesis))
                throw new ParseException("Expected ')'.", tokens.peekPrevious());
            return value;
        }
        tokens.previous();
        return null;
    }

    private void ensureEqualTypes(Value value1, Value value2, String message, int startPos, int endPos) throws ParseException {
        if (value1 instanceof IntegerValue && value2 instanceof IntegerValue)
            return;
        if (value1 instanceof BooleanValue && value2 instanceof BooleanValue)
            return;
        if (value1 instanceof ConstString && value2 instanceof ConstString)
            return;
        try {
            if (value1 instanceof ParameterReference || value1 instanceof ParameterRefChannel) {
                ((ParameterReference)value1).getParam().match(value2);
                return;
            }
            if (value2 instanceof ParameterReference || value2 instanceof ParameterRefChannel) {
                ((ParameterReference)value2).getParam().match(value1);
                return;
            }
        } catch (ParseException e) {
            throw new ParseException(e.getMessage(), startPos, endPos);
        }
        if (value1 instanceof ConditionalValue) {
            ensureEqualTypes(((ConditionalValue)value1).getThenValue(), value2, message, startPos, endPos);
            ensureEqualTypes(((ConditionalValue)value1).getElseValue(), value2, message, startPos, endPos);
        } else if (value2 instanceof ConditionalValue) {
            ensureEqualTypes(value1, ((ConditionalValue)value2).getThenValue(), message, startPos, endPos);
            ensureEqualTypes(value1, ((ConditionalValue)value2).getElseValue(), message, startPos, endPos);
        }
        throw new ParseException(message + " The values \"" + value1 + "\" and \"" + value2 + "\" have different types.", startPos, endPos);
    }

    private void ensureBoolean(Value value, String message, int startPos, int endPos) throws ParseException {
        if (value instanceof BooleanValue)
            return;
        if (value instanceof IntegerValue)
            throw new ParseException(message + " The value \"" + value + "\" has type integer.", startPos, endPos);
        if (value instanceof ConstString)
            throw new ParseException(message + " The value \"" + value + "\" has type string.", startPos, endPos);
        if (value instanceof ParameterReference) {
            try {
                ((ParameterReference)value).getParam().setType(Parameter.Type.BOOLEANVALUE);
            } catch (ParseException e) {
                throw new ParseException(message + e.getMessage(), startPos, endPos);
            }
            return;
        }
        if (value instanceof ConditionalValue) {
            ensureBoolean(((ConditionalValue)value).getThenValue(), message, startPos, endPos);
            ensureBoolean(((ConditionalValue)value).getElseValue(), message, startPos, endPos);
        }
        assert false;
        throw new ParseException(message, startPos, endPos);
    }

    private void ensureInteger(Value value, String message, int startPos, int endPos) throws ParseException {
        if (value instanceof IntegerValue)
            return;
        if (value instanceof BooleanValue)
            throw new ParseException(message + " The value \"" + value + "\" has type boolean.", startPos, endPos);
        if (value instanceof ConstString)
            throw new ParseException(message + " The value \"" + value + "\" has type string.", startPos, endPos);
        if (value instanceof ParameterReference) {
            try {
                ((ParameterReference)value).getParam().setType(Parameter.Type.INTEGERVALUE);
            } catch (final ParseException e) {
                throw new ParseException(message + e.getMessage(), startPos, endPos);
            }
            return;
        }
        if (value instanceof ConditionalValue) {
            ensureInteger(((ConditionalValue)value).getThenValue(), message, startPos, endPos);
            ensureInteger(((ConditionalValue)value).getElseValue(), message, startPos, endPos);
        }
        assert false;
        throw new ParseException(message, startPos, endPos);
    }

    protected void identifierParsed(Identifier identifier, Object semantic) {
        // ignore in this implementation
    }

    protected void changedIdentifierMeaning(ConstString constString,
            Range range) {
        // ignore in this implementation
    }

    public void addProblemListener(IParsingProblemListener listener) {
        listeners.add(listener);
    }

    public void removeProblemListener(IParsingProblemListener listener) {
        listeners.remove(listener);
    }

    public void reportProblem(ParsingProblem problem) {
        for (IParsingProblemListener listener: listeners)
            listener.reportParsingProblem(problem);
    }

}
