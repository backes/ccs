package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.Reader;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;

public class LoggingCCSLexer extends CCSLexer {

    private final ParsingResult result;

    public LoggingCCSLexer() {
        this.result = new ParsingResult();
    }

    public LoggingCCSLexer(ParsingResult result) {
        if (result == null)
            throw new NullPointerException();
        this.result = result;
    }

    public ParsingResult getResult() {
        return result;
    }

    @Override
    public List<Token> lex(Reader input) throws LexException {
        List<Token> tokens = super.lex(input);
        result.inputLength = position;
        return tokens;
    }

    @Override
    protected void commentRead(int startPosition, int endPosition) {
        result.newComment(startPosition, endPosition);
        super.commentRead(startPosition, endPosition);
    }

    @Override
    protected void completeLine() {
        result.lineStarts.add(position+1);
        super.completeLine();
    }

}
