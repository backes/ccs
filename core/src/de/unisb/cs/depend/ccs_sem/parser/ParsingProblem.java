package de.unisb.cs.depend.ccs_sem.parser;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;

public class ParsingProblem {

    // problem types
    public static final int WARNING = 0;
    public static final int ERROR = 1;

    private final int type;
    private final String message;
    private final int startPosition;
    private final int endPosition;

    public ParsingProblem(int type, String message, int startPosition, int endPosition) {
        this.type = type;
        this.message = message;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public ParsingProblem(int type, String message, Token token) {
        this.type = type;
        this.message = message;
        this.startPosition = token.getStartPosition();
        this.endPosition = token.getEndPosition();
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

}
