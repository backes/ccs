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
import de.unisb.cs.depend.ccs_sem.semantics.expressions.*;
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

    // "Stack" of the currently read parameters, it's increased and decreased by
    // the read... methods. When a string is read, we try to match it with one
    // of these parameters *from left to right*.
    // i.e. we can "overwrite" parameters by just adding them on the left to this list.
    private LinkedList<Parameter> parameters;

    // a Map of all constants that are defined in the current program
    private Map<String, ConstantValue> constants;

    // a Map of all ranges that are defined in the current program
    private Map<String, Range> ranges;

    public Program parse(Reader input) throws ParseException, LexException {
        return parse(new CCSLexer().lex(input));
    }

    public Program parse(String input) throws ParseException, LexException {
        return parse(new CCSLexer().lex(input));
    }

    // synchronized to make sure that this method is only called once at a time
    public synchronized Program parse(List<Token> tokens) throws ParseException {
        final ArrayList<ProcessVariable> processVariables = new ArrayList<ProcessVariable>();
        parameters = new LinkedList<Parameter>();
        constants = new HashMap<String, ConstantValue>();
        ranges = new HashMap<String, Range>();

        final ExtendedIterator<Token> it = new ExtendedIterator<Token>(tokens);

        try {
            readDeclarations(it, processVariables);

            // then, read the ccs expression
            Expression expr = readMainExpression(it);

            // now make it a "top most expression"
            expr = ExpressionRepository.getExpression(new TopMostExpression(expr));

            final Token eof = it.next();
            if (!(eof instanceof EOFToken)) {
                throw new ParseException("Syntax error: Unexpected '" + eof + "'");
            }

            final Program program = new Program(processVariables, expr);

            return program;
        } catch (final ParseException e) {
            e.setEnvironment(getEnvironment(tokens, it.previousIndex(), 5));
            throw e;
        }
    }

    private void readDeclarations(final ExtendedIterator<Token> it,
            final ArrayList<ProcessVariable> processVariables) throws ParseException {

         while (it.hasNext()) {
            if (it.peek() instanceof ConstToken) {
                it.next();
                final Token nextToken = it.next();
                if (!(nextToken instanceof Identifier))
                    throw new ParseException("Expected an identifier after 'const' keyword.");

                final String constName = ((Identifier)nextToken).getName();

                // check for double constant name
                if (constants.get(constName) != null)
                    throw new ParseException("Constant name \"" + constName + "\" already used.");

                if (!it.hasNext() || !(it.next() instanceof Assign))
                    throw new ParseException("Expected '=' after const identifier.");

                final Value constValue = readArithmeticExpression(it);

                if (!(it.next() instanceof Semicolon))
                    throw new ParseException("Expected ';' after constant declaration.");

                if (!(constValue instanceof ConstantValue))
                    throw new ParseException("Expected constant value.");

                constants.put(constName, (ConstantValue)constValue);
            } else if (it.peek() instanceof RangeToken) {
                it.next();
                final Token nextToken = it.next();
                if (!(nextToken instanceof Identifier))
                    throw new ParseException("Expected an identifier after 'range' keyword.");

                final String rangeName = ((Identifier)nextToken).getName();

                // check for double range name
                if (ranges.get(rangeName) != null)
                    throw new ParseException("Range name \"" + rangeName + "\" already used.");

                if (!it.hasNext() || !(it.next() instanceof Assign))
                    throw new ParseException("Expected '=' after range identifier.");

                final Range range = readRange(it);

                if (!(it.next() instanceof Semicolon))
                    throw new ParseException("Expected ';' after constant declaration.");

                ranges.put(rangeName, range);
            } else {
                final int oldPosition = it.nextIndex();
                final ProcessVariable nextProcessVariable = readProcessDeclaration(it);
                if (nextProcessVariable == null) {
                    it.setPosition(oldPosition);
                    break;
                }

                // check if a process variable with the same name and number of parameters is already known
                for (final ProcessVariable proc: processVariables)
                    if (proc.getName().equals(nextProcessVariable.getName())
                            && proc.getParamCount() == nextProcessVariable.getParamCount())
                        throw new ParseException("Duplicate process variable definition ("
                            + nextProcessVariable.getName() + "[" + nextProcessVariable.getParamCount() + "]");

                processVariables.add(nextProcessVariable);
            }
        }
        processVariables.trimToSize();
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
     * @return <code>null</code>, if there are no more declarations
     */
    protected ProcessVariable readProcessDeclaration(ExtendedIterator<Token> tokens) throws ParseException {
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
            throw new ParseException("Expected ';' after this declaration");

        final ProcessVariable proc = new ProcessVariable(identifier.getName(), myParameters, expr);
        // hook for logging:
        identifierParsed(identifier, proc);
        return proc;
    }

    private Range readRange(ExtendedIterator<Token> tokens) throws ParseException {
        return readRangeAdd(tokens);
    }

    private Range readRangeAdd(ExtendedIterator<Token> tokens) throws ParseException {
        Range range = readRangeDef(tokens);
        while (tokens.peek() instanceof Plus || tokens.peek() instanceof Minus) {
            final boolean isSub = tokens.next() instanceof Minus;
            final Range secondRange = readRangeDef(tokens);
            range = isSub ? range.subtract(secondRange) : range.add(secondRange);
        }

        return range;
    }

    private Range readRangeDef(ExtendedIterator<Token> it) throws ParseException {
        // just a range definition in parenthesis?
        if (it.peek() instanceof LParenthesis) {
            it.next();
            final Range range = readRange(it);
            if (!it.hasNext() || !(it.next() instanceof RParenthesis))
                throw new ParseException("Expected ')'.");
            return range;
        }

        // or a set of independant values
        if (it.peek() instanceof LBrace) {
            it.next();
            final Set<Value> rangeValues = readRangeValues(it);
            return new SetRange(rangeValues);
        }

        // or a range of integer values
        final Value startValue = readArithmeticExpression(it);
        // are there '..'?
        if (it.peek() instanceof IntervalDots) {
            ensureInteger(startValue, "Expected constant integer expression before '..'.");

            it.next();

            final Value endValue = readArithmeticExpression(it);
            ensureInteger(endValue, "Expected constant integer expression after '..'.");

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
        throw new ParseException("No valid range definition.");
    }

    private Set<Value> readRangeValues(ExtendedIterator<Token> it) throws ParseException {
        if (it.peek() instanceof RBrace) {
            it.next();
            return Collections.emptySet();
        }

        final Set<Value> values = new TreeSet<Value>();

        while (it.hasNext()) {
            final Value value = readArithmeticExpression(it);

            /*
            if (!(value instanceof ConstantValue))
                throw new ParseException("Only constant values allowed in ranges.");
            */

            values.add(value);

            if (!it.hasNext())
                throw new ParseException("Expected '}'");
            final Token nextToken = it.next();

            if (nextToken instanceof RBrace)
                return values;

            if (!(nextToken instanceof Comma))
                throw new ParseException("Expected ',' or '}'");
        }

        throw new ParseException("Expected '}'");
    }

    /**
     * Read all parameters up to the next RBracket (this token is read too).
     * @return <code>null</code> if there was no declaration
     * @throws ParseException if there was definitly a declaration, but it had
     *                        syntactical errors
     */
    private List<Parameter> readParameters(ExtendedIterator<Token> tokens) throws ParseException {
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
    private List<Value> readParameterValues(ExtendedIterator<Token> tokens) throws ParseException {

        if (tokens.hasNext() && tokens.peek() instanceof RBracket) {
            tokens.next();
            return Collections.emptyList();
        }

        final ArrayList<Value> readParameters = new ArrayList<Value>();

        while (tokens.hasNext()) {

            if (tokens.peek() instanceof RBracket) {
                tokens.next();
                readParameters.trimToSize();
                return readParameters;
            }

            final Value nextValue = readArithmeticExpression(tokens);

            readParameters.add(nextValue);

            if (!tokens.hasNext())
                throw new ParseException("Expected ']'");

            final Token nextToken = tokens.next();

            if (nextToken instanceof RBracket) {
                readParameters.trimToSize();
                return readParameters;
            }
            if (!(nextToken instanceof Comma))
                throw new ParseException("Expected ',' or ']'");
        }
        throw new ParseException("Expected ']'");
    }

    /**
     * Read one Expression.
     */
    private Expression readExpression(ExtendedIterator<Token> tokens) throws ParseException {
        // the topmost operator is restriction:
        return readRestrictExpression(tokens);
    }

    /**
     * Read the "main expression".
     */
    protected Expression readMainExpression(ExtendedIterator<Token> tokens) throws ParseException {
        return readExpression(tokens);
    }

    /**
     * Read one restriction expression.
     */
    private Expression readRestrictExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Expression expr = readParallelExpression(tokens);
        while (tokens.peek() instanceof Restrict) {
            tokens.next();
            if (!(tokens.next() instanceof LBrace))
                throw new ParseException("Expected '{'");
            final Set<Action> restricted = readRestrictionActionSet(tokens);
            expr = ExpressionRepository.getExpression(new RestrictExpression(expr, restricted));
        }

        return expr;
    }

    /**
     * Read all actions up to the next RBrace (this token is read too).
     */
    private Set<Action> readRestrictionActionSet(ExtendedIterator<Token> tokens) throws ParseException {
        final Set<Action> actions = new TreeSet<Action>();

        if (tokens.hasNext() && tokens.peek() instanceof RBrace) {
            tokens.next();
            return Collections.emptySet();
        }

        while (tokens.hasNext()) {

            final Action newAction = readAction(tokens, false);
            if (newAction == null)
                throw new ParseException("Expected an action here");

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
        final Channel channel = readChannel(tokens);
        if (channel == null)
            return null;
        if (channel instanceof TauChannel) {
            if (!tauAllowed)
                throw new ParseException("Tau action not allowed here");
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

            final Value value = readArithmeticBaseExpression(tokens); // may return null
            if (value instanceof ParameterReference)
                ((ParameterReference)value).getParam().setType(Parameter.Type.VALUE);
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
    private Value readOutputValue(ExtendedIterator<Token> tokens) throws ParseException {
        final Value value = readArithmeticBaseExpression(tokens); // may return null
        if (value instanceof ParameterReference)
            ((ParameterReference)value).getParam().setType(Parameter.Type.VALUE);
        return value;
    }

    private Channel readChannel(ExtendedIterator<Token> tokens) throws ParseException {
        if (!(tokens.peek() instanceof Identifier))
            return null;

        final Identifier identifier = (Identifier)tokens.next();
        Channel channel = null;
        if ("i".equals(identifier.getName()))
            channel = TauChannel.get();
        else if (!identifier.isQuoted()) {
            for (final Parameter param: parameters) {
                if (param.getName().equals(identifier.getName())) {
                    param.setType(Parameter.Type.CHANNEL);
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
    private Expression readParallelExpression(ExtendedIterator<Token> tokens) throws ParseException {
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
    private Expression readChoiceExpression(ExtendedIterator<Token> tokens) throws ParseException {
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
    private Expression readPrefixExpression(ExtendedIterator<Token> tokens) throws ParseException {
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

    private Expression readWhenExpression(ExtendedIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext() && tokens.peek() instanceof When) {
            tokens.next();
            final Value condition = readArithmeticExpression(tokens);
            ensureBoolean(condition, "Expected boolean expression after 'when'.");

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
    private Expression readBaseExpression(ExtendedIterator<Token> tokens) throws ParseException {
        if (tokens.hasNext()) {
            final Token nextToken = tokens.next();

            if (nextToken instanceof Stop)
                return ExpressionRepository.getExpression(StopExpression.get());

            if (nextToken instanceof ErrorToken)
                return ExpressionRepository.getExpression(ErrorExpression.get());

            if (nextToken instanceof LParenthesis) {
                final Expression expr = readExpression(tokens);
                if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                    throw new ParseException("Expected ')'");
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
                    final Expression expression = ExpressionRepository.getExpression(new UnknownRecursiveExpression(id.getName(), myParameters));
                    // hook for logging:
                    identifierParsed(id, expression);
                    return expression;
                }
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
        if (tokens.peek() instanceof QuestionMark) {
            tokens.next();
            ensureBoolean(orValue, "Boolean expression required before '?:' construct.");
            final Value thenValue = readArithmeticConditionalExpression(tokens);
            if (!(tokens.next() instanceof Colon))
                throw new ParseException("Expected ':'");
            final Value elseValue = readArithmeticConditionalExpression(tokens);
            ensureEqualTypes(thenValue, elseValue, "Expression in '?:' construct must have the same type.");
            return ConditionalValue.create(orValue, thenValue, elseValue);
        }

        return orValue;
    }

    private Value readArithmeticOrExpression(ExtendedIterator<Token> tokens) throws ParseException {
        Value value = readArithmeticAndExpression(tokens);
        while (tokens.peek() instanceof Or) {
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
        while (tokens.peek() instanceof Equals
                || tokens.peek() instanceof Neq) {
            final boolean isNeq = tokens.next() instanceof Neq;
            final Value secondValue = readArithmeticCompExpression(tokens);
            ensureEqualTypes(value, secondValue, "Values to compare must have the same type.");
            value = EqValue.create(value, secondValue, isNeq);
        }

        return value;
    }

    private Value readArithmeticCompExpression(ExtendedIterator<Token> it) throws ParseException {
        final Value value = readArithmeticShiftExpression(it);

        final Token nextToken = it.peek();
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
            it.next();
            ensureInteger(value, "Only integer values can be compared.");
            final Value secondValue = readArithmeticShiftExpression(it);
            ensureInteger(secondValue, "Only integer values can be compared.");
            return CompValue.create(value, secondValue, type);
        }

        return value;
    }

    private Value readArithmeticShiftExpression(ExtendedIterator<Token> it) throws ParseException {
        Value value = readArithmeticAddExpression(it);
        while (it.peek() instanceof LeftShift
                || it.peek() instanceof RightShift) {
            ensureInteger(value, "Only integer values can be shifted.");
            final boolean shiftRight = it.next() instanceof RightShift;
            final Value secondValue = readArithmeticAddExpression(it);
            ensureInteger(secondValue, "Shifting width must be an integer.");
            value = ShiftValue.create(value, secondValue, shiftRight);
        }

        return value;
    }

    private Value readArithmeticAddExpression(ExtendedIterator<Token> it) throws ParseException {
        Value value = readArithmeticMultExpression(it);
        while (it.peek() instanceof Plus
                || it.peek() instanceof Minus) {
            ensureInteger(value, "Both sides of an addition must be integers.");
            final boolean isSubtraction = it.next() instanceof Minus;
            final Value secondValue = readArithmeticMultExpression(it);
            ensureInteger(secondValue, "Both sides of an addition must be integers.");
            value = AddValue.create(value, secondValue, isSubtraction);
        }

        return value;
    }

    private Value readArithmeticMultExpression(ExtendedIterator<Token> it) throws ParseException {
        Value value = readArithmeticUnaryExpression(it);
        while (true) {
            final Token nextToken = it.peek();
            MultValue.Type type = null;
            if (nextToken instanceof Multiplication)
                type = MultValue.Type.MULT;
            else if (nextToken instanceof Division)
                type = MultValue.Type.DIV;
            else if (nextToken instanceof Modulo)
                type = MultValue.Type.MOD;

            if (type == null)
                break;

            it.next();

            ensureInteger(value, "Both sides of a multiplication/division must be integer expressions.");
            final Value secondValue = readArithmeticUnaryExpression(it);
            ensureInteger(secondValue, "Both sides of a multiplication/division must be integer expressions.");
            value = MultValue.create(value, secondValue, type);
        }

        return value;
    }

    private Value readArithmeticUnaryExpression(ExtendedIterator<Token> it) throws ParseException {
        final Token nextToken = it.peek();
        if (nextToken instanceof Exclamation) {
            it.next();
            final Value negatedValue = readArithmeticUnaryExpression(it);
            ensureBoolean(negatedValue, "The negated value must be a boolean expression.");
            return NotValue.create(negatedValue);
        } else if (nextToken instanceof Plus) {
            it.next();
            return readArithmeticUnaryExpression(it);
        } else if (nextToken instanceof Minus) {
            it.next();
            final Value negativeValue = readArithmeticUnaryExpression(it);
            ensureInteger(negativeValue, "The negated value must be an integer expression.");
            return NegativeValue.create(negativeValue);
        }

        // else:
        return readArithmeticBaseExpression(it);
    }

    private Value readArithmeticBaseExpression(ExtendedIterator<Token> tokens) throws ParseException {
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
                throw new ParseException("Expected ')'.");
            return value;
        }
        tokens.previous();
        return null;
    }

    private void ensureEqualTypes(Value value1, Value value2, String message) throws ParseException {
        if (value1 instanceof IntegerValue && value2 instanceof IntegerValue)
            return;
        if (value1 instanceof BooleanValue && value2 instanceof BooleanValue)
            return;
        if (value1 instanceof ConstString && value2 instanceof ConstString)
            return;
        if (value1 instanceof ParameterReference || value1 instanceof ParameterRefChannel) {
            ((ParameterReference)value1).getParam().match(value2);
            return;
        }
        if (value2 instanceof ParameterReference || value2 instanceof ParameterRefChannel) {
            ((ParameterReference)value2).getParam().match(value1);
            return;
        }
        if (value1 instanceof ConditionalValue) {
            ensureEqualTypes(((ConditionalValue)value1).getThenValue(), value2, message);
            ensureEqualTypes(((ConditionalValue)value1).getElseValue(), value2, message);
        } else if (value2 instanceof ConditionalValue) {
            ensureEqualTypes(value1, ((ConditionalValue)value2).getThenValue(), message);
            ensureEqualTypes(value1, ((ConditionalValue)value2).getElseValue(), message);
        }
        throw new ParseException(message + " The values \"" + value1 + "\" and \"" + value2 + "\" have different types.");
    }

    private void ensureBoolean(Value value, String message) throws ParseException {
        if (value instanceof BooleanValue)
            return;
        if (value instanceof IntegerValue)
            throw new ParseException(message + " The value \"" + value + "\" has type integer.");
        if (value instanceof ConstString)
            throw new ParseException(message + " The value \"" + value + "\" has type string.");
        if (value instanceof ParameterReference) {
            ((ParameterReference)value).getParam().setType(Parameter.Type.BOOLEANVALUE);
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
            throw new ParseException(message + " The value \"" + value + "\" has type boolean.");
        if (value instanceof ConstString)
            throw new ParseException(message + " The value \"" + value + "\" has type string.");
        if (value instanceof ParameterReference) {
            try {
                ((ParameterReference)value).getParam().setType(Parameter.Type.INTEGERVALUE);
            } catch (final ParseException e) {
                throw new ParseException(message + e.getMessage(), e);
            }
            return;
        }
        if (value instanceof ConditionalValue) {
            ensureInteger(((ConditionalValue)value).getThenValue(), message);
            ensureInteger(((ConditionalValue)value).getElseValue(), message);
        }
        assert false;
        throw new ParseException(message);
    }

    protected void identifierParsed(Identifier identifier, Object semantic) {
        // ignore in this implementation
    }

    protected void changedIdentifierMeaning(ConstString constString,
            Range range) {
        // ignore in this implementation
    }

}
