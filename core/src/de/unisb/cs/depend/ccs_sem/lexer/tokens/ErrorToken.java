package de.unisb.cs.depend.ccs_sem.lexer.tokens;


public class ErrorToken extends AbstractToken {

    public ErrorToken(int startPosition, int endPosition) {
        super(startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "ERROR";
    }

}
