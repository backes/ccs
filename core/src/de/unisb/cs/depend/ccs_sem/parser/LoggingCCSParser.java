package de.unisb.cs.depend.ccs_sem.parser;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.LoggingCCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
import de.unisb.cs.depend.ccs_sem.semantics.types.values.ConstString;



public class LoggingCCSParser extends CCSParser {

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
    public Program parse(Reader input) throws ParseException, LexException {
        return parse(new LoggingCCSLexer(result).lex(input));
    }

    @Override
    public Program parse(String input) throws ParseException, LexException {
        return parse(new LoggingCCSLexer(result).lex(input));
    }

    public ParsingResult getResult() {
        return result;
    }

    @Override
    public synchronized Program parse(List<Token> tokens) throws ParseException {
        result.tokens = tokens;
        return super.parse(tokens);
    }

    @Override
    protected ProcessVariable readProcessDeclaration(ExtendedIterator<Token> tokens)
            throws ParseException {
        final int tokenPositionBefore = tokens.nextIndex();
        final ProcessVariable readProcessVariable = super.readProcessDeclaration(tokens);
        final int tokenPositionAfter = tokens.previousIndex();
        if (readProcessVariable != null) {
            result.addProcessVariable(readProcessVariable, tokenPositionBefore, tokenPositionAfter);
        }
        return readProcessVariable;
    }

    @Override
    protected Expression readMainExpression(ExtendedIterator<Token> tokens)
            throws ParseException {
        final int tokenPositionBefore = tokens.nextIndex();
        final Expression readExpression = super.readMainExpression(tokens);
        final int tokenPositionAfter = tokens.previousIndex();
        result.mainExpressionTokenIndexStart = tokenPositionBefore;
        result.mainExpressionTokenIndexEnd = tokenPositionAfter;
        return readExpression;
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

}
