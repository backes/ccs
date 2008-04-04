package de.unisb.cs.depend.ccs_sem.parser;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.LoggingCCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstString;
import de.unisb.cs.depend.ccs_sem.utils.Pair;



public class LoggingCCSParser extends CCSParser implements IParsingProblemListener {

    private final ParsingResult result;

    public LoggingCCSParser() {
        this(new ParsingResult());
    }

    public LoggingCCSParser(ParsingResult result) {
        if (result == null)
            throw new NullPointerException();
        this.result = result;
    }

    @Override
    public Program parse(Reader input) {
        addProblemListener(this);
        final Program program = super.parse(input);
        removeProblemListener(this);
        result.finish();
        return program;
    }

    @Override
    public Program parse(String input) {
        addProblemListener(this);
        final Program program = super.parse(input);
        removeProblemListener(this);
        result.finish();
        return program;
    }

    @Override
    protected CCSLexer getDefaultLexer() {
        return new LoggingCCSLexer(result);
    }

    public ParsingResult getResult() {
        return result;
    }

    @Override
    public Program parse(List<Token> tokens) {
        result.tokens = tokens;
        addProblemListener(this);
        final Program program = super.parse(tokens);
        removeProblemListener(this);
        result.finish();
        return program;
    }

    @Override
    protected ProcessVariable readProcessDeclaration(ExtendedListIterator<Token> tokens) {
        final int tokenPositionBefore = tokens.nextIndex();
        final ProcessVariable readProcessVariable = super.readProcessDeclaration(tokens);
        final int tokenPositionAfter = tokens.previousIndex();
        if (readProcessVariable != null) {
            result.addProcessVariable(readProcessVariable, tokenPositionBefore, tokenPositionAfter);
        }
        return readProcessVariable;
    }

    @Override
    protected Expression readMainExpression(ExtendedListIterator<Token> tokens)
            throws ParseException {
        final int tokenPositionBefore = tokens.nextIndex();
        final Expression readExpression = super.readMainExpression(tokens);
        final int tokenPositionAfter = tokens.previousIndex();
        result.mainExpressionTokenIndexStart = tokenPositionBefore;
        result.mainExpressionTokenIndexEnd = tokenPositionAfter;
        return readExpression;
    }

    @Override
    protected Action readAction(ExtendedListIterator<Token> tokens) throws ParseException {
        final Token firstToken = tokens.peek();
        final Action readAction = super.readAction(tokens);
        if (readAction != null) {
            final Token endToken = tokens.peekPrevious();
            result.actions.add(new Pair<Action, Pair<Token, Token>>(
                    readAction, new Pair<Token, Token>(firstToken, endToken)));
        }
        return readAction;
    }

    @Override
    protected void identifierParsed(Identifier identifier, Object semantic) {
        result.addIdentifierMapping(identifier, semantic);
    }

    @Override
    protected void changedIdentifierMeaning(ConstString constString, Range range) {
        final Map<Identifier, Object> newMappings = new HashMap<Identifier, Object>();
        for (final Entry<Identifier, Object> entry: result.identifiers.entrySet()) {
            if (entry.getValue().equals(constString))
                newMappings.put(entry.getKey(), range);
        }
        result.identifiers.putAll(newMappings);
    }

    @Override
    protected void reportUnboundInputParameter(Action act) {
        for (final Pair<Action, Pair<Token, Token>> readAct: result.actions) {
            if (readAct.getFirst().equals(act)) {
                reportProblem(new ParsingProblem(ParsingProblem.ERROR,
                    "This action is not restricted and without a range. "
                        + "This would leed to infinitely many transitions.",
                    readAct.getSecond().getFirst().getStartPosition(),
                    readAct.getSecond().getSecond().getEndPosition()
                    ));
            }
        }
    }

    public void reportParsingProblem(ParsingProblem problem) {
        result.parsingProblems.add(problem);
    }

}
