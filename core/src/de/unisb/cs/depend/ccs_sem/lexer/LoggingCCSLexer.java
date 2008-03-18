package de.unisb.cs.depend.ccs_sem.lexer;

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
    protected void commentRead(int startPosition, int endPosition) {
        result.newComment(startPosition, endPosition);
        super.commentRead(startPosition, endPosition);
    }

}
