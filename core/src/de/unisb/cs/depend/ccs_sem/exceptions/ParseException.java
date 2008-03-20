package de.unisb.cs.depend.ccs_sem.exceptions;

import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;


public class ParseException extends Exception {

    private static final long serialVersionUID = 279050231911730217L;
    private final int startPosition;
    private final int endPosition;
    private final Token token;

    public ParseException(String message, Token token) {
        super(message);
        this.startPosition = token.getStartPosition();
        this.endPosition = token.getEndPosition();
        this.token = token;
    }

    public ParseException(String message, int startPosition,
            int endPosition) {
        super(message);
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.token = null;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public Token getToken() {
        return token;
    }

}
